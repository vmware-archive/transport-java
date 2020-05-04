package samples.bifrost;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BifrostMetadataService extends AbstractService<Request<String>, Response<Object>> {
    public static final String Channel = "bifrost-metadata-service";
    public static final String GITLAB_HOST = "gitlab.eng.vmware.com";
    public static final String TYPESCRIPT_REPO_URI = "bifrost/typescript.git";

    private final ObjectMapper objectMapper;
    private final Lock lock;

    @Value("${gitlab.bifrost.typescript.tokens.id}")
    private String gitlabBifrostTSReadTokenId;

    @Value("${gitlab.bifrost.typescript.tokens.secret}")
    private String gitlabBifrostTSReadTokenSecret;

    private final ExecutorService executorService;
    private Object bifrostMetadataCache;
    private AtomicBoolean cacheUpdateInProgress;

    public BifrostMetadataService() {
        super(BifrostMetadataService.Channel);
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);

        this.executorService = Executors.newFixedThreadPool(5);
        this.objectMapper = new ObjectMapper(jsonFactory);
        this.lock = new ReentrantLock();
        this.cacheUpdateInProgress = new AtomicBoolean(false);

        // clean cache every 24 hours
        this.setCacheCleanerTimer(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1));
    }

    @Override
    protected void handleServiceRequest(Request request, Message busMessage) {
        switch (request.getRequest()) {
            case BifrostMetadataServiceCommand.TSLibMetadata:
                try {
                    bus.sendResponseMessageWithId(BifrostMetadataService.Channel, getBifrostTSMetadata(), request.getId());

                } catch (ExecutionException | IOException | InterruptedException e) {
                    e.printStackTrace();
                    bus.sendErrorMessageWithId(BifrostMetadataService.Channel, e.getMessage(), request.getId());
                }
                break;

            case BifrostMetadataServiceCommand.TSLibLatestVersion:
                try {
                    String version = getBifrostLatestVersion();
                    sendResponse(new Response(request.getId(), version), request.getId());

                } catch (ExecutionException | IOException | InterruptedException | JsonSyntaxException e) {
                    e.printStackTrace();
                    bus.sendErrorMessageWithId(BifrostMetadataService.Channel, e.getMessage(), request.getId());
                }
                break;
            default:
                super.handleUnknownRequest(request);
        }
    }

    /**
     * Clears cache {@link #bifrostMetadataCache} every {@code interval} after initial {@code delay}
     *
     * @param delay initial delay
     * @param interval interval at which to clear cache
     */
    private synchronized void setCacheCleanerTimer(long delay, long interval) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                bifrostMetadataCache = null;
                log.info("bifrostMetadataCache cleared after 24 hours");
            }
        }, delay, interval);
    }

    /**
     * Retrieve Bifrost TypeScript metadata from GitLab repo. If the cache is found, return the results from cache
     * directly instead of cloning the git repo.
     *
     * @return {@code Object} as a result of the Bifrost TypeScript metadata
     */
    public Object getBifrostTSMetadata() throws ExecutionException, IOException, InterruptedException {
        // if metadata cache is in the process of getting updated by another thread, wait until
        // the operation has finished so other requests could use the cached results instead of having to invoke
        // the same expensive operation.
        while (cacheUpdateInProgress.get()) {
            log.debug("Bifrost TS metadata cache is being updated. Waiting for it to finish...");
            Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100));
        }

        // if cache is set, return it here.
        if (bifrostMetadataCache != null) {
            return bifrostMetadataCache;
        }

        // if cache is empty, start a task to get the data. while this operation is in progress, other threads
        // will be kept from accessing {@link #bifrostMetadataCache} until {@link #cacheUpdateInProgress} is false.
        lock.lock();
        cacheUpdateInProgress.getAndSet(true);

        Path tempDir = null;
        Future<String> output;

        try {
            tempDir = Files.createTempDirectory("bifrost-version-svc");
            output = executorService.submit(new FetchBifrostMetadataTask(
                    tempDir,
                    GITLAB_HOST,
                    TYPESCRIPT_REPO_URI,
                    gitlabBifrostTSReadTokenId,
                    gitlabBifrostTSReadTokenSecret));

            // remove invalid string literals in the JSON string
            String results = output.get()
                    .replaceAll("//@exclude", "")
                    .replaceAll("//@endexclude", "");

            // store the parsed results in the cache
            bifrostMetadataCache = objectMapper.readValue(results, Object.class);
        } catch (ExecutionException | IOException | InterruptedException e) {
            // throw again so that we can send the error message to consumer
            throw e;
        } finally {
            // clean up resources
            FileSystemUtils.deleteRecursively(tempDir.toFile());
            cacheUpdateInProgress.getAndSet(false);
            lock.unlock();
        }

        return bifrostMetadataCache;
    }

    /**
     * Retrieve the latest Bifrost TS version from {@link #getBifrostTSMetadata()} by walking the JSON nodes
     * to the first (and thus the latest) change log entry, and returning the version from there.
     *
     * @return {@String} version of the latest change log entry
     * @throws ExecutionException
     * @throws IOException when File IO, ObjectMapper, or malformed JSON tree where the latest change log could not be found
     * @throws InterruptedException
     * @throws JsonSyntaxException
     */
    public String getBifrostLatestVersion() throws ExecutionException, IOException, InterruptedException, JsonSyntaxException {
        com.google.gson.JsonParser jsonParser = new com.google.gson.JsonParser();
        String notFoundErrMessage = "Latest Bifrost TypeScript could not be fetched";
        boolean isError = false;

        JsonElement jsonElement = jsonParser.parse(
                objectMapper.writer().writeValueAsString(getBifrostTSMetadata()));
        if (jsonElement.isJsonObject()) {
            JsonObject root = jsonElement.getAsJsonObject();
            JsonElement changelogHistoryElem = root.get("changelogHistory");
            isError = changelogHistoryElem == null;

            if (!isError && changelogHistoryElem.isJsonArray()) {
                JsonArray changelogsJsonArray = changelogHistoryElem.getAsJsonArray();
                JsonElement firstChangeLogElem = changelogsJsonArray.get(0);
                isError = firstChangeLogElem == null;

                if (!isError && firstChangeLogElem.isJsonObject()) {
                    JsonObject firstChangelogObject = firstChangeLogElem.getAsJsonObject();
                    JsonElement latestVerElem = firstChangelogObject.get("version");
                    isError = latestVerElem == null;

                    if (!isError) {
                        return latestVerElem.getAsString();
                    }
                }
            }
        }

        if (isError) {
            throw new IOException(notFoundErrMessage);
        }

        return null;
    }
}

package samples.bifrost;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import com.vmware.bifrost.core.model.RestOperation;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Component
public class BifrostMetricsService extends AbstractService<Request<String>, Response<Object>> {
    public static final String Channel = "bifrost-metrics-service";
    public static final String BIFROST_TS_STATS_URL = "https://build-artifactory.eng.vmware.com/" +
            "artifactory/ui/v1/native/packages/npm/extra_info?packageName=@vmw%2Fbifrost";
    public static final List<Map<String, Object>> bifrostTSStatsRequestBody;

    static {
        Map<String, Object> requestBodyMap = new LinkedHashMap<>();
        requestBodyMap.put("id", "npmName");
        requestBodyMap.put("comparator", "equals");
        requestBodyMap.put("values", new String[]{"@vmw/bifrost"});

        // wrap the map around a list because the API expects the post to be an array of maps
        List<Map<String, Object>> requestBody = new LinkedList<>();
        requestBody.add(requestBodyMap);

        // make the body immutable
        bifrostTSStatsRequestBody = Collections.unmodifiableList(requestBody);
    }

    BifrostMetricsService() {
        super(BifrostMetricsService.Channel);
    }

    @Override
    protected void handleServiceRequest(Request request, Message busMessage) {
        switch (request.getRequest()) {
            case Command.GetTSLibDownloadsCount:
                try {
                    RestOperation restOperation = new RestOperation();
                    restOperation.setId(UUID.randomUUID());
                    restOperation.setUri(new URI(BIFROST_TS_STATS_URL));
                    restOperation.setApiClass("java.lang.Object");
                    restOperation.setBody(bifrostTSStatsRequestBody);
                    restOperation.setMethod(HttpMethod.POST);
                    restOperation.setSuccessHandler((Object response) -> {
                        bus.sendResponseMessageWithId(BifrostMetricsService.Channel, response, request.getId());
                    });
                    restOperation.setErrorHandler((Object error) -> {
                        bus.sendErrorMessageWithId(BifrostMetricsService.Channel, error, request.getId());
                    });
                    this.restServiceRequest(restOperation);

                } catch (URISyntaxException e) {
                    bus.sendErrorMessageWithId(BifrostMetricsService.Channel, e.getMessage(), request.getId());
                    e.printStackTrace();
                }
                break;

            default:
                super.handleUnknownRequest(request);
        }
    }
}

abstract class Command {
    static final String GetTSLibDownloadsCount = "ts-lib-download-count";
}

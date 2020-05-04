package samples.bifrost;

import com.vmware.bifrost.core.util.Loggable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class FetchBifrostMetadataTask extends Loggable implements Callable<String> {
    private Path tempDir;
    private String gitlabHost;
    private String gitlabRepoName;
    private String repoUser;
    private String repoPassword;
    private ProcessBuilder processBuilder;

    FetchBifrostMetadataTask(Path tempDir, String gitlabHost, String gitlabRepoName, String repoUser, String repoPassword) {
        assert tempDir != null;
        assert gitlabHost != null;
        assert gitlabRepoName != null;
        assert repoUser != null;
        assert repoPassword != null;
        assert gitlabHost != null;

        this.gitlabHost = gitlabHost;
        this.repoUser = repoUser;
        this.repoPassword = repoPassword;
        this.gitlabRepoName = gitlabRepoName;
        this.processBuilder = new ProcessBuilder();
        this.tempDir = tempDir;
    }

    @Override
    public String call() throws IOException, InterruptedException {
        String fullRepoURL = String.format("https://%s:%s@%s/%s", repoUser, repoPassword, gitlabHost, gitlabRepoName);

        // set working directory
        processBuilder.directory(tempDir.toFile());

        // build a command that would look something like below:
        // git clone https://id:password@gitlab.eng.vmware.com/bifrost/typescript && cat typescript/build/npm/bifrost.json
        processBuilder.command("bash", "-c",
                String.format("git clone %s && cat %s", fullRepoURL, buildPathToBifrostTSMetadataFile()));

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitVal = process.waitFor();
        if (exitVal != 0) {
            throw new RuntimeException(output.toString());
        }

        return output.toString();
    }

    /**
     * Build the absolute path to bifrost.json which is the metadata file for the Bifrost TS library
     *
     * @return {@code String} as an absolute path to bifrost.json
     */
    private String buildPathToBifrostTSMetadataFile() {
        StringBuilder pathBuilder = new StringBuilder();

        pathBuilder.append(tempDir + File.separator);
        pathBuilder.append("typescript" + File.separator);
        pathBuilder.append("build" + File.separator);
        pathBuilder.append("npm" + File.separator);
        pathBuilder.append("bifrost.json");

        return pathBuilder.toString();
    }
}

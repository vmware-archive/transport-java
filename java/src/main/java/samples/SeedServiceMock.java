package samples;

import com.vmware.bifrost.bridge.Response;
import io.swagger.client.ApiException;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import com.vmware.bifrost.bridge.Request;
import samples.model.SeedResponse;

import java.util.List;

@Service("seedServiceMock")
@Profile("test")
public class SeedServiceMock extends SeedService implements ApplicationListener<ContextRefreshedEvent> {

    public SeedServiceMock(SeedApi seedApi) {
        super(seedApi);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.loadSampleModels();
    }

    @Override
    protected Response getSeeds(Request request) {
        this.logDebugMessage("Running Mock API Method", "getSeeds()");

        if (!this.mockFail) {
            List<Seed> result = mockModel.seed;

            this.logDebugMessage("Mock API call success","getSeeds()");
            Response response = new SeedResponse(request.getId(), result);
            this.sendResponse(response);
            return response;

        } else {
            this.apiFailedHandler(
                    new SeedResponse(request.getId(), true),
                    new ApiException("Mock API call failed"),
                    "getSeeds()");
        }
        return null;
    }

    @Override
    protected void plantSeed(Request request) {
        this.logDebugMessage("Running Mock API Method", "plantSeed()");

        if (!this.mockFail) {
            List<Seed> result = mockModel.seed.subList(0, 1);

            this.logDebugMessage("Mock API call success","plantSeed()");
            this.sendResponse(new SeedResponse(request.getId(), result));

        } else {
            this.apiFailedHandler(
                    new SeedResponse(request.getId(), true),
                    new ApiException("Mock API call failed"),
                    "plantSeed()");
        }
    }

    @Override
    protected void killPlant(Request request) {
        this.logDebugMessage("Running Mock API Method", "killPlant()");

        if (!this.mockFail) {

            this.logDebugMessage("Mock API call success","killPlant()");
            this.sendResponse(new SeedResponse(request.getId(), null));

        } else {
            this.apiFailedHandler(
                    new SeedResponse(request.getId(), true),
                    new ApiException("Mock API call failed"),
                    "killPlant()");
        }
    }
}

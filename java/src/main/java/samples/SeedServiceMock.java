package samples;

import io.swagger.client.ApiException;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import samples.model.SeedRequest;
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
    protected void getSeeds(SeedRequest request) {
        this.logDebugMessage("Running Mock API Method", "getSeeds()");

        if (!this.mockFail) {
            List<Seed> result = mockModel.seed;

            this.logDebugMessage("Mock API call success for getSeeds()", String.valueOf(result.size()));
            this.sendResponse(new SeedResponse(request.getUuid(), result));

        } else {
            this.apiFailedHandler(
                    new SeedResponse(request.getUuid(), null),
                    new ApiException(),
                    "getSeeds()");
        }
    }
}

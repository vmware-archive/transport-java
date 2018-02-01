package samples;

import io.swagger.client.ApiException;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import samples.model.SeedRequest;
import samples.model.SeedResponse;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("seedService")
@Profile("prod")
public class SeedService extends AbstractService<SeedRequest, SeedResponse> {

    public static String Channel = "service-seed";

    protected SeedApi seedApi;

    public SeedService(SeedApi seedApi) {
        super(SeedService.Channel);
        this.seedApi = seedApi;
    }

    @Override
    public void handleServiceRequest(SeedRequest request) {
        switch (request.getType()) {
            case GetSeeds:
                this.getSeeds(request);
                break;

            case PlantSeed:
                this.plantSeed(request);
                break;

            case KillPlant:
                this.killPlant(request);
                break;

        }
    }

    protected void getSeeds(SeedRequest request) {
        super.logDebugMessage("Running API Method", "getSeeds()");
        try {

            List<Seed> result = this.seedApi.getSeeds();
            this.logDebugMessage("API call success for","getSeeds()");
            this.sendResponse(new SeedResponse(request.getUuid(), result));

        } catch (ApiException e) {
            this.apiFailedHandler(new SeedResponse(request.getUuid(), null), e, "getSeeds()");
        }

    }

    protected void plantSeed(SeedRequest request) {
        super.logDebugMessage("Running API Method", "plantSeed()");
        try {

            Seed seed = this.seedApi.plantSeed(request.getPayload());
            List<Seed> seeds = new ArrayList<>(Arrays.asList(seed));
            this.logDebugMessage("API call success for","plantSeed()");
            this.sendResponse(new SeedResponse(request.getUuid(), seeds));

        } catch (ApiException e) {
            this.apiFailedHandler(new SeedResponse(request.getUuid(), null), e, "plantSeed()");
        }

    }

    protected void killPlant(SeedRequest request) {
        super.logDebugMessage("Running API Method", "killPlant()");
        try {

            this.seedApi.killPlant(request.getPayload());
            this.logDebugMessage("API call success for","killPlant()");
            this.sendResponse(new SeedResponse(request.getUuid(), null));

        } catch (ApiException e) {
            this.apiFailedHandler(new SeedResponse(request.getUuid(), null), e, "killPlant()");
        }

    }
}

package samples;

import com.vmware.bifrost.AbstractService;
import io.swagger.client.ApiException;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.springframework.stereotype.Service;
import samples.model.SeedRequest;
import samples.model.SeedResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeedService extends AbstractService<SeedRequest, SeedResponse> {

    public static String Channel = "service-seedservice";

    private SeedApi seedApi;

    public SeedService() {
        super(SeedService.Channel);
        this.seedApi = new SeedApi();
    }

    @Override
    public void handleServiceRequest(SeedRequest request) {
        switch(request.getType()) {
            case GetSeeds:
                this.getSeeds(request);
                break;

            case PlantSeed:
//                this.plantSeed(request);
                break;

            case KillPlant:
//                this.killPlant(request);
                break;

            default:
                break;
        }
    }

    private void getSeeds(SeedRequest request) {
        super.logDebugMessage("Running API Method", "getSeeds()");
        List<Seed> result;
        try {

            result = this.seedApi.getSeeds();

            super.logDebugMessage("API call success for getSeeds()", String.valueOf(result.size()));
            this.sendResponse(new SeedResponse(request.getUuid(), result));

        } catch (ApiException e) {
            this.apiFailedHandler(new SeedResponse(request.getUuid(), null), e);
        }

    }

    private void plantSeed(SeedRequest request) {
        try {
            this.seedApi.plantSeed(request.getPayload());

            List<Seed> seeds = new ArrayList<>();
            seeds.add(request.getPayload());

            this.sendResponse(new SeedResponse(request.getUuid(), seeds));

        } catch (ApiException e) {

            this.sendError("unable to call SeedApi#plantSeed()");
            e.printStackTrace();
        }
    }

    private void killPlant(SeedRequest request) {
        try {
            this.seedApi.killPlant(request.getPayload().getId().toString());

            List<Seed> seeds = new ArrayList<>();
            seeds.add(request.getPayload());

            this.sendResponse(new SeedResponse(request.getUuid(), seeds));

        } catch (ApiException e) {

            this.sendError("unable to call SeedApi#killPlant()");
            e.printStackTrace();
        }
    }
}

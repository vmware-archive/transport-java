package samples;

import com.vmware.bifrost.AbstractService;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.springframework.stereotype.Service;
import samples.model.SeedRequest;
import samples.model.SeedResponse;

import java.util.List;

public class SeedService extends AbstractService<SeedRequest, SeedResponse> {

    public static String Channel = "service-seedservice";

    private SeedApi seedApi;

    public SeedService() {
        super(SeedService.Channel);
        this.seedApi = new SeedApi();
    }

    public void getSeeds(SeedRequest request) {

        try {
            List<Seed> result = this.seedApi.getSeeds();
            this.sendResponse(new SeedResponse(request.getUuid(), result));

        } catch (ApiException e) {

            this.sendError("unable to call SeedApi#getSeeds()");
            e.printStackTrace();
        }

    }

    public void plantSeed(SeedRequest request) {
        try {
            // todo come and fix this with a regen
            this.seedApi.plantSeed(request.getPayload().get(0));
            // todo.. fix the api, this sucks.
            this.sendResponse(new SeedResponse(request.getUuid(), request.getPayload()));

        } catch (ApiException e) {

            this.sendError("unable to call SeedApi#plantSeed()");
            e.printStackTrace();
        }
    }

    public void killPlant(SeedRequest request) {
        try {
            // todo come and fix this with a regen
            this.seedApi.killPlant(request.getPayload().get(0).getId().toString());
            // todo.. fix the api, this sucks.
            this.sendResponse(new SeedResponse(request.getUuid(), request.getPayload()));

        } catch (ApiException e) {

            this.sendError("unable to call SeedApi#killPlant()");
            e.printStackTrace();
        }
    }

    @Override
    public void initializeSubscriptions() {

    }

    @Override
    public void handleServiceRequest(SeedRequest request) {
        switch(request.getType()) {
            case GetSeeds:
                this.getSeeds(request);
                break;

            case PlantSeed:
                this.plantSeed(request);
                break;

            case KillPlant:
                this.killPlant(request);
                break;

            default:
                break;
        }
    }
}

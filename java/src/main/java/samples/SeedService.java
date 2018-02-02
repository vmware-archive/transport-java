package samples;

import io.swagger.client.ApiException;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("seedService")
@Profile("prod")
public class SeedService extends AbstractService {

    public static String Channel = "service-seed";

    protected SeedApi seedApi;

    public SeedService(SeedApi seedApi) {
        super(SeedService.Channel);
        this.seedApi = seedApi;
    }

    @Override
    public void handleServiceRequest(Request request) {
        switch (request.getType()) {
            case "GetSeeds":
                this.getSeeds(request);
                break;

            case "PlantSeed":
                this.plantSeed(request);
                break;

            case "KillPlant":
                this.killPlant(request);
                break;

        }
    }

    protected void getSeeds(Request request) {
        super.logDebugMessage("Running API Method", "getSeeds()");
        try {

            List<Seed> result = this.seedApi.getSeeds();
            this.logDebugMessage("API call success for","getSeeds()");
            this.sendResponse(new Response(request.getId(), result));

        } catch (ApiException e) {
            this.apiFailedHandler(
                    new Response(request.getVersion(), request.getId(), true), e, "getSeeds()");
        }

    }

    protected void plantSeed(Request request) {
        super.logDebugMessage("Running API Method", "plantSeed()");
        try {

            Seed requestSeed = this.castPayload(Seed.class, request);
            Seed responseSeed = this.seedApi.plantSeed(requestSeed);
            List<Seed> seeds = new ArrayList<>(Arrays.asList(responseSeed));
            this.logDebugMessage("API call success for", "plantSeed()");
            this.sendResponse(new Response(request.getId(), seeds));

        } catch (ApiException e) {
            this.apiFailedHandler(new Response(request.getId(), null), e, "plantSeed()");
        }

    }

    protected void killPlant(Request request) {
        super.logDebugMessage("Running API Method", "killPlant()");
        try {

            Seed requestSeed = this.castPayload(Seed.class, request);
            this.seedApi.killPlant(requestSeed);
            this.logDebugMessage("API call success for","killPlant()");
            this.sendResponse(new Response(request.getId(), null));

        } catch (ApiException e) {
            this.apiFailedHandler(new Response(request.getId(), null), e, "killPlant()");
        }

    }
}

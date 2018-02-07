package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import io.swagger.client.model.Seed;
import org.springframework.stereotype.Component;

@CustomServiceCode(serviceName="SeedService")
@Component
public class SeedServiceCustom {

    @CustomServiceCodeHandler(stage = RunStage.Before, methodName = "getSeeds")
    public void getSeedsBefore(Request<Seed> request) {
        // preprocess
    }

    @CustomServiceCodeHandler(stage = RunStage.After, methodName = "getSeeds")
    public void getSeedsAfter(Response<Seed> response) {
        for(Seed seed: response.getPayload()) {
            // post process
        }
    }

}

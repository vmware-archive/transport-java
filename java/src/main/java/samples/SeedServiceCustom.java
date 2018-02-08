package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import io.swagger.client.model.Seed;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@CustomServiceCode(serviceName = "SeedService")
@Component
public class SeedServiceCustom {

    @CustomServiceCodeHandler(stage = RunStage.Before, methodName = "getSeeds")
    public void getSeedsBefore(Request request) {
        // preprocess
    }

    @CustomServiceCodeHandler(stage = RunStage.After, methodName = "getSeeds")
    public void getSeedsAfter(Response response) {
        // postprocess
        List<MutatedSeed> seeds = new ArrayList<>();

        for (Object seed : response.getPayload()) {
            seeds.add(new MutatedSeed((Seed) seed, "red"));
        }
        response.setPayload(seeds);
    }

}

class MutatedSeed extends Seed {

    private String seedColor;

    public MutatedSeed(Seed seed, String color) {
        super();
        seedColor = color;
        this.setId(seed.getId());
        this.setType(seed.getType());
    }

    public String getSeedColor() {
        return seedColor;
    }

    public void setSeedColor(String seedColor) {
        this.seedColor = seedColor;
    }
}

/*

 */
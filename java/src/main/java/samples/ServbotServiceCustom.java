package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import org.springframework.stereotype.Component;


@CustomServiceCode(serviceName = "ServbotService")
@Component
public class ServbotServiceCustom {


    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "recordChat")
    public Response recordChat(Request request) {

        System.out.println("FAT CAKES YEAH!");

       return null;
    }

}
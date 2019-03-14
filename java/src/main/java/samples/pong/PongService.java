package samples.pong;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import org.springframework.stereotype.Component;

@Component
public class PongService extends AbstractService<Request<String>, Response<String>> {
    // define the channel the service operates on,.
    public static final String Channel = "services::PongService";

    PongService() {
        super(PongService.Channel);
    }

    protected void handleServiceRequest(Request request, Message busMessage) {

        // which command shall we run?
        switch(request.getRequest()) {
            case PongRequestType.Basic:
                this.handleBasicPongRequest(request);
                break;

            case PongRequestType.Full:
                this.handleFullPongRequest(request);
                break;
        }
    }

    private void handleBasicPongRequest(Request request) {
        Response response = new Response(request.getId(), "Pong (basic)");
        this.sendResponse(response, request.getId());
    }

    private void handleFullPongRequest(Request request) {
        Response response = new Response(request.getId(), "Pong (full)");
        this.sendResponse(response, request.getId());
    }
}

abstract class PongRequestType {
    public static final String Basic = "Basic";
    public static final String Full = "Full";
}

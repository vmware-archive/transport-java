package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import samples.model.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("servbotService")
@Profile("prod")
@RestController
@RequestMapping("/servbot")
@SuppressWarnings("unchecked")
public class ServbotService extends AbstractService {

    public static String Channel = "servbot";
    private List<ChatMessage> chatMessageList;

    public ServbotService() {
        super(ServbotService.Channel);
        this.chatMessageList = new ArrayList<>();
    }

    @Override
    public void handleServiceRequest(Request request) {
        this.logDebugMessage("I AM A REQUEST " + request.getType());
        switch (request.getType()) {
            case "PostMessage":
                this.recordChat(request);
                break;
            case "MessageStats":
                this.postMessageStats(request);
                break;

        }
    }

    private Response postMessageStats(Request request) {
        Response response = new Response(request.getId(),
                Arrays.asList("Total of " + this.chatMessageList.size() + " messages"));

        // push to bus and return response (for any restful callers)
        this.sendResponse(response);
        return response;
    }

    private void recordChat(Request request) {
        ChatMessage msg = this.castPayload(ChatMessage.class, request);
        this.chatMessageList.add(msg);
    }
}

package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import samples.model.ChatCommand;
import samples.model.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("ServbotService")
@Profile("prod")
@RestController
@RequestMapping("/servbot")
@SuppressWarnings("unchecked")
public class ServbotService extends AbstractService {

    public static String Channel = "servbot";
    private List<ChatMessage> chatMessageList;

    private List<String> helpList;

    public ServbotService() {
        super(ServbotService.Channel);
        this.chatMessageList = new ArrayList<>();
        this.helpList = new ArrayList<>();
        //this.populateHelpList();
    }

    @Override
    public void handleServiceRequest(Request request) {
        switch (request.getType()) {
            case ChatCommand.PostMessage:
                this.recordChat(request);
                break;

            case ChatCommand.MessageStats:
                this.postMessageStats(request);
                break;

            case ChatCommand.Help:
                this.postHelp(request);
                break;

            default:
                this.postError(request);

        }
    }

    private void postError(Request request) {
        Response response = new Response(request.getId(),
                Arrays.asList("No such command as " + request.getType()));

        this.runCustomCodeBefore("ServbotService", "postError", request);

        // push to bus and return response (for any restful callers)
        this.sendResponse(response);
    }

    private Response postMessageStats(Request request) {

        this.runCustomCodeBefore("ServbotService", "postMessageStats", request);

        Response response = new Response(request.getId(),
                Arrays.asList("Total of " + this.chatMessageList.size() + " messages"));

        // push to bus and return response (for any restful callers)
        this.sendResponse(response);
        return response;
    }

    private Response recordChat(Request request) {
        return this.runCustomCodeAndReturnResponse(
                "ServbotService", "recordChat", request);
//
//        ChatMessage msg = this.castPayload(ChatMessage.class, request);
//        this.chatMessageList.add(msg);
    }

    private Response postHelp(Request request) {

        Response response = new Response(request.getId(),
                Arrays.asList("There is no help."));

        // push to bus and return response (for any restful callers)
        this.sendResponse(response);
        return response;
    }
}

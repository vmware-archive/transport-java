package oldsamples.servbot;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vmware.bifrost.core.AbstractService;
import oldsamples.model.ChatCommand;

import java.util.Arrays;

@Service("ServbotService")
@Profile("prod")
@RestController
@RequestMapping("/servbot")
@SuppressWarnings("unchecked")
public class BaseServbotService extends AbstractService {

    public static String Channel = "servbot";

    public BaseServbotService() {
        super(BaseServbotService.Channel);
    }


    /**
     * Bus Enabled Handling.
     *
     * @param request
     */
    @Override
    public void handleServiceRequest(Request request) {
        switch (request.getType()) {
            case ChatCommand.PostMessage:
                this.postMessage(request);
                break;

            case ChatCommand.MessageStats:
                this.messageStats(request);
                break;

            case ChatCommand.Help:
                this.help(request);
                break;

            case ChatCommand.Joke:
                this.joke(request);
                break;

            case ChatCommand.Motd:
                this.motd(request);
                break;


            default:
                this.error(request);

        }
    }

    // REST Mappings.
    @GetMapping(ChatCommand.Motd)
    Response restMotd() throws Exception {
        return this.motd(new Request(ChatCommand.Motd));
    }

    @GetMapping(ChatCommand.Help)
    Response restHelp() throws Exception {
        return this.help(new Request(ChatCommand.Help));
    }

    @GetMapping(ChatCommand.Joke)
    Response restJoke() throws Exception {
        return this.joke(new Request(ChatCommand.Joke));
    }

    @GetMapping(ChatCommand.MessageStats)
    Response restMessageStats() throws Exception {
        return this.messageStats(new Request(ChatCommand.MessageStats));
    }


    private void error(Request request) {
        Response response = new Response(request.getId(),
                Arrays.asList("No such command as " + request.getType()));

        this.runCustomCodeBefore("ServbotService", "error", request);
        this.sendResponse(response);
    }

    private Response messageStats(Request request) {

        Response response = this.runCustomCodeAndReturnResponse(
                "ServbotService", "MessageStats", request);

        this.sendResponse(response);
        return response;
    }

    private Response postMessage(Request request) {
        return this.runCustomCodeAndReturnResponse(
                "ServbotService", "PostMessage", request);

    }

    private Response help(Request request) {

        Response response = this.runCustomCodeAndReturnResponse(
                "ServbotService", "Help", request);

        this.sendResponse(response);
        return response;
    }

    private Response joke(Request request) {

        Response response = this.runCustomCodeAndReturnResponse(
                "ServbotService", "Joke", request);

        this.sendResponse(response);
        return response;
    }

    private Response motd(Request request) {

        Response response = this.runCustomCodeAndReturnResponse(
                "ServbotService", "Motd", request);

        this.sendResponse(response);
        return response;
    }
}

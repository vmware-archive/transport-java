package oldsamples.servbot;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import com.vmware.bifrost.bus.model.Message;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.vmware.bifrost.core.AbstractService;
import com.vmware.bifrost.core.util.ClassMapper;

import oldsamples.model.ChatCommand;
import oldsamples.model.ChatMessage;

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
    private HttpComponentsClientHttpRequestFactory requestFactory;

    public ServbotService() {
        super(ServbotService.Channel);
        this.chatMessageList = new ArrayList<>();
        this.helpList = new ArrayList<>();
        this.buildHelp();

        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
    }

    /**
     * Bus Enabled Handling.
     *
     * @param request
     */
    @Override
    public void handleServiceRequest(Request request, Message message) {
        switch (request.getCommand()) {
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
        Response response = new Response(request.getId(), Arrays.asList("No such command as " + request.getCommand()));
        this.sendResponse(response, request.getId());
    }

    private Response messageStats(Request request) {
        Response response = new Response(request.getId(),
                Arrays.asList("Total of " + this.chatMessageList.size() + " messages sent"));
        this.sendResponse(response, request.getId());
        return response;
    }

    private Response postMessage(Request request) {
        ChatMessage msg = ClassMapper.CastPayload(ChatMessage.class, request);
        this.chatMessageList.add(msg);

        return null;
    }

    private Response help(Request request) {
        Response response = new Response(request.getId(), this.helpList);
        this.sendResponse(response, request.getId());
        return response;
    }

    private Response joke(Request request) {
        Response response = new Response(request.getId(), Arrays.asList(getAJoke().joke));
        this.sendResponse(response, request.getId());
        return response;
    }

    private Response motd(Request request) {
        Response response = new Response(request.getId(), Arrays.asList("You should enable VMCBot. It's pretty cool."));
        this.sendResponse(response, request.getId());
        return response;
    }

    private Joke getAJoke() {
        return new RestTemplate(requestFactory).exchange("https://icanhazdadjoke.com", HttpMethod.GET, null, Joke.class)
                .getBody();
    }

    private void buildHelp() {
        this.helpList.add("/messageStats");
        this.helpList.add("/motd");
        this.helpList.add("/joke");
    }

}

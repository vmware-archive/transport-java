package oldsamples.servbot;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.core.util.ClassMapper;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.vmware.bifrost.core.interfaces.CustomServiceCode;
import com.vmware.bifrost.core.interfaces.CustomServiceCodeHandler;
import com.vmware.bifrost.core.interfaces.RunStage;
import oldsamples.model.ChatMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CustomServiceCode(serviceName = "ServbotService")
@Component
@SuppressWarnings("unchecked")
public class ServbotService {

    private List<ChatMessage> chatMessageList;
    private List<String> helpList;
    private HttpComponentsClientHttpRequestFactory requestFactory;

    ServbotService() {
        super();
        this.chatMessageList = new ArrayList<>();
        this.helpList = new ArrayList<>();
        this.buildHelp();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "PostMessage")
    public Response recordChatMessage(Request request) {

        ChatMessage msg = ClassMapper.CastPayload(ChatMessage.class, request);
        this.chatMessageList.add(msg);

       return null;
    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "MessageStats")
    public Response messageStats(Request request) {

        return new Response(request.getId(),
                Arrays.asList("Total of " + this.chatMessageList.size() + " messages sent"));

    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "Help")
    public Response getHelp(Request request) {

        return new Response(request.getId(), this.helpList);

    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "Motd")
    public Response getMotd(Request request) {

        return new Response(request.getId(),
                Arrays.asList("You should enable VMCBot. It's pretty cool."));

    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "Joke")
    public Response getAJoke(Request request) {

        return new Response(request.getId(),
                Arrays.asList(getAJoke().joke));

    }

    private Joke getAJoke() {
        return new RestTemplate(requestFactory).exchange(
                "https://icanhazdadjoke.com", HttpMethod.GET, null, Joke.class).getBody();
    }

    private void buildHelp() {
        this.helpList.add("/messageStats");
        this.helpList.add("/motd");
        this.helpList.add("/joke");
    }

}
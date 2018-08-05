package samples.servbot;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.ClassMapper;
import org.springframework.stereotype.Component;
import samples.CustomServiceCode;
import samples.CustomServiceCodeHandler;
import samples.RunStage;
import samples.model.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@CustomServiceCode(serviceName = "ServbotService")
@Component
@SuppressWarnings("unchecked")
public class ServbotService {

    private List<ChatMessage> chatMessageList;
    private List<String> helpList;

    ServbotService() {
        super();
        this.chatMessageList = new ArrayList<>();
        this.helpList = new ArrayList<>();
        this.buildHelp();
    }

    private void buildHelp() {
        this.helpList.add("/messageStats message statistics");
        this.helpList.add("/motd message of the day");
        this.helpList.add("/joke tell a joke");
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
                Arrays.asList("A total of " + this.chatMessageList.size() + " messages sent"));

    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "Help")
    public Response getHelp(Request request) {

        return new Response(request.getId(), this.helpList);

    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "Motd")
    public Response getMotd(Request request) {

        return new Response(request.getId(),
                Arrays.asList("A chicken in the bush, is worth twelve in the eye"));

    }

    @CustomServiceCodeHandler(stage = RunStage.DropIn, methodName = "Joke")
    public Response getAJoke(Request request) {

        return new Response(request.getId(),
                Arrays.asList("the fact I have not been fucking promoted yet, look at what I can do!"));

    }

}
package hello;

import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.MessageHandler;
import com.vmware.bifrost.bus.MessageResponder;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private MessagebusService bus;

    private BusTransaction responder;

    TestService() {

    }

    private void respondToRequests() {
        responder = bus.respondStream("test-response",
                (Message message) -> {
                    System.out.println("GOT A MESSAGE ON BUS: " + message.getPayload());
                    return "Cakes and balls man";
                }
        );
    }


}

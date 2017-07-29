package com.vmware.bifrost.bridge;

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostPeer;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.MessageHandler;
import com.vmware.bifrost.bus.MessageResponder;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@BifrostPeer
public class TestService implements BifrostEnabled {

    @Autowired
    private MessagebusService bus;

    private BusTransaction responder;

    TestService() {
    }

    @Override
    public void initializeSubscriptions() {
        System.out.println("Listening on bus!!!!");
        System.out.println(this.bus);

        responder = bus.respondStream("kitty",
                (Message message) -> {
                    System.out.println("GOT A MESSAGE ON BUS: " + message.getPayload());
                    return new Pop();
                }
        );
    }
}

class Pop {
    public String name = "Chip";
}
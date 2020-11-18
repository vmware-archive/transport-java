package samples;

import com.vmware.transport.bus.model.Message;
import com.vmware.transport.core.AbstractBase;
import org.springframework.stereotype.Component;

/*
 * Copyright(c) VMware Inc. 2019
 */
@Component
public class HelloWorld extends AbstractBase {

    // define your service
    private String myChannel;

    // initialize is the only mandatory method required. This will always run on boot.
    public void initialize() {

        // create a local channel.
        myChannel = "myChannel";

        // create a responder
        this.createResponder();
        this.sendRequest();
    }

    // send a response to a request.
    private void createResponder() {
        this.bus.respondOnce(myChannel,
                (Message msg) -> msg.getPayload().toString() + " world"
        );
    }

    // send a request
    private void sendRequest() {
        this.bus.requestOnce(myChannel, "hello",
                (Message msg) -> {
                    this.logInfoMessage("HelloWorld:", "Got a response! ", msg.getPayload().toString());
                }
        );
    }
}

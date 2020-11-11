/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */

package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractBase;
import com.vmware.bifrost.core.interfaces.BusServiceEnabled;
import org.springframework.stereotype.Component;

import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@BifrostService
@Component
public class SimpleStreamTicker extends AbstractBase implements BusServiceEnabled {

    private ScheduledExecutorService executorService;
    private String channelName = "simple-stream";
    private boolean online = false;

    SimpleStreamTicker() {
        this.executorService = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void initialize() {
        // ensure we open up the channel locally and keep it open, even it we're offline.
        bus.getApi().getChannelObject(channelName, "sample-stream-demo");

        this.online();

        // listen for any messages coming in and decide what to do with them.
        bus.listenRequestStream(channelName,
                (Message msg) -> {

                    // if the msg is 'offline or online' then switch this service online or offline.
                    String command = ((Request) msg.getPayload()).getRequest();
                    switch (command) {
                        case "online":
                            online();
                            break;
                        case "offline":
                            offline();
                            break;
                    }
                }
        );


        // create a runnable task that sends a message every 300ms with random values.
        Runnable runnableTask = () -> {

            // this is what we want to send, a simple string, with somewhat random data.
            String responseString = "ping-" +
                    GregorianCalendar.getInstance().get(GregorianCalendar.MILLISECOND) +
                    GregorianCalendar.getInstance().get(GregorianCalendar.SECOND);

            // create our response.
            Response<String> response = new Response<>(UUID.randomUUID(), responseString);

            // send response.
            if (this.online) {
                bus.sendResponseMessage(channelName, response);
            }
        };

        // loop every 300ms, sending the same message over and over,
        executorService.scheduleAtFixedRate(runnableTask, 1000, 300, TimeUnit.MILLISECONDS);
    }

    // listen for requests.
    public void online() {
        this.online = true;
    }

    // stop listening for requests.
    public void offline() {
        this.online = false;
    }
}

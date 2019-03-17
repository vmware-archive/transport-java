/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */

package samples;

import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.core.AbstractBase;
import org.springframework.stereotype.Component;

import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@BifrostService
@Component
public class SimpleStreamTicker extends AbstractBase {

    private ScheduledExecutorService executorService;

    SimpleStreamTicker() {
        this.executorService = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void initialize() {
        // ensure we open up the channel locally.
        bus.getApi().getChannelObject("simple-stream", this.getName());

        // create a runnable task that sends a message every 300ms with random values.
        Runnable runnableTask = () -> {

            // this is what we want to send, a simple string, with somewhat random data.
            String responseString = "ping-" +
                    GregorianCalendar.getInstance().get(GregorianCalendar.MILLISECOND) +
                    GregorianCalendar.getInstance().get(GregorianCalendar.SECOND);

            // create our response.
            Response<String> response = new Response<>(UUID.randomUUID(), responseString);

            // send response.
            bus.sendResponseMessage("simple-stream", response);
        };
        // loop every 300ms, sending the same message over and over,
        executorService.scheduleAtFixedRate(runnableTask, 1000, 300, TimeUnit.MILLISECONDS);
    }
}

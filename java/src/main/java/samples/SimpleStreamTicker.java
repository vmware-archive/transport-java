/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */

package samples;

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.core.util.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@BifrostService
@Component
public class SimpleStreamTicker extends Loggable implements BifrostEnabled {

    private ScheduledExecutorService executorService;
    private EventBus bus;

    @Autowired
    SimpleStreamTicker(EventBus bus) {
        super();
        this.bus = bus;
        this.executorService = Executors.newScheduledThreadPool(5);
    }

    @Override
    public void initializeSubscriptions() {

        Runnable runnableTask = () -> {
            String msg = "{\"payload\": \"ping-" + GregorianCalendar.getInstance().get(GregorianCalendar.SECOND) + "\"}";
            this.bus.sendResponseMessage("simple-stream", msg);
        };
        executorService.scheduleAtFixedRate(runnableTask, 1, 5, TimeUnit.SECONDS);
    }
}

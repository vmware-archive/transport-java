package oldsamples.servbot;


import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.MessagebusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import oldsamples.model.Metrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Profile("prod")
@SuppressWarnings("unchecked")
@BifrostService
@Service("SampleStreamService")
public class SampleStreamService implements BifrostEnabled {

    @Autowired
    private MessagebusService bus;

    ScheduledExecutorService scheduledExecutor;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static String Channel = "sample-stream";

    public SampleStreamService() {

        this.scheduledExecutor = Executors.newScheduledThreadPool(3);
    }

    @Override
    public void initializeSubscriptions() {
        logger.info("Initializing SampleStream Service");

        scheduledExecutor.scheduleAtFixedRate(
                new Metrics(bus, SampleStreamService.Channel),
                1000,
                1000,
                TimeUnit.MILLISECONDS);


    }

}

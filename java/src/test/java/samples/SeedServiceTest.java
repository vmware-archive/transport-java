package samples;


import com.vmware.bifrost.bridge.util.AbstractTest;
import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import io.reactivex.Observable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import samples.model.SeedRequest;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class SeedServiceTest extends AbstractTest {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MessagebusService bus;

    @Autowired
    protected SeedService seedService;

    @Test
    public void checkDI() {
        Assert.assertNotNull(seedService);
        Assert.assertNotNull(seedService.getServiceChannel());
        Assert.assertNotNull(bus);
    }

    @Test
    public void sendSomething() {


        SeedRequest request = new SeedRequest(SeedRequest.Type.GetSeeds);
        //Subject<Message> monitorStream = bus.getMonitor();

        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        stream.subscribe(
                (Message msg) -> {
                    this.logTestMessage("got a response", msg.toString());
                }
        );

        this.logTestMessage("sending request to", seedService.getServiceChannel());
        bus.sendRequest(seedService.getServiceChannel(), request);




        //TestObserver<Message> observer = monitorStream.test();
        //observer.assertSubscribed();

    }

}

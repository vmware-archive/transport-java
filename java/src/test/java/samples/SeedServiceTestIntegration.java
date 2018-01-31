package samples;

import com.vmware.bifrost.bridge.util.AbstractTest;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import samples.model.SeedRequest;
import samples.model.SeedResponse;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest()
@ActiveProfiles("prod")
//@Ignore
public class SeedServiceTestIntegration extends AbstractTest {

    @Autowired
    protected MessagebusService bus;

    @Autowired
    protected SeedService seedService;

    @Before
    public void setUp() {
        seedService.mockFail = false;
    }

    @Test
    public void testGetSeeds() {

        SeedRequest request = new SeedRequest(SeedRequest.Type.GetSeeds);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        observer.assertNoErrors();
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getUuid(), response.getUuid());
        Assert.assertFalse(response.isError());
        Assert.assertNotNull(response.getPayload());
        Assert.assertEquals(2, response.getPayload().size());
    }

    @Test
    public void testGetSeedsError() {

        SeedRequest request = new SeedRequest(SeedRequest.Type.GetSeeds);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        seedService.mockFail = true;
        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getUuid(), response.getUuid());
        Assert.assertTrue(response.isError());
        Assert.assertNull(response.getPayload());

    }

    @Test
    public void testPlantSeed() {

        SeedRequest request = new SeedRequest(SeedRequest.Type.PlantSeed);

        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        observer.assertNoErrors();
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getUuid(), response.getUuid());
        Assert.assertFalse(response.isError());
        Assert.assertNotNull(response.getPayload());
        Assert.assertEquals(1, response.getPayload().size());
    }

    @Test
    public void testPlantSeedError() {

        SeedRequest request = new SeedRequest(SeedRequest.Type.PlantSeed);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        seedService.mockFail = true;
        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getUuid(), response.getUuid());
        Assert.assertTrue(response.isError());
        Assert.assertNull(response.getPayload());

    }

    @Test
    public void testKillPlant() {

        SeedRequest request = new SeedRequest(SeedRequest.Type.KillPlant);

        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        observer.assertNoErrors();
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getUuid(), response.getUuid());
        Assert.assertFalse(response.isError());
        Assert.assertNull(response.getPayload());
    }

    @Test
    public void testKillPlantError() {

        SeedRequest request = new SeedRequest(SeedRequest.Type.KillPlant);

        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        seedService.mockFail = true;
        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getUuid(), response.getUuid());
        Assert.assertTrue(response.isError());
        Assert.assertNull(response.getPayload());

    }

}

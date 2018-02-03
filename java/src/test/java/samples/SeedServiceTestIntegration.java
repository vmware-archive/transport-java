package samples;

import com.vmware.bifrost.bridge.util.AbstractTest;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.swagger.client.model.Seed;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import samples.model.SeedRequest;
import samples.model.SeedResponse;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//@RunWith(SpringRunner.class)
//@SpringBootTest()
//@ActiveProfiles("test")
@Ignore
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

        SeedRequest request
                = new SeedRequest(1, UUID.randomUUID(), "GetSeeds", null);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        observer.assertNoErrors();
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getId(), response.getId());
        Assert.assertFalse(response.isError());
        Assert.assertNotNull(response.getPayload());
        Assert.assertEquals(2, response.getPayload().size());
    }

    @Test
    public void testGetSeedsError() {

        SeedRequest request
                = new SeedRequest(1, UUID.randomUUID(), "GetSeeds", null);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        seedService.mockFail = true;
        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getId(), response.getId());
        Assert.assertTrue(response.isError());
        Assert.assertNull(response.getPayload());

    }

    @Test
    public void testPlantSeed() {
        Seed seed = new Seed();
        seed.setType(Seed.TypeEnum.FLOWER);

        SeedRequest request
                = new SeedRequest(1, UUID.randomUUID(), "PlantSeed", seed);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        observer.assertNoErrors();
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getId(), response.getId());
        Assert.assertFalse(response.isError());
        Assert.assertNotNull(response.getPayload());
        Assert.assertEquals(1, response.getPayload().size());
    }

    @Test
    public void testPlantSeedError() {

        SeedRequest request
                = new SeedRequest(1, UUID.randomUUID(), "PlantSeed", null);


        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        seedService.mockFail = true;
        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getId(), response.getId());
        Assert.assertTrue(response.isError());
        Assert.assertNull(response.getPayload());

    }

    @Test
    public void testKillPlant() {

        Seed seed = new Seed();
        seed.setType(Seed.TypeEnum.FLOWER);

        SeedRequest request
                = new SeedRequest(1, UUID.randomUUID(), "KillPlant", seed);

        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        observer.assertNoErrors();
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getId(), response.getId());
        Assert.assertFalse(response.isError());
        Assert.assertNull(response.getPayload());
    }

    @Test
    public void testKillPlantError() {

        SeedRequest request
                = new SeedRequest(1, UUID.randomUUID(), "KillPlant", null);

        Observable<Message> stream = this.bus.getResponseChannel(seedService.getServiceChannel(), seedService.getName());
        TestObserver<Message> observer = new TestObserver<>();

        stream.subscribeOn(Schedulers.computation())
                .subscribe(observer);

        seedService.mockFail = true;
        bus.sendRequest(seedService.getServiceChannel(), request);

        observer.awaitTerminalEvent(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, observer.valueCount());
        SeedResponse response = (SeedResponse) observer.values().get(0).getPayload();
        Assert.assertEquals(request.getId(), response.getId());
        Assert.assertTrue(response.isError());
        Assert.assertNull(response.getPayload());

    }

}

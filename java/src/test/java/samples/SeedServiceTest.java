package samples;

import com.vmware.bifrost.bridge.util.AbstractTest;
import com.vmware.bifrost.bus.MessagebusService;
import io.swagger.client.ApiException;
import io.swagger.client.api.SeedApi;
import io.swagger.client.model.Seed;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.core.io.ResourceLoader;
import samples.model.SeedRequest;

import static org.mockito.Mockito.*;

//@RunWith(MockitoJUnitRunner.class)
@Ignore
public class SeedServiceTest extends AbstractTest {

    @Mock
    MessagebusService bus;

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    SeedApi seedApi;

    @InjectMocks
    private SeedService seedService;
    private SeedService seedServiceSpy;
    private Seed seed;


    @Before
    public void createSeed() {
        seed = new Seed();
        seed.setType(Seed.TypeEnum.FLOWER);
        seedServiceSpy = spy(seedService);
    }

    @Test
    public void testGetSeeds() throws ApiException {

        seedService.handleServiceRequest(new SeedRequest(SeedRequest.Type.GetSeeds));
        verify(seedApi).getSeeds();
    }

    @Test
    public void testGetSeedsError() throws ApiException {

        doThrow(new ApiException("testGetSeedsError() API Failure")).when(seedApi).getSeeds();
        seedServiceSpy.handleServiceRequest(new SeedRequest(SeedRequest.Type.GetSeeds));
        verify(seedServiceSpy, atLeastOnce()).apiFailedHandler(any(), any(), any());

    }

    @Test
    public void testPlantSeed() throws ApiException {

        seedService.handleServiceRequest(new SeedRequest(SeedRequest.Type.PlantSeed, seed));
        verify(seedApi).plantSeed(seed);
    }

    @Test
    public void testPlantSeedError() throws ApiException {

        doThrow(new ApiException("testPlantSeedError() API Failure")).when(seedApi).plantSeed(seed);
        seedServiceSpy.handleServiceRequest(new SeedRequest(SeedRequest.Type.PlantSeed, seed));
        verify(seedServiceSpy, atLeastOnce()).apiFailedHandler(any(), any(), any());
    }

    @Test
    public void testKillPlant() throws ApiException {

        seedService.handleServiceRequest(new SeedRequest(SeedRequest.Type.KillPlant, seed));
        verify(seedApi).killPlant(seed);
    }

    @Test
    public void testKillPlantError() throws ApiException {

        doThrow(new ApiException("testKillPlantError() API Failure")).when(seedApi).killPlant(seed);
        seedServiceSpy.handleServiceRequest(new SeedRequest(SeedRequest.Type.KillPlant, seed));
        verify(seedServiceSpy, atLeastOnce()).apiFailedHandler(any(), any(), any());
    }
}
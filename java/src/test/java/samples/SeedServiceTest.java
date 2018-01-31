package samples;

import com.vmware.bifrost.bridge.util.AbstractTest;
import com.vmware.bifrost.bus.MessagebusService;
import io.swagger.client.api.SeedApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.core.io.ResourceLoader;
import samples.model.SeedRequest;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SeedServiceTest extends AbstractTest {

    @Mock
    MessagebusService bus;

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    SeedApi seedApi;

    @InjectMocks
    private SeedService seedService;

    @Test
    public void testGetSeeds() throws Exception {

        SeedRequest request = new SeedRequest(SeedRequest.Type.GetSeeds);

        this.logTestMessage("starting testing things");

        seedService.handleServiceRequest(request);
        verify(seedApi).getSeeds();
    }
}
package samples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

public interface Mockable {

    default Resource loadResources(ResourceLoader loader) {
        return loader.getResource("classpath:db.json");
    }

    default <T> T getModels(Class<T> clazz, ObjectMapper mapper, Resource res) throws IOException {
        return mapper.readValue(res.getFile(), clazz);
    }
}

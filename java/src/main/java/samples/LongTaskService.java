package samples;

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import samples.model.Task;

@Component
@BifrostService
public class LongTaskService implements BifrostEnabled {

    @Autowired
    private MessagebusService bus;

    ExecutorService executorService;

    private int taskCount = 0;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    LongTaskService() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void initializeSubscriptions() {

        logger.info("Initializing LongTaskService");

        bus.respondStream("task-a",
                (Message message) -> {

                    Task task = new Task(taskCount, bus, 100, "task-a");
                    executorService.submit(task);
                    this.taskCount++;
                    return task;
                }
        );

        bus.respondStream("task-b",
                (Message message) -> {

                    Task task = new Task(taskCount, bus, 500, "task-b");
                    executorService.submit(task);
                    this.taskCount++;
                    return task;
                }
        );
    }
}


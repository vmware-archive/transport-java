package oldsamples.model;

import com.vmware.bifrost.bus.MessagebusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task implements Runnable {
    private int taskId;
    private TaskStatus taskStatus = TaskStatus.NotStarted;
    private String category;
    private String task;
    private int sleep;
    private String channel;

    private int completedState = 0;
    MessagebusService bus;

    public Task(int id, MessagebusService bus, int sleep, String channel) {
        this.taskId = id;
        this.bus = bus;
        this.sleep = sleep;
        this.channel = channel;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getCategory() {
        return category;
    }

    public String getTask() {
        return task;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public int getCompletedState() {
        return completedState;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run() {
        try {

            this.taskStatus = TaskStatus.Running;
            this.task = "Initializing";
            this.category = "Environment";
            for(int x = 0; x < 100; x++) {
                Thread.sleep(this.sleep);
                this.completedState++;
                this.updateLabels();
                bus.sendResponse(this.channel, this);

            }
            this.taskStatus = TaskStatus.Finished;
            bus.sendResponse(this.channel, this);

        } catch (InterruptedException exp) {

        }
    }

    private void updateLabels() {

        switch(this.completedState) {
            case 10:
                this.task = "Preparing";
                break;
            case 15:
                this.task = "Unpacking";
                break;
            case 20:
                this.task = "Installing";
                break;
            case 25:
                this.category = "Networking";
                this.task = "Configuring";
                break;
            case 30:
                this.task = "Testing DHCP";
                break;
            case 35:
                this.task = "Testing IPv4";
                break;
            case 40:
                this.task = "Testing IPv6";
                break;
            case 45:
                this.task = "Applying Changes";
                break;
            case 50:
                this.category = "Virtualization";
                this.task = "Unpacking Appliances";
                break;
            case 55:
                this.task = "Installing Appliances";
                break;
            case 60:
                this.task = "Configuring Appliances";
                break;
            case 68:
                this.task = "Applying Networking Settings";
                break;
            case 70:
                this.task = "Cleaning up";
                break;
            case 80:
                this.category = "Finalizing";
                this.task = "Deleting Temp Files";
                break;
            case 90:
                this.task = "Removing Cache";
                break;
            case 93:
                this.task = "Disconnecting Magic";
                break;
            case 98:
                this.task = "Opening Champagne";
                break;
            case 100:
                this.category = "Completed";
                this.task = "Completed";
                break;

        }
    }

}

enum TaskStatus {
    NotStarted,
    Running,
    Finished
}

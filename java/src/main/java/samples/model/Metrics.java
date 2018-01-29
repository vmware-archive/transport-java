package samples.model;

import com.vmware.bifrost.bridge.util.OpenSimplexNoise;
import com.vmware.bifrost.bus.MessagebusService;

public class Metrics implements Runnable {

    private MessagebusService bus;
    private int interval;
    private String channel;

    OpenSimplexNoise noise;
    double x,y;

    public Metrics(MessagebusService bus, int interval, String channel) {
        this.bus = bus;
        this.interval = interval;
        this.channel = channel;

        noise = new OpenSimplexNoise();
        x = 0;
        y = 0;

    }

    private double generateValue() {
        double newX = noise.eval(x, y);
        double newY = noise.eval(x, y);
        x = ++x + newX;
        y = ++y + newY;
        return newX;
    }

    @Override
    public void run() {

        try {

            while(true) {
                Thread.sleep(this.interval);
                bus.sendResponse(this.channel, new SampleMetric(this.generateValue()));
            }

        } catch (InterruptedException exp) {

        }
    }
}

class SampleMetric {
    private Double value;

    SampleMetric(Double val) {
        value = val;
    }

    public Double getValue() {
        return value;
    }

}

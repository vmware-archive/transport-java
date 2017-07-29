package com.vmware.bifrost.bridge;

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostPeer;
import com.vmware.bifrost.bridge.util.OpenSimplexNoise;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.MessageHandler;
import com.vmware.bifrost.bus.MessageResponder;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@BifrostPeer
public class TestService implements BifrostEnabled {

    @Autowired
    private MessagebusService bus;

    private BusTransaction responder;

    OpenSimplexNoise noise;
    double x,y;

    TestService() {
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
    public void initializeSubscriptions() {
        responder = bus.respondStream("metrics",
                (Message message) -> {
                    return new SampleMetric(this.generateValue());
                }
        );
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
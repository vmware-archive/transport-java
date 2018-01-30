package samples;

/*
 * Copyright(c) VMware Inc. 2017
 */

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import samples.model.*;

import java.util.UUID;


@Component
@BifrostService
public class TheGarden implements BifrostEnabled {

    private Flower[] gardenFlowers;
    private int numFlowers = 5;

    @Autowired
    MessagebusService bus;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    TheGarden() {
        gardenFlowers = new Flower[numFlowers];
    }

    @Override
    public void initializeSubscriptions() {

        bus.respondStream("garden-state",
                (Message message) -> gardenFlowers
        );

        bus.respondStream("garden-action",
                (Message message) -> {
                    try {
                        GardenAction action = (GardenAction) message.getPayload();

                        switch (action.getType()) {
                            case Plant:
                                return new GardenResponse(this.plantFlower());

                            case Water:
                                return new GardenResponse(this.waterFlower(action));

                            default:
                                return new GardenResponse(true, "Invalid action (" + action.getType());

                        }

                    } catch (ClassCastException exp) {

                        // I can't deal with this request.
                        logger.info("Garden Action Dropped, Invalid Message");
                        return new GardenResponse(true, "request ignored");

                    } catch (GardenFullException gfe) {

                        // The garden is full, can't plant!
                        logger.info("Garden Plant Request Failed, Garden Full!");
                        return new GardenResponse(true, "Garden is full");

                    } catch (FlowerNotFoundException fnf) {

                        // Flower not found, can't update,.
                        logger.info("Flower not found, cannot process.");
                        return new GardenResponse(true, "Flower not found");

                    }

                }
        );

    }

    private Flower findFlower(UUID id) throws FlowerNotFoundException {
        for (Flower flower : gardenFlowers) {
            if (flower.getId().equals(id)) {
                return flower;
            }
        }
        throw new FlowerNotFoundException("No flower found with ID: " + id.toString());
    }

    private Flower plantFlower() throws GardenFullException {
        if (this.isGardenFull()) {
            throw new GardenFullException("no more room for lovely flowers");
        }

        Flower newFlower = new Flower();
        int emptyFlowerSpot = this.findEmptyFlowerSpot();
        gardenFlowers[emptyFlowerSpot] = newFlower;
        return newFlower;
    }

    private Flower waterFlower(GardenAction action) throws FlowerNotFoundException {
        // TODO: This is where the garden sends a unit of work to the UI to churn,
        // the garden will only allow the flower to be watered once the work is completed.

        Flower flower = findFlower(action.getFlowerId());
        flower.setGrowthPercentage(action.getGrowPercentage());
        return flower;
    }


    private boolean isGardenFull() {
        int emptySpaces = 0;
        for (int x = 0; x < gardenFlowers.length; x++) {
            if (gardenFlowers[x] == null) {
                emptySpaces++;
            }
        }

        if (emptySpaces > 0) {
            return false;
        }
        return true;
    }

    private int findEmptyFlowerSpot() throws GardenFullException {
        for (int x = 0; x < gardenFlowers.length; x++) {
            if (gardenFlowers[x] == null) {
                return x;
            }
        }
        throw new GardenFullException("garden filled up before we could find a spot");
    }
}

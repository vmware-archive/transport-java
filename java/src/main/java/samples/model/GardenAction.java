package samples.model;

import java.util.UUID;

/*
 * Copyright(c) VMware Inc. 2017
 */


public class GardenAction {
    private GardenActionType type;
    private UUID flowerId;
    private Integer growPercentage;

    public GardenAction() {

    }

    public GardenAction(GardenActionType type) {
        this.type = type;
    }

    public GardenAction(GardenActionType type, UUID id) {
        this(type);
        flowerId = id;
    }

    public GardenActionType getType() {
        return type;
    }


    public UUID getFlowerId() {
        return flowerId;
    }

    public Integer getGrowPercentage() {
        return growPercentage;
    }
}

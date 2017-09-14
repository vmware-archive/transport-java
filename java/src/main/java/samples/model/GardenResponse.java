package samples.model;

/*
 * Copyright(c) VMware Inc. 2017
 */
public class GardenResponse {
    private boolean error;
    private Flower flower;
    private String message;

    public GardenResponse() {
        error = false;
    }

    public GardenResponse(boolean error, String message) {
        this();
        this.message = message;
    }

    public GardenResponse(Flower flower) {
        this();
        this.flower = flower;
    }

    public boolean isError() {
        return error;
    }

    public Flower getFlower() {
        return flower;
    }

    public String getMessage() {
        return message;
    }
}

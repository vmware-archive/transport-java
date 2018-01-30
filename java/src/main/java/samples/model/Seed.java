package samples.model;

import java.util.Date;

public class Seed {

    enum Type {
        Tree,
        Flower,
        Bush
    }

    private Type type;
    private Date planted;

    public Seed(Type type) {
        this.type = type;
        this.planted = new Date();
    }

    public Type getType() {
        return type;
    }

    public long getPlanted() {
        return planted.getTime();
    }
}

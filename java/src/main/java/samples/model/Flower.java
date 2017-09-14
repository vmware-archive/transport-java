package samples.model;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/*
 * Copyright(c) VMware Inc. 2017
 */
public class Flower {
    private UUID id;
    private Boolean growing;
    private Integer growthPercentage;
    private Date planted;
    private Integer lifeExpectancy;

    public Flower() {
        id = UUID.randomUUID();
        planted = new Date();
        growthPercentage = 0;
        lifeExpectancy = 120;
    }

    public UUID getId() {
        return id;
    }

    public Boolean getGrowing() {
        return growing;
    }

    public Integer getGrowthPercentage() {
        return growthPercentage;
    }

    public Date getPlanted() {
        return planted;
    }

    public Integer getLifeExpectancy() {
        return lifeExpectancy;
    }

    public void setGrowthPercentage(Integer percent) {
        growthPercentage = percent;
    }
}

package samples.rest;

import lombok.Getter;
import lombok.Setter;

/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */
public class CloudServicesPage {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String url;

    @Getter @Setter
    private String time_zone;

    @Getter @Setter
    private String updated_at;
}
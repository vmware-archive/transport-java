package samples.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */
public class CloudServicesStatusResponse {

    @Getter @Setter
    private CloudServicesStatus status;

    @Getter @Setter
    private CloudServicesPage page;

}

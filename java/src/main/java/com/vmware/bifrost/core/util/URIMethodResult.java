/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class URIMethodResult<Req, Resp> {

    @Getter @Setter
    private Map<String, String> queryString;

    @Getter @Setter
    private List<String> pathItems;

    @Getter @Setter
    private List<String> methodArgNames;

    @Getter @Setter
    private List<Class> methodAnnotations;

    @Getter @Setter
    private Req payloadType;

    @Getter @Setter
    private Resp responseType;


}

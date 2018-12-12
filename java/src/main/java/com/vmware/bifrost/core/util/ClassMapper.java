/*
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.Request;

public class ClassMapper {

    public static <T> T CastPayload(Class clazz, Request request) throws ClassCastException, IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.convertValue(request.getPayload(), clazz);
    }
}

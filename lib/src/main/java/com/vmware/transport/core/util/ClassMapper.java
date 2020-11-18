/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.MessageHeaders;

import java.util.LinkedHashMap;

@SuppressWarnings("unchecked")
public class ClassMapper {

    public static <T> T CastPayload(Class clazz, Request request) throws ClassCastException, IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.convertValue(request.getPayload(), clazz);
    }

    public static <T> T CastPayload(Class clazz, Response response) throws ClassCastException, IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.convertValue(response.getPayload(), clazz);
    }

    public static MessageHeaders CastMessageHeaders(Object msgHeadersObject) {
        if (msgHeadersObject == null) {
            return null;
        }
        LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap) msgHeadersObject;
        MessageHeaders messageHeaders = MessageHeaders.newInstance();
        for (String key : linkedHashMap.keySet()) {
            messageHeaders.setHeader(key, linkedHashMap.get(key));
        }
        return messageHeaders;
    }
}

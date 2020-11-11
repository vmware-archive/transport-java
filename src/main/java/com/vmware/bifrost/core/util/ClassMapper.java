/*
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.MessageHeaders;

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

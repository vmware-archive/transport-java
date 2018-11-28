/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class URIMethodResult {

    @Getter @Setter
    private Map<String, String> queryString;

    @Getter @Setter
    private Map<String, String> pathItemMap;

    @Getter @Setter
    private List<String> pathItems;

    @Getter @Setter
    private Map<String, Class> methodArgs;

    @Getter @Setter
    private Map<String, Class> methodAnnotationTypes;

    @Getter @Setter
    private Map<String, Object> methodAnnotationValues;

    @Getter @Setter
    private Method method;

    @Getter @Setter
    private Object controller;

    @Getter @Setter
    private Class payloadType;

    @Getter @Setter
    private Class responseType;


}

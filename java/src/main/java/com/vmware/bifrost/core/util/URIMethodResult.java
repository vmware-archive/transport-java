/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class URIMethodResult {

    @Getter @Setter
    private Map<String, Object> queryString;

    @Getter @Setter
    private Map<String, Object> pathItemMap;

    @Getter @Setter
    private List<String> pathItems;

    @Getter @Setter
    private List<String> methogArgList;

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


    public String getRequestParamArgumentName() {

        if(this.methodArgs == null)
            return null;

        if(this.methodAnnotationTypes == null)
            return null;

        String argName = null;
        for(String key: this.methodAnnotationTypes.keySet() ){
            if(this.methodAnnotationTypes.get(key).equals(RequestParam.class)) {
                argName = key;
            }
        }
        return argName;
    }

    public String getRequestBodyArgumentName() {

        if(this.methodArgs == null)
            return null;

        if(this.methodAnnotationTypes == null)
            return null;

        String argName = null;
        for(String key: this.methodAnnotationTypes.keySet() ){
            if(this.methodAnnotationTypes.get(key).equals(ResponseBody.class)) {
                argName = key;
            }
        }
        return argName;
    }

}

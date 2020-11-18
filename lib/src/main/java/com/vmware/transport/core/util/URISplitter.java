/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.*;

/**
 * URI Splitter breaks up a URI by path, and extracts query params into a Map.
 */
public class URISplitter {

    /**
     * Split a URI path into an array (without slashes)
     *
     * @param uri The URI to be looked at
     * @return List of all the path items in a URI.
     */
    static List<String> split(URI uri) {
        List<String> pathItems = Arrays.asList(uri.getRawPath().split("/"));
        return cleanPathItems(pathItems);
    }

    /**
     * Split a path (Sting value) into an array, without slashes.
     *
     * @param uri the URI to be looked at (as a string)
     * @return List of all the path items in a URI
     */
    public static List<String> split(String uri) {
        List<String> pathItems = Arrays.asList(uri.split("/"));
        return cleanPathItems(pathItems);
    }

    /**
     * Extract query parameters from a URI into a map.
     * @param uri The URI to be looked at
     * @param methodArgs Map of method argument names and types
     * @return Map of query names and the values (String, UUID, Integer)
     */
    public static Map<String, Object> extractQueryParams(URI uri, Map<String, Class> methodArgs) {
        if (uri.getRawQuery() != null) {
            List<String> queryPairs = Arrays.asList(uri.getRawQuery().split("&"));
            Map<String, Object> mappedQueryPairs = new HashMap<>();

            for (String val : queryPairs) {
                if (!val.isEmpty()) {
                    String[] keyVal = val.split("=");
                    String valueString = keyVal[1];
                    Object value = null;

                    try {

                        // is value numeric?
                        if (StringUtils.isNumeric(valueString) && methodArgs.get(keyVal[0]).equals(Integer.class)) {
                            value = Integer.parseInt(valueString);
                        }

                        // is value a UUID?
                        if(value == null) {
                            UUID uuid = UUID.fromString(valueString);
                            if(methodArgs.get(keyVal[0]).equals(UUID.class)) {
                                value = uuid;
                            }
                        }

                    } catch(Exception exp) {
                        value = valueString;
                    }

                    if(value == null) {
                        value = valueString;
                    }

                    mappedQueryPairs.put(keyVal[0], value);
                }
            }
            return mappedQueryPairs;
        }
        return null;
    }

    private static List<String> cleanPathItems(List<String> pathItems) {
        List<String> cleanedItems = new ArrayList<>();

        for (String val : pathItems) {
            if (!val.isEmpty()) {
                cleanedItems.add(val);
            }
        }
        return cleanedItems;
    }
}

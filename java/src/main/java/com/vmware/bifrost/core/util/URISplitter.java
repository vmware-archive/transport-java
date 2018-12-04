/**
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.util;

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
     * @param uri
     * @return
     */
    public static List<String> split(URI uri) {
        List<String> pathItems = Arrays.asList(uri.getRawPath().split("/"));
        List<String> cleanedItems = new ArrayList<>();

        for (String val : pathItems) {
            if (!val.isEmpty()) {
                cleanedItems.add(val);
            }
        }
        return cleanedItems;
    }

    /**
     * Split a path (Sting value) into an array, without slashes.
     *
     * @param uri
     * @return
     */
    public static List<String> split(String uri) {
        List<String> pathItems = Arrays.asList(uri.split("/"));
        List<String> cleanedItems = new ArrayList<>();

        for (String val : pathItems) {
            if (!val.isEmpty()) {
                cleanedItems.add(val);
            }
        }
        return cleanedItems;
    }

    /**
     * Extract query parameters from a URI into a map.
     *
     * @param uri
     * @return
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
                            if(uuid != null && methodArgs.get(keyVal[0]).equals(UUID.class)) {
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
}

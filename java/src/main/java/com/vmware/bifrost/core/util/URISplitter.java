/**
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.util;

import java.net.URI;
import java.util.*;

/**
 * URI Splitter breaks up a URI by path, and extracts query params into a Map.
 */
public class URISplitter {

    /**
     * Split a URI path into an array (without slashes)
     * @param uri
     * @return
     */
    public static List<String> split(URI uri) {
        List<String> pathItems =  Arrays.asList(uri.getRawPath().split("/"));
        List<String> cleanedItems = new ArrayList<>();

        for(String val: pathItems) {
            if (!val.isEmpty()) {
                cleanedItems.add(val);
            }
        }
        return cleanedItems;
    }

    /**
     * Split a path (Sting value) into an array, without slashes.
     * @param uri
     * @return
     */
    public static List<String> split(String uri) {
        List<String> pathItems =  Arrays.asList(uri.split("/"));
        List<String> cleanedItems = new ArrayList<>();

        for(String val: pathItems) {
            if (!val.isEmpty()) {
                cleanedItems.add(val);
            }
        }
        return cleanedItems;
    }


    /**
     * Extract query parameters from a URI into a map.
     * @param uri
     * @return
     */
    public static Map<String, String> extractQueryParams(URI uri) {
        if (uri.getRawQuery() != null) {
            List<String> queryPairs = Arrays.asList(uri.getRawQuery().split("&"));
            Map<String, String> mappedQueryPairs = new HashMap<>();

            for (String val : queryPairs) {
                if (!val.isEmpty()) {
                    String[] keyVal = val.split("=");
                    mappedQueryPairs.put(keyVal[0], keyVal[1]);
                }
            }
            return mappedQueryPairs;
        }
        return null;
    }
}

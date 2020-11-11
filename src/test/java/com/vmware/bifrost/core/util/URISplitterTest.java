package com.vmware.bifrost.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class URISplitterTest {

    @Test
    public void testSplitSimple() throws Exception {

        URI uri = new URI("/some/path/here");

        List<String> result = URISplitter.split(uri);

        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals("some", result.get(0));
        Assert.assertEquals("path", result.get(1));
        Assert.assertEquals("here", result.get(2));

    }

    @Test
    public void testSplitComplex() throws Exception {

        URI uri = new URI("http://some.domain/some/path/here/longer?query=val");

        List<String> result = URISplitter.split(uri);

        Assert.assertEquals(result.size(), 4);
        Assert.assertEquals("some", result.get(0));
        Assert.assertEquals("path", result.get(1));
        Assert.assertEquals("here", result.get(2));
        Assert.assertEquals("longer", result.get(3));

    }

    @Test
    public void testExtractQueryString() throws Exception {

        URI uri = new URI("/some/path/here/longer?query=val&something=nothing");

        Map<String, Object> queryMap = URISplitter.extractQueryParams(uri, null);

        Assert.assertEquals(2, queryMap.size());
        Assert.assertEquals("val", queryMap.get("query"));
        Assert.assertEquals("nothing", queryMap.get("something"));

    }

}

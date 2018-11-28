/**
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.operations;

import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController(value = "MockRestController")
public class MockRestController {


    @RequestMapping(value = "/foo/{bar}", method = GET)
    @ResponseBody
    public String simpleGetPath(@PathVariable String baz,
                                @RequestParam(value = "boz", required = false) String bozQuery) {
        return "FooBarSimple-" + baz + bozQuery;
    }

    @RequestMapping(value = "/foo/{bar}/bar/{orgId}", method = GET)
    @ResponseBody
    public String normalGetPath(@PathVariable String baz,
                                @PathVariable String orgId,
                                @RequestParam(value = "boz", required = false) String bozQuery) {
        return "FooBarNormal-" + baz + orgId + bozQuery;
    }

    @RequestMapping(value = "/foo", method = GET)
    @ResponseBody
    public String simplestGetPath() {
        return "FooBarSimplest";
    }

}

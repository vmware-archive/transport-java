/**
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.operations;

import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class MockRestController {

    @RequestMapping(value = "/foo/{bar}", method = GET)
    @ResponseBody
    public String getSimpleGetPath(@PathVariable String baz,
                                   @RequestParam(value = "boz", required = false) String bozQuery) {
        return "FooBar" + baz;
    }

}

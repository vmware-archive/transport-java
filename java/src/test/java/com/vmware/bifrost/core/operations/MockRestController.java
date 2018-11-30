/**
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core.operations;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController(value = "MockRestController")
public class MockRestController {


    @RequestMapping(value = "/foo/{baz}", method = GET)
    @ResponseBody
    public String simpleGetPath(@PathVariable String baz,
                                @RequestParam(value = "boz", required = false) String bozQuery) {
        return "FooBarSimple:/foo/" + baz + "?boz=" + bozQuery;
    }

    @RequestMapping(value = "/foo/{baz}/bar/{orgId}", method = GET)
    @ResponseBody
    public String normalGetPath(@PathVariable String baz,
                                @PathVariable String orgId,
                                @RequestParam(value = "someQuery", required = true) String bozQuery,
                                @RequestParam(value = "anotherQuery", required = false) String bizzleQuery) {
        return "FooBarNormal:/foo/" + baz + "/bar/" + orgId + "?someQuery=" + bozQuery + "&anotherQuery=" + bizzleQuery;
    }

    @RequestMapping(value = "/foo", method = GET)
    @ResponseBody
    public String simplestGetPath() {
        return "FooBarSimplest";
    }


    @RequestMapping(value = "/multi", method = GET)
    @ResponseBody
    public String multiMethodURIGET() {
        return "multiMethodURIGet";
    }

    @RequestMapping(value = "/multi", method = POST)
    @ResponseBody
    public String multiMethodURIPost(@RequestBody String postedData) {
        return "multiMethodURIPost-" + postedData;
    }

    @RequestMapping(value = "/multi", method = PATCH)
    @ResponseBody
    public String multiMethodURIPatch(
            @RequestParam(value = "query", required = false) String paramQuery,
            @RequestBody String postedData
    ) {
        return "multiMethodURIPatch-" + paramQuery + "-" + postedData;
    }

    @PatchMapping(value = "/patch-mapping/{id}")
    public String patchMappingWithParams(@PathVariable String id, SampleDTO dto) {
        return "patchMappingWithParams-" + id + "-" + dto.getName() + ':' + dto.getValue();
    }

    @GetMapping(value = "/get-mapping/{id}")
    public String getMappingWithParams(@PathVariable String id) {
        return "getMappingWithParams-" + id;
    }

    @DeleteMapping(value = "/delete-mapping/{id}")
    public String deleteMappingMappingWithParams(@PathVariable UUID id) {
        return "deleteMappingWithParams-" + id;
    }

    @PutMapping(value = "/put-mapping/{id}")
    public String putMappingMappingWithParams(@PathVariable UUID id) {
        return "putMappingWithParams-" + id;
    }

    @PostMapping(value = "/post-mapping")
    public String postMapping(String payload) {
        return "postMapping-" + payload;
    }

    @PostMapping(value = "/post-mapping/dto")
    public SampleDTO postMappingWithDTOAndQuery(@RequestParam Integer value, String payload) {
        SampleDTO dto = new SampleDTO();
        dto.setName(payload);
        dto.setValue(value);
        return dto;
    }

}

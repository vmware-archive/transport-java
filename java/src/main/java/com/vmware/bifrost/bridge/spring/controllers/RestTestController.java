package com.vmware.bifrost.bridge.spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class RestTestController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> sendToMenuItems(String test) {
        messagingTemplate.convertAndSend("/topic/notification", "test");
        return new ResponseEntity<>(test, HttpStatus.OK);
    }
}

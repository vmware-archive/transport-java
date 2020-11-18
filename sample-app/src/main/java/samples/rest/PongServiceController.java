/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.rest;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.core.AbstractBase;
import com.vmware.transport.core.error.GeneralError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import samples.pong.PongService;

import java.util.UUID;

@Component
@RestController
@RequestMapping("/rest/samples")
@SuppressWarnings("unchecked")
public class PongServiceController extends AbstractBase {

    PongServiceController() {
        super();
    }

    @Override
    public void initialize() {

    }

    private void callPongService(Request request, DeferredResult<ResponseEntity<?>> result) {
        this.callService(
                request.getId(),
                PongService.Channel,
                request,
                (Response resp) -> {
                    result.setResult(ResponseEntity.ok(resp));
                },
                (GeneralError err) -> {
                    result.setResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err));
                }
        );
    }

    @RequestMapping(value = "/pong/full", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> pongFull(@RequestBody Request request) {
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        new Thread(() -> {
            this.callPongService(request, result);

        }).start();
        return result;
    }

    @RequestMapping(value = "/pong/basic", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> pongBasic(@RequestBody Request request) {
        request.setRequest("basic");
        return this.pongFull(request);
    }
}

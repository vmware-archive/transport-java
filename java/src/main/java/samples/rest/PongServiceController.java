package samples.rest;

/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractBase;
import com.vmware.bifrost.core.error.GeneralError;
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

    @RequestMapping(value = "/pong", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> pongBasic(@RequestBody Request request) {
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }
        new Thread(() -> {

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
        }).start();
        return result;
    }
}

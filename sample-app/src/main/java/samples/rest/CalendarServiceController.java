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
import samples.calendar.CalendarService;
import samples.pong.PongService;

import java.util.UUID;

@Component
@RestController
@RequestMapping("/rest/samples")
@SuppressWarnings("unchecked")
public class CalendarServiceController extends AbstractBase {

    CalendarServiceController() {
        super();
    }

    @Override
    public void initialize() {

    }

    private void callCalendarService(Request request, DeferredResult<ResponseEntity<?>> result) {
        this.callService(
                request.getId(),
                CalendarService.Channel,
                request,
                (Response resp) -> {
                    result.setResult(ResponseEntity.ok(resp));
                },
                (GeneralError err) -> {
                    result.setResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err));
                }
        );
    }

    @RequestMapping(value = "/calendar/date", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> calendarDate(@RequestBody Request request) {
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        if (request.getId() == null) {
            request.setId(UUID.randomUUID());
        }

        if (request.getRequest() == null) {
            request.setRequest("date");
        }
        new Thread(() -> {
            this.callCalendarService(request, result);

        }).start();
        return result;
    }

    @RequestMapping(value = "/calendar/time", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> calendarTime(@RequestBody Request request) {
        request.setRequest("time");
        return this.calendarDate(request);
    }
}

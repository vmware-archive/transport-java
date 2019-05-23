/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package rabbitSamples;

import com.vmware.bifrost.core.AbstractBase;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import rabbitSamples.config.GalacticChannels;
import rabbitSamples.util.MessageUtil;

@Component
@RestController
@RequestMapping("/rest")
public class SampleRestController extends AbstractBase {

    private int responseCount = 0;

    @RequestMapping(value = "/post-request", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<?>> postRequest(
            @RequestParam("request") String request) {

        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        this.bus.requestOnce(
              GalacticChannels.REQUEST_CHANNEL,
              request,
              GalacticChannels.RESPONSE_CHANNEL,
              message -> {
                  try {
                      result.setResult(ResponseEntity.ok(
                            "Response: " + MessageUtil.getStringPayload(message)));
                  } catch (Exception ex) {
                      result.setResult(ResponseEntity.status(500).body(ex.getMessage()));
                  }
              });
        return result;
    }

    @RequestMapping(value = "/get-log-entry", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<?>> getLogEntry() {

        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        this.bus.listenOnce(
              GalacticChannels.REQUEST_LOG_CHANNEL,
              message -> {
                  try {
                      RequestLogEntry logEntry = (RequestLogEntry) message.getPayload();
                      result.setResult(ResponseEntity.ok(logEntry));
                  } catch (Exception ex) {
                      result.setResult(ResponseEntity.status(500).body(ex.getMessage()));
                  }
              });
        return result;
    }

    @RequestMapping(value = "/get-response-count", method = RequestMethod.GET)
    @ResponseBody
    public String getResponseCount() {
        return "Number of received responses: " + responseCount;
    }

    @Override
    public void initialize() {
        try {
            this.bus.listenStream(GalacticChannels.RESPONSE_CHANNEL,
                  message -> responseCount++);
        } catch (Exception ex) {
            logErrorMessage("Cannot subscribe to galactic channel " +
                  GalacticChannels.RESPONSE_CHANNEL, ex.getMessage());
        }
    }
}
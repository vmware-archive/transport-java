/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package kafkaSamples;

import com.vmware.bifrost.broker.kafka.KafkaMessageWrapper;
import com.vmware.bifrost.core.AbstractBase;
import kafkaSamples.config.GalacticChannels;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.LinkedList;
import java.util.List;

@Component
@RestController
@RequestMapping("/rest")
public class SampleRestController extends AbstractBase {

    private List<RequestLogEntry> requestLogEntries = new LinkedList<>();

    @RequestMapping(value = "/post-request", method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<?>> postRequest(
            @RequestParam("request") String request,
            @RequestParam(value = "requestKey", required = false) String key) {

        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        this.bus.requestOnce(
              GalacticChannels.REQUEST_CHANNEL,
              new KafkaMessageWrapper(key, request),
              GalacticChannels.RESPONSE_CHANNEL,
              message -> {
                  try {
                      result.setResult(ResponseEntity.ok(
                            "Response: " + message));
                  } catch (Exception ex) {
                      result.setResult(ResponseEntity.status(500).body(ex.getMessage()));
                  }
              });
        return result;
    }

    @RequestMapping(value = "/get-log", method = RequestMethod.GET)
    @ResponseBody
    public String getLogEntry() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>\n");
        stringBuilder.append("<table style=\"width:100%; border: 1px solid black;\">\n");
        stringBuilder.append("<tr>\n");
        stringBuilder.append("<th style=\"text-align: left;\">request</th>\n");
        stringBuilder.append("<th style=\"text-align: left;\">response</th>\n");
        stringBuilder.append("<th style=\"text-align: left;\">requestTimeInMillis</th>\n");
        stringBuilder.append("</tr>\n");

        for (RequestLogEntry entry : requestLogEntries) {
           stringBuilder.append("<tr>\n");
           stringBuilder.append("<td>" + StringEscapeUtils.escapeHtml4(entry.request) + "</td>");
           stringBuilder.append("<td>" + StringEscapeUtils.escapeHtml4(entry.response) + "</td>");
           stringBuilder.append("<td>" + entry.requestTimeInMillis + "</td>");
           stringBuilder.append("</tr>\n");
        }
       stringBuilder.append("</table>\n");
       stringBuilder.append("</html>\n");

       return stringBuilder.toString();
    }

    @Override
    public void initialize() {
        try {
            this.bus.listenStream(GalacticChannels.REQUEST_LOG_CHANNEL,
                  message -> {
                     try {
                        requestLogEntries.add((RequestLogEntry) message.getPayload());
                     } catch (Exception ex) {
                        logErrorMessage("Error processing request log entry.", ex.getMessage());
                     }
                  });
        } catch (Exception ex) {
            logErrorMessage("Cannot subscribe to galactic channel " +
                  GalacticChannels.RESPONSE_CHANNEL, ex.getMessage());
        }
    }
}
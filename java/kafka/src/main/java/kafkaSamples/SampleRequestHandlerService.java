/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package kafkaSamples;

import com.vmware.bifrost.broker.kafka.KafkaMessageWrapper;
import com.vmware.bifrost.core.AbstractBase;
import org.springframework.stereotype.Component;
import kafkaSamples.config.GalacticChannels;

@Component
public class SampleRequestHandlerService extends AbstractBase {

   @Override
   public void initialize() {
      try {
         this.bus.listenStream(GalacticChannels.REQUEST_CHANNEL,
               message -> {
                  String request = (String) ((KafkaMessageWrapper) message.getPayload()).getMessage();
                  String requestKey = (String) ((KafkaMessageWrapper) message.getPayload()).getKey();

                  // Send the response
                  String response = request + "-response";
                  if (requestKey != null) {
                     response = requestKey + "-" + response;
                  }
                  this.bus.sendRequestMessage(GalacticChannels.RESPONSE_CHANNEL, response);

                  // Add a log entry for the request
                  RequestLogEntry logEntry = new RequestLogEntry();
                  logEntry.request = request;
                  logEntry.response = response;
                  logEntry.requestTimeInMillis = System.currentTimeMillis();
                  this.bus.sendRequestMessage(GalacticChannels.REQUEST_LOG_CHANNEL, logEntry);
               });

      } catch (Exception ex) {
         logErrorMessage("Cannot subscribe to galactic channel " +
               GalacticChannels.REQUEST_CHANNEL, ex.getMessage());
      }
   }
}

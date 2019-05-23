/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package rabbitSamples;

import com.vmware.bifrost.core.AbstractBase;
import org.springframework.stereotype.Component;
import rabbitSamples.config.GalacticChannels;
import rabbitSamples.util.MessageUtil;

@Component
public class SampleRequestHandlerService extends AbstractBase {

   @Override
   public void initialize() {
      try {
         this.bus.listenStream(GalacticChannels.REQUEST_CHANNEL,
               message -> {
                  String request = MessageUtil.getStringPayload(message);

                  // Send the response
                  String response = request + "-response";
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

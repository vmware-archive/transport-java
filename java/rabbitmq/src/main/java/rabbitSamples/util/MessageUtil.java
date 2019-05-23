/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package rabbitSamples.util;

import com.vmware.bifrost.bus.model.Message;

import java.nio.charset.Charset;

public class MessageUtil {

   public static String getStringPayload(Message message) {
      if (message.getPayload() instanceof String) {
         return (String) message.getPayload();
      }
      // Message payload can be byte array if the message was posted without
      // any type header (this can happen if you post a message directly from
      // the RabbitMQ Web UI).
      // Message posted from the RabbitMessageBrokerConnector will have their
      // type header correctly set.
      if (message.getPayload() instanceof byte[]) {
         return new String((byte[]) message.getPayload(), Charset.forName("UTF-8"));
      }
      return "";
   }
}

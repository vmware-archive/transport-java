/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package messageFabricSamples.config;

import com.vmware.atlas.message.client.model.enums.MessageType;
import com.vmware.atlas.message.client.service.MessageClient;
import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfigurer;
import com.vmware.bifrost.broker.messageFabric.MessageFabricBrokerConnector;
import com.vmware.bifrost.bus.EventBus;
import messageFabricSamples.RequestLogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BifrostConfig implements BifrostBridgeConfigurer {

   @Value("${messages.service.name}")
   private String serviceName;

   @Autowired
   EventBus eventBus;

   @Autowired
   MessageClient messageClient;

   @Override
   public void configureGalacticChannels() {

      // Create new MessageFabricBrokerConnector and register it in the EventBus
      MessageFabricBrokerConnector mfConnector = new MessageFabricBrokerConnector(messageClient);
      eventBus.registerMessageBroker(mfConnector);

      // Map GalacticChannels.REQUEST_CHANNEL to MessageFabric "request-topic" topic.
      eventBus.markChannelAsGalactic(
            GalacticChannels.REQUEST_CHANNEL,
            mfConnector.newTopicMessageFabricChannelConfig(
                  MessageType.MESSAGE, "request-topic", String.class));

      // Map GalacticChannels.RESPONSE_CHANNEL to MessageFabric "response-topic" topic.
      eventBus.markChannelAsGalactic(
            GalacticChannels.RESPONSE_CHANNEL,
            mfConnector.newTopicMessageFabricChannelConfig(
                  MessageType.MESSAGE, "response-topic", String.class));

      // Map GalacticChannels.REQUEST_LOG_CHANNEL directly to a MessageFabric cloud service.
      eventBus.markChannelAsGalactic(
            GalacticChannels.REQUEST_LOG_CHANNEL,
            mfConnector.newDirectMessageFabricChannelConfig(
                  MessageType.MESSAGE, serviceName, RequestLogEntry.class));
   }
}

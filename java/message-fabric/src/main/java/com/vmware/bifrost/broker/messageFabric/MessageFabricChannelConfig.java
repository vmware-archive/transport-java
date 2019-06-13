/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.messageFabric;

import com.vmware.atlas.message.client.model.enums.MessageType;
import com.vmware.bifrost.broker.GalacticChannelConfig;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.Serializable;

/**
 * GalacticChannelConfig implementation for MessageFabric broker.
 */
public class MessageFabricChannelConfig extends GalacticChannelConfig {

   @Getter(AccessLevel.PACKAGE)
   private final MessageType messageType;

   @Getter(AccessLevel.PACKAGE)
   private final String destinationTopicOrServiceName;

   @Getter(AccessLevel.PACKAGE)
   private final boolean isDirect;

   @Getter(AccessLevel.PACKAGE)
   private final Class<? extends Serializable> payloadType;

   MessageFabricChannelConfig(String messageBrokerId,
         Class<? extends Serializable> payloadType,
         MessageType messageType,
         String destinationTopicOrServiceName, boolean isDirect) {

      super(messageBrokerId);

      this.payloadType = payloadType;
      this.messageType = messageType;
      this.destinationTopicOrServiceName = destinationTopicOrServiceName;
      this.isDirect = isDirect;
   }
}

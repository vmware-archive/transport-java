/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bridge;

/**
 * Defines the supported modes of a Bridge Channel
 */
public enum BridgeChannelMode {
   /**
    * The bridge channel will used only for sending requests from the UI to Java. All response
    * messages send to the Java Transport channel won't be forwarded to the UI.
    *
    * One use case for this mode is when we want to send requests from the UI to the
    * external message brokers like RabbitMQ. If this mode is not used opening a galactic channel
    * from the UI will also create subscription to the external message broker.
    */
   REQUESTS_ONLY,

   /**
    * Default mode, the channel will be used to send requests from the UI to the Java and
    * response messages from the Java to the UI.
    */
   REQUESTS_AND_RESPONSES
}

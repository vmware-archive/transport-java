/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker;

/**
 * Handles message and errors coming from external MessageBrokers.
 */
public interface GalacticMessageHandler {

   /**
    * Handles incoming message.
    */
   void onMessage(Object message);

   /**
    * Handles incoming error.
    */
   void onError(Object error);
}

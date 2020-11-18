/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.broker;

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

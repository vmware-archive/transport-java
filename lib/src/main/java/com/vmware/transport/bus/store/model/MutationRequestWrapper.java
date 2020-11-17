/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.functions.Consumer;
import lombok.Getter;

/**
 * Wrapper for mutation requests providing success and error handlers.
 */
public class MutationRequestWrapper<RequestType> {

   @Getter
   private final RequestType request;

   private Consumer<Object> errorHandler;

   private Consumer<Object> successHandler;

   public MutationRequestWrapper(
         RequestType request, Consumer<Object> successHandler, Consumer<Object> errorHandler) {

      this.request = request;
      this.successHandler = successHandler;
      this.errorHandler = errorHandler;
   }

   /**
    * Notify client that mutation operation has failed.
    */
   public void error(Object error) throws Exception {
      if (errorHandler != null) {
         errorHandler.accept(error);
      }
   }

   /**
    * Notify client that mutation operation was successful.
    */
   public void success(Object result) throws Exception {
      if (errorHandler != null) {
         successHandler.accept(result);
      }
   }
}

/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import io.reactivex.functions.Consumer;

/**
 * Asynchronous or synchronous transaction composed of bus requests. When committed asynchronous
 * transactions fire all requests at once and return once all of them complete. Synchronous transactions
 * will fire requests in sequence and only proceed to the next transaction event once
 * the preceding response has returned.
 */
public interface Transaction {

    /**
     * Create a command to a channel as a part of this transaction.
     * @param channel channel to send the command to
     * @param payload what ever you want to send.
     */
    void sendRequest(String channel, Object payload);

    /**
     * Wait for a store to be ready / initialized as a part of this transaction.
     * @param storeType, the ID of the store.
     */
    void waitForStoreReady(String storeType);

    /**
     * Registers a new complete handler. Once all responses to requests have been received,
     * the transaction is complete.
     *
     * @param completeHandler {@link Consumer} which will be called with an array with all responses
     * in the order the requests were sent.
     */
    void onComplete(Consumer<Message[]> completeHandler);

    /**
     * Register a new error handler. If an error is thrown by any of the responders, the transaction
     * is aborted and the error sent to the registered errorHandlers.
     *
     * @param errorHandler {@link Consumer} which handles any errors during the transaction.
     */
    void onError(Consumer<Message> errorHandler);

    /**
     * Commit the transaction, all requests will be sent and will wait for responses.
     * Once all the responses are in, onComplete handlers will be called with the responses.
     * @returns {@link TransactionReceipt} allows observer to track state of the transaction
     * for monitoring purposes.
     */
    TransactionReceipt commit();

    /**
     * Transaction type.
     */
    enum TransactionType {
        ASYNC,
        SYNC
    }
}

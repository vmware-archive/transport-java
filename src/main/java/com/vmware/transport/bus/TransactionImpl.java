/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bus;

import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.model.MessageObject;
import com.vmware.transport.bus.model.MessageType;
import com.vmware.transport.bus.store.BusStoreApi;
import com.vmware.transport.core.util.Loggable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TransactionImpl extends Loggable implements Transaction {

    private final EventBus bus;
    private final BusStoreApi storeManager;
    private final TransactionType transactionType;
    private final String transactionName;
    private final UUID id;
    private boolean useRandomIdForRequests;

    private TransactionState state;

    private List<Consumer<Message[]>> onCompleteHandlers = new ArrayList<>();
    private List<Consumer<Message>> onErrorHandlers = new ArrayList<>();
    private List<TransactionRequest> requests = new ArrayList<>();

    private Message[] responses;

    private TransactionReceiptImpl transactionReceipt;

    public TransactionImpl(EventBus bus, BusStoreApi storeManager,
            TransactionType type, String name) {

        this(bus, storeManager, type, name, UUID.randomUUID());
        this.useRandomIdForRequests = true;
    }

    public TransactionImpl(EventBus bus, BusStoreApi storeManager,
            TransactionType type, String name, UUID id) {
        this.bus = bus;
        this.storeManager = storeManager;
        this.transactionType = type;
        this.id = id;
        this.useRandomIdForRequests = false;
        this.transactionName = name != null ? name : "transaction-" + this.id.toString();
        this.state = TransactionState.uncommitted;
    }

    @Override
    public void sendRequest(String channel, Object payload) {
        assertUncommittedState("cannot queue a new request via sendRequest()");
        this.requests.add(
              new TransactionRequest(this.requests.size(), channel, payload, null, this.id));
    }

    @Override
    public void waitForStoreReady(String storeType) {
        assertUncommittedState("cannot queue a new request via sendRequest()");
        this.requests.add(
              new TransactionRequest(this.requests.size(), null, null, storeType, this.id));
    }

    @Override
    public void onComplete(Consumer<Message[]> completeHandler) {
        assertUncommittedState("cannot register onComplete() handler");
        this.onCompleteHandlers.add(completeHandler);
    }

    @Override
    public void onError(Consumer<Message> errorHandler) {
        assertUncommittedState("cannot register onError() handler");
        this.onErrorHandlers.add(errorHandler);
    }

    @Override
    public TransactionReceipt commit() {
        assertUncommittedState("cannot commit transaction");

        if (this.requests.isEmpty()) {
            throw new IllegalStateException("Transaction cannot be committed, no requests made.");
        }

        // Mark the transaction as committed
        this.state = TransactionState.committed;

        this.transactionReceipt = new TransactionReceiptImpl(this.requests.size());
        this.responses = new Message[this.requests.size()];

        if (this.transactionType == TransactionType.SYNC) {
            startSyncTransaction();
        } else {
            startAsyncTransaction();
        }

        return this.transactionReceipt;
    }

    private void startAsyncTransaction() {
        for (TransactionRequest request : this.requests) {
            if (request.isStoreTransaction()) {
                waitForStoreAndListen(request, null);
            } else {
                sendRequestAndListen(request, null);
            }
        }
    }

    private void startSyncTransaction() {
        Subject<TransactionRequest> syncStream = PublishSubject.create();

        syncStream.subscribe((TransactionRequest request) -> {
            Consumer<Message> requestHandler = (Message response) -> {
                // Fire the next request (if available).
                if (request.requestIndex + 1 < this.requests.size()) {
                    syncStream.onNext(this.requests.get(request.requestIndex + 1));
                }
            };

            if (request.isStoreTransaction()) {
                waitForStoreAndListen(request, requestHandler);
            } else {
                sendRequestAndListen(request, requestHandler);
            }
        });

        syncStream.onNext(this.requests.get(0));
    }

    private void waitForStoreAndListen(TransactionRequest request, Consumer<Message> onSuccess) {
        this.logDebugMessage(String.format("➡️ Transaction: Waiting '%s' for store '%s'",
              this.transactionType.toString(), request.storeType), this.transactionName);
        this.transactionReceipt.requestsSent++;
        this.storeManager.createStore(request.storeType).whenReady( map -> {
            if (this.state == TransactionState.aborted) {
                // Ignore this response if the transaction is in aborted state.
                return;
            }
            onTransactionRequestSuccess(
                  request, new MessageObject(MessageType.MessageTypeResponse, map), onSuccess);
        });

    }

    private void sendRequestAndListen(TransactionRequest request, Consumer<Message> onSuccess) {

        this.logDebugMessage(String.format("-->️ Transaction: Sending '%s' Request to channel: %s",
              this.transactionType.toString(), request.channel), this.transactionName);
        this.transactionReceipt.requestsSent++;
        UUID requestId;
        if (useRandomIdForRequests) {
            requestId = UUID.randomUUID();
        } else {
            requestId = request.id;
        }

        this.bus.requestOnceWithId(requestId,
              request.channel,
              request.payload,
              request.channel,
              this.transactionName,
              (Message response) -> {

                  if (this.state == TransactionState.aborted) {
                      // Ignore this response if the transaction is in aborted state.
                      return;
                  }

                  logDebugMessage(String.format("<-- Transaction: Received '%s' Response on channel: %s - %s",
                        this.transactionType,
                        request.channel,
                        response.toString()),
                        this.transactionName);

                  onTransactionRequestSuccess(request, response, onSuccess);
              },
              (Message errMessage) -> {
                  if (this.state == TransactionState.aborted) {
                      // Ignore the error if the transaction is in aborted state.
                      return;
                  }
                  logDebugMessage(String.format("<-- Transaction: Received '%s' Error response on channel: %s - %s",
                        this.transactionType,
                        request.channel,
                        errMessage.toString()),
                        this.transactionName);
                  onTransactionError(errMessage);
              });
    }

    private void onTransactionRequestSuccess(
          TransactionRequest request, Message response, Consumer<Message> onSuccess) throws Exception {

        this.transactionReceipt.requestsCompleted++;
        this.responses[request.requestIndex] = response;
        if (onSuccess != null) {
            onSuccess.accept(response);
        }
        if (this.transactionReceipt.requestsCompleted == this.transactionReceipt.totalRequests) {
            // Complete the transaction if this is the last request.
            onTransactionComplete();
        }
    }

    private void onTransactionError(Message errMessage) throws Exception {
        this.state = TransactionState.aborted;
        this.transactionReceipt.aborted = true;
        this.transactionReceipt.abortedTime = new Date();

        for (Consumer<Message> onErrorHandler : this.onErrorHandlers) {
            onErrorHandler.accept(errMessage);
        }
    }

    private void onTransactionComplete() throws Exception {
        this.transactionReceipt.complete = true;
        this.transactionReceipt.completedTime = new Date();
        this.state = TransactionState.completed;

        for (Consumer<Message[]> onCompleteHandler : this.onCompleteHandlers) {
            onCompleteHandler.accept(this.responses);
        }
    }

    /**
     * Asserts that the transaction is in uncommitted state and
     * throws {@link IllegalStateException} in case it is not.
     */
    private void assertUncommittedState(String msg) {
        if (this.state != TransactionState.uncommitted) {
            this.logWarnMessage(String.format("Transaction '%s' is in '%s' state: %s",
                  this.transactionName, this.state.toString(), msg));
            throw new IllegalStateException(String.format("Transaction %s is in '%s' state",
                  this.id, this.state.toString()));
        }
    }

    private enum TransactionState {
        uncommitted,
        committed,
        completed,
        aborted
    }

    private static class TransactionRequest {

        final int requestIndex;
        final String channel;
        final Object payload;
        final UUID id;
        final String storeType;

        TransactionRequest(int index, String channel, Object payload, String storeType, UUID id) {
            this.requestIndex = index;
            this.payload = payload;
            this.channel = channel;
            this.storeType = storeType;
            this.id = id;
        }

        public boolean isStoreTransaction() {
            return this.storeType != null;
        }
    }

    private static class TransactionReceiptImpl implements TransactionReceipt {

        @Getter
        public int totalRequests;

        @Getter
        public int requestsSent;

        @Getter
        public int requestsCompleted;

        @Getter
        public boolean complete;

        @Getter
        public boolean aborted;

        @Getter
        public Date startedTime;

        @Getter
        public Date completedTime;

        @Getter
        public Date abortedTime;

        TransactionReceiptImpl(int totalRequests) {
            this.totalRequests = totalRequests;
            this.startedTime = new Date();
        }
    }
}

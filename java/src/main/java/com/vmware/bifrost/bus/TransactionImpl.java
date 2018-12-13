/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.util.Loggable;
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
    private final TransactionType transactionType;
    private final String transactionName;
    private final String id;

    private TransactionState state;

    private List<Consumer<Message[]>> onCompleteHandlers = new ArrayList<>();
    private List<Consumer<Message>> onErrorHandlers = new ArrayList<>();
    private List<TransactionRequest> requests = new ArrayList<>();

    private Message[] responses;

    private TransactionReceiptImpl transactionReceipt;

    public TransactionImpl(EventBus bus, TransactionType type, String name) {

        this.bus = bus;
        this.transactionType = type;
        this.id = UUID.randomUUID().toString();
        this.transactionName = name != null ? name : "transaction-" + this.id;
        this.state = TransactionState.uncommitted;
    }

    @Override
    public void sendRequest(String channel, Object payload) {
        assertUncommittedState("cannot queue a new request via sendRequest()");
        this.requests.add(new TransactionRequest(this.requests.size(), channel, payload));
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
            sendRequestAndListen(request, null);
        }
    }

    private void startSyncTransaction() {
        Subject<TransactionRequest> syncStream = PublishSubject.create();

        syncStream.subscribe((TransactionRequest request) ->
            sendRequestAndListen(request, (Message response) -> {
                // Fire the next request (if available).
                if (request.requestIndex + 1 < this.requests.size()) {
                    syncStream.onNext(this.requests.get(request.requestIndex + 1));
                }
            }));

        syncStream.onNext(this.requests.get(0));
    }

    private void sendRequestAndListen(TransactionRequest request, Consumer<Message> onSuccess) {

        this.logDebugMessage(String.format("➡️ Transaction: Sending '%s' Request to channel: %s",
              this.transactionType.toString(), request.channel), this.transactionName);
        this.transactionReceipt.requestsSent++;
        UUID requestId = UUID.randomUUID();
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

                  logDebugMessage(String.format("⬅️ Transaction: Received '%s' Response on channel: %s - %s",
                        this.transactionType,
                        request.channel,
                        response.toString()),
                        this.transactionName);

                  this.transactionReceipt.requestsCompleted++;
                  this.responses[request.requestIndex] = response;
                  if (onSuccess != null) {
                      onSuccess.accept(response);
                  }
                  if (this.transactionReceipt.requestsCompleted == this.transactionReceipt.totalRequests) {
                      // Complete the transaction if this is the last request.
                      onTransactionComplete();
                  }
              },
              (Message errMessage) -> {
                  if (this.state == TransactionState.aborted) {
                      // Ignore the error if the transaction is in aborted state.
                      return;
                  }
                  logDebugMessage(String.format("⬅️ Transaction: Received '%s' Error response on channel: %s - %s",
                        this.transactionType,
                        request.channel,
                        errMessage.toString()),
                        this.transactionName);
                  onTransactionError(errMessage);
              });
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

        TransactionRequest(int index, String channel, Object payload) {
            this.requestIndex = index;
            this.payload = payload;
            this.channel = channel;
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

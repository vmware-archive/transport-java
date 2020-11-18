/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus;

import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.store.BusStoreApi;
import com.vmware.transport.bus.store.StoreManager;
import com.vmware.transport.bus.store.model.BusStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionImplTest {

    private EventBus bus;
    private BusStoreApi storeManager;

    private Message[] responses;
    private Message errorMsg;
    private String channel;

    private int counter;

    private List<Message> requestMessages;

    @Before
    public void before() throws Exception {
        this.bus = new EventBusImpl();
        this.storeManager = new StoreManager(this.bus);
        ((EventBusImpl)this.bus).setStoreManager(this.storeManager);
        this.requestMessages = new ArrayList<>();
        this.channel = "local-channel";
        this.counter = 0;
    }

    @Test
    public void testSingleRequest() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);

        transaction.sendRequest(this.channel, "request1");

        // verify that no request have been triggered before calling commit
        Assert.assertNull(this.responses);
        Assert.assertNull(this.errorMsg);
        Assert.assertTrue(this.requestMessages.isEmpty());

        TransactionReceipt receipt = transaction.commit();

        Assert.assertEquals(receipt.getTotalRequests(), 1);
        Assert.assertEquals(receipt.getRequestsCompleted(), 0);
        Assert.assertFalse(receipt.isComplete());
        Assert.assertFalse(receipt.isAborted());

        Assert.assertEquals(this.requestMessages.size(), 1);

        this.bus.sendResponseMessageWithId(channel, "response1", this.requestMessages.get(0).getId());

        verifyTransactionCompleted(receipt, "response1");
    }

    @Test
    public void testSingleRequestWithError() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);

        transaction.sendRequest(this.channel, "request1");

        TransactionReceipt receipt = transaction.commit();

        this.bus.sendErrorMessageWithId(channel, "error", this.requestMessages.get(0).getId());

        verifyTransactionAborted(receipt, "error", 0);
    }

    @Test
    public void testSyncTransactionMultipleRequests() {
        Transaction transaction = initTransaction(Transaction.TransactionType.SYNC);

        transaction.sendRequest(this.channel, "request1");
        transaction.sendRequest(this.channel, "request2");
        transaction.waitForStoreReady("testStore");
        transaction.sendRequest(this.channel, "request3");

        TransactionReceipt receipt = transaction.commit();

        Assert.assertEquals(this.requestMessages.size(), 1);
        Assert.assertEquals(this.requestMessages.get(0).getPayload(), "request1");

        this.bus.sendResponseMessageWithId(channel, "response1", this.requestMessages.get(0).getId());
        verifyTransactionInProgress(receipt, 1);

        Assert.assertEquals(this.requestMessages.size(), 2);
        Assert.assertEquals(this.requestMessages.get(1).getPayload(), "request2");

        this.bus.sendResponseMessageWithId(channel, "response2", this.requestMessages.get(1).getId());
        verifyTransactionInProgress(receipt, 2);

        UUID storeItemId = UUID.randomUUID();
        this.storeManager.getStore("testStore").getBusStoreInitializer()
              .add(storeItemId, "testItem")
              .done();
        verifyTransactionInProgress(receipt, 3);

        Assert.assertEquals(this.requestMessages.size(), 3);
        Assert.assertEquals(this.requestMessages.get(2).getPayload(), "request3");
        this.bus.sendResponseMessageWithId(channel, "response3", this.requestMessages.get(2).getId());

        verifyTransactionCompleted(receipt,
              "response1", "response2", this.storeManager.getStore("testStore"), "response3");

        // Verify that there is only one channel reference (the one from the listenRequestStream).
        Assert.assertEquals(this.bus.getApi().getChannelMap().get(channel).getRefCount().intValue(), 1);
    }

    @Test
    public void testSyncTransactionMultipleRequestsWithError() {
        Transaction transaction = initTransaction(Transaction.TransactionType.SYNC);

        transaction.sendRequest(this.channel, "request1");
        transaction.sendRequest(this.channel, "request2");
        transaction.sendRequest(this.channel, "request3");

        TransactionReceipt receipt = transaction.commit();

        this.bus.sendResponseMessageWithId(channel, "response1", this.requestMessages.get(0).getId());
        verifyTransactionInProgress(receipt, 1);

        this.bus.sendErrorMessageWithId(channel, "request2-error", this.requestMessages.get(1).getId());
        verifyTransactionAborted(receipt, "request2-error", 1);

        // Verify that third request wasn't sent after the error.
        Assert.assertEquals(this.requestMessages.size(), 2);

        // Verify that there is only one channel reference (the one from the listenRequestStream).
        Assert.assertEquals(this.bus.getApi().getChannelMap().get(channel).getRefCount().intValue(), 1);
    }

    @Test
    public void testAsyncTransactionMultipleRequests() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);

        transaction.sendRequest(this.channel, "request1");
        transaction.sendRequest(this.channel, "request2");
        transaction.waitForStoreReady("testStore");
        transaction.sendRequest(this.channel, "request3");

        TransactionReceipt receipt = transaction.commit();

        Assert.assertEquals(this.requestMessages.size(), 3);
        Assert.assertEquals(this.requestMessages.get(0).getPayload(), "request1");
        Assert.assertEquals(this.requestMessages.get(1).getPayload(), "request2");
        Assert.assertEquals(this.requestMessages.get(2).getPayload(), "request3");
        Assert.assertEquals(receipt.getRequestsSent(), 4);

        this.bus.sendResponseMessageWithId(channel, "response3", this.requestMessages.get(2).getId());
        verifyTransactionInProgress(receipt, 1);

        this.bus.sendResponseMessageWithId(channel, "response1", this.requestMessages.get(0).getId());
        verifyTransactionInProgress(receipt, 2);

        this.storeManager.getStore("testStore").getBusStoreInitializer()
              .add(UUID.randomUUID(), "testValue").done();
        verifyTransactionInProgress(receipt, 3);

        this.bus.sendResponseMessageWithId(channel, "response2", this.requestMessages.get(1).getId());
        verifyTransactionCompleted(receipt,
              "response1", "response2", this.storeManager.getStore("testStore"), "response3");

        // Verify that there is only one channel reference (the one from the listenRequestStream).
        Assert.assertEquals(this.bus.getApi().getChannelMap().get(channel).getRefCount().intValue(), 1);
    }

    @Test
    public void testAsyncTransactionMultipleRequestsWithError() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);

        transaction.sendRequest(this.channel, "request1");
        transaction.sendRequest(this.channel, "request2");
        transaction.sendRequest(this.channel, "request3");

        TransactionReceipt receipt = transaction.commit();

        this.bus.sendResponseMessageWithId(channel, "response3", this.requestMessages.get(2).getId());
        verifyTransactionInProgress(receipt, 1);

        this.bus.sendErrorMessageWithId(channel, "response1-error", this.requestMessages.get(0).getId());

        verifyTransactionAborted(receipt, "response1-error", 1);

        this.bus.sendResponseMessageWithId(channel, "response2", this.requestMessages.get(1).getId());

        Assert.assertEquals(receipt.getRequestsCompleted(), 1);

        // Verify that there is only one channel reference (the one from the listenRequestStream).
        Assert.assertEquals(this.bus.getApi().getChannelMap().get(channel).getRefCount().intValue(), 1);
    }

    @Test
    public void testAsyncTransactionWithMultipleErrors() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);

        transaction.sendRequest(this.channel, "request1");
        transaction.sendRequest(this.channel, "request2");
        transaction.waitForStoreReady("testStore");
        transaction.sendRequest(this.channel, "request3");

        TransactionReceipt receipt = transaction.commit();

        this.bus.sendResponseMessageWithId(channel, "response3", this.requestMessages.get(2).getId());

        this.bus.sendErrorMessageWithId(channel, "response1-error", this.requestMessages.get(0).getId());
        this.bus.sendErrorMessageWithId(channel, "response2-error", this.requestMessages.get(1).getId());
        this.storeManager.getStore("testStore").initialize();

        verifyTransactionAborted(receipt, "response1-error", 1);
    }

    @Test
    public void testCommitEmptyTransaction() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);
        IllegalStateException invalidStateEx = null;
        try {
            transaction.commit();
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        Assert.assertNotNull(invalidStateEx);
        Assert.assertEquals(invalidStateEx.getMessage(), "Transaction cannot be committed, no requests made.");
    }

    @Test
    public void testReCommitTransaction() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);
        transaction.sendRequest(this.channel, "request1");
        transaction.commit();
        IllegalStateException invalidStateEx = null;
        try {
            transaction.commit();
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        Assert.assertNotNull(invalidStateEx);
        verifyInvalidTransactionStateError(invalidStateEx, "committed");
    }

    @Test
    public void testAddHandlersForCommittedTransaction() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);
        transaction.sendRequest(this.channel, "request1");
        transaction.commit();

        IllegalStateException invalidStateEx = null;
        try {
            transaction.onComplete((Message[] msgs) -> {});
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        verifyInvalidTransactionStateError(invalidStateEx, "committed");

        this.bus.sendResponseMessageWithId(this.channel, "response1", this.requestMessages.get(0).getId());

        invalidStateEx = null;
        try {
            transaction.onError((Message msg) -> {});
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        verifyInvalidTransactionStateError(invalidStateEx, "completed");
    }

    @Test
    public void testAddRequestForCommittedAndAbortedTransactions() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);
        transaction.sendRequest(this.channel, "request1");
        transaction.commit();

        IllegalStateException invalidStateEx = null;
        try {
            transaction.sendRequest(this.channel, "request2");
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        verifyInvalidTransactionStateError(invalidStateEx, "committed");

        try {
            transaction.waitForStoreReady("testStore");
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        verifyInvalidTransactionStateError(invalidStateEx, "committed");

        this.bus.sendErrorMessageWithId(this.channel, "error1", this.requestMessages.get(0).getId());

        invalidStateEx = null;
        try {
            transaction.sendRequest(this.channel, "request3");
        } catch (IllegalStateException ex) {
            invalidStateEx = ex;
        }
        verifyInvalidTransactionStateError(invalidStateEx, "aborted");
    }

    @Test
    public void testWithMultipleOnCompleteHandlers() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);
        transaction.onComplete((Message[] msgs) -> this.counter++);
        transaction.onComplete((Message[] msgs) -> this.counter++);

        transaction.sendRequest(this.channel, "request1");
        transaction.waitForStoreReady("testStore");
        transaction.commit();

        this.storeManager.getStore("testStore").initialize();
        this.bus.sendResponseMessageWithId(this.channel, "response1", this.requestMessages.get(0).getId());
        Assert.assertEquals(this.counter, 2);
    }

    @Test
    public void testWithMultipleOnErrorHandlers() {
        Transaction transaction = initTransaction(Transaction.TransactionType.ASYNC);
        transaction.onError((Message msg) -> this.counter++);
        transaction.onError((Message msg) -> this.counter++);

        transaction.sendRequest(this.channel, "request1");
        transaction.commit();

        this.bus.sendErrorMessageWithId(this.channel, "response1-error", this.requestMessages.get(0).getId());
        Assert.assertEquals(this.counter, 2);
    }

    private Transaction initTransaction(Transaction.TransactionType type) {
        Transaction transaction = bus.createTransaction(type, "test-transaction");
        transaction.onComplete( (Message[] msgs) -> this.responses = msgs );
        transaction.onError( (Message err) -> this.errorMsg = err );

        bus.listenRequestStream(channel, (Message requestMsg) -> this.requestMessages.add(requestMsg));
        return transaction;
    }

    private void verifyInvalidTransactionStateError(Exception ex, String state) {
        Assert.assertNotNull(ex);
        Assert.assertNotNull(ex.getMessage());
        Assert.assertTrue(String.format("Expect transaction to be in %s state.", state),
              ex.getMessage().matches("Transaction .* is in '" + state + "' state"));
    }

    private void verifyTransactionCompleted(TransactionReceipt receipt, Object... expectedResponses) {
        Assert.assertEquals(this.responses.length, expectedResponses.length);

        for (int i = 0; i < this.responses.length; i++) {
            if (expectedResponses[i] instanceof BusStore) {
                Map<UUID, Object> expectedValue =
                      ((BusStore)expectedResponses[i]).allValuesAsMap();

                Map<UUID, Object> actualValue = (Map<UUID, Object>) this.responses[i].getPayload();
                Assert.assertEquals(expectedValue.size(), actualValue.size());
                for (Map.Entry<UUID, Object> value : expectedValue.entrySet()) {
                    Assert.assertEquals(value.getValue(), actualValue.get(value.getKey()));
                }
            } else {
                Assert.assertEquals(this.responses[i].getPayload(), expectedResponses[i]);
            }
        }

        Assert.assertEquals(receipt.getRequestsCompleted(), expectedResponses.length);
        Assert.assertNotNull(receipt.getCompletedTime());
        Assert.assertNotNull(receipt.getStartedTime());
        Assert.assertFalse(receipt.isAborted());
        Assert.assertTrue(receipt.isComplete());
        Assert.assertNull(this.errorMsg);
    }

    private void verifyTransactionInProgress(TransactionReceipt receipt, int expectedCompletedRequests) {
        Assert.assertEquals(receipt.getRequestsCompleted(), expectedCompletedRequests);
        Assert.assertNull(receipt.getCompletedTime());
        Assert.assertFalse(receipt.isAborted());
        Assert.assertFalse(receipt.isComplete());
        Assert.assertNull(this.errorMsg);
        Assert.assertNull(this.responses);
    }

    private void verifyTransactionAborted(TransactionReceipt receipt,
            Object expectedError, int expectedCompletedRequests) {
        Assert.assertNull(this.responses);
        Assert.assertEquals(this.errorMsg.getPayload(), expectedError);

        Assert.assertEquals(receipt.getRequestsCompleted(), expectedCompletedRequests);
        Assert.assertNotNull(receipt.getAbortedTime());
        Assert.assertNotNull(receipt.getStartedTime());
        Assert.assertTrue(receipt.isAborted());
        Assert.assertFalse(receipt.isComplete());
        Assert.assertEquals(this.errorMsg.getPayload(), expectedError);
    }

}
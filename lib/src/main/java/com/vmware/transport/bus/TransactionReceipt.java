/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus;

import java.util.Date;

/**
 * Describes the current state of a committed {@link Transaction}.
 */
public interface TransactionReceipt {

    /**
     * Total number of requests in the transaction.
     */
    int getTotalRequests();

    /**
     * Current number of the sent requests.
     */
    int getRequestsSent();

    /**
     * Current nNumber of the completed requests.
     */
    int getRequestsCompleted();

    /**
     * Returns true if the transaction is in completed state.
     */
    boolean isComplete();

    /**
     * Returns true if the transaction is in aborted state due to error.
     */
    boolean isAborted();

    /**
     * The time when the transaction was started (committed).
     */
    Date getStartedTime();

    /**
     * The completed time of the transaction. Will be null for aborted or running transactions.
     */
    Date getCompletedTime();

    /**
     * The time when transaction was aborted. Will be null for completed or running transactions.
     */
    Date getAbortedTime();
}

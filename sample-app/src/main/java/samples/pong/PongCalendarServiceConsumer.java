/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.pong;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.Transaction;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.core.AbstractBase;
import com.vmware.transport.core.util.ClassMapper;
import org.springframework.stereotype.Component;
import samples.calendar.CalendarService;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class PongCalendarServiceConsumer extends AbstractBase {

    private ScheduledExecutorService executorService;

    PongCalendarServiceConsumer() { this.executorService = Executors.newScheduledThreadPool(5); }

    /**
     * Call PongService and Calendar Service as a part of a transaction
     */
    @Override
    public void initialize() {
        // create task, wait 1 second before executing.
        Runnable runTransactionTask = () -> this.runTransaction();
        executorService.schedule(runTransactionTask, 1000,  TimeUnit.MILLISECONDS);
    }

    private void runTransaction() {
        // create async transaction
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = bus.createTransaction(Transaction.TransactionType.ASYNC, transactionId);

        // send request to PongService
        transaction.sendRequest(
                PongService.Channel,
                new Request<String>(transactionId, "Basic") // request basic pong
        );

        // send request to CalendarService
        transaction.sendRequest(
                CalendarService.Channel,
                new Request<String>(transactionId, "time") // request time
        );

        // when transaction is done
        transaction.onComplete(
                (Message[] responses) -> {

                    // concatenate service responses into a string.
                    StringBuilder serviceResponses = new StringBuilder();
                    for(Message msg: responses) {

                        String serviceResponse = ClassMapper.CastPayload(String.class, (Response)msg.getPayload());
                        serviceResponses.append("> " + serviceResponse + " "); // add each response together.
                    }

                    // log output.
                    this.logInfoMessage("PongCalendarServiceConsumer:", "Response", serviceResponses.toString());
                }
        );

        // commit transaction.
        transaction.commit();

    }
}

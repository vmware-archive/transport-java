/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.vmware.bifrost.broker.GalacticChannelConfig;
import com.vmware.bifrost.broker.MessageBrokerConnector;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageHeaders;
import io.reactivex.functions.Consumer;

import java.util.UUID;
import java.util.function.Function;

public interface EventBus {

    /**
     * Reference to Low Level API.
     */
    EventBusLowApi getApi();

    /**
     * Send command payload to channel.
     *
     * @param channel channel name to send payload to
     * @param payload the payload to be sent
     */
    void sendRequestMessage(String channel, Object payload);

    /**
     * Send command payload to channel.
     *
     * @param channel channel name to send payload to
     * @param payload the payload to be sent
     * @param payload optional message headers
     */
    void sendRequestMessage(String channel, Object payload, MessageHeaders headers);

    /**
     * Send a command payload to a channel with a supplied ID in the command
     *
     * @param channel channel name to send payload to
     * @param payload the payload to be sent
     * @parma id the UUID of the command
     */
    void sendRequestMessageWithId(String channel, Object payload, UUID id);

    /**
     * Send a command payload to a channel with a supplied ID in the command
     *
     * @param channel channel name to send payload to
     * @param payload the payload to be sent
     * @parma id the UUID of the command
     * @param headers optional message headers
     */
    void sendRequestMessageWithId(String channel, Object payload, UUID id, MessageHeaders headers);

    /**
     * Send a command payload to a channel for the specified user.
     *
     * @param channel channel name to send payload to
     * @param payload the payload to be sent
     * @parma id the UUID of the command
     * @param targetUser target user name
     */
    void sendRequestMessageToTarget(String channel, Object payload, UUID id, String targetUser);

    /**
     * Send a command payload to a channel for the specified user.
     *
     * @param channel channel name to send payload to
     * @param payload the payload to be sent
     * @parma id the UUID of the command
     * @param targetUser target user name
     * @param headers optional message headers
     */
    void sendRequestMessageToTarget(String channel, Object payload, UUID id, String targetUser,
                                    MessageHeaders headers);

    /**
     * Send response payload to a channel.
     *
     * @param channel the channel name to send payload to
     * @param payload the payload to be sent
     */
    void sendResponseMessage(String channel, Object payload);

    /**
     * Send response payload to a channel.
     *
     * @param channel the channel name to send payload to
     * @param payload the payload to be sent
     * @param headers optional message headers
     */
    void sendResponseMessage(String channel, Object payload, MessageHeaders headers);

    /**
     * Send a response payload to a channel with a supplied ID in the response.
     * @param channel the channel name to send payload to
     * @param payload the payload to be sent
     * @param id the UUID to be attached to the response
     */
    void sendResponseMessageWithId(String channel, Object payload, UUID id);

    /**
     * Send a response payload to a channel with a supplied ID in the response.
     * @param channel the channel name to send payload to
     * @param payload the payload to be sent
     * @param id the UUID to be attached to the response
     * @param headers optional message headers
     */
    void sendResponseMessageWithId(String channel, Object payload, UUID id, MessageHeaders headers);

    /**
     * Send a response payload to a channel for the specified user.
     *
     * @param channel the channel name to send payload to
     * @param payload the payload to be sent
     * @parma id the UUID to be attached to the response
     * @param targetUser target user name
     */
    void sendResponseMessageToTarget(String channel, Object payload, UUID id, String targetUser);

    /**
     * Send a response payload to a channel for the specified user.
     *
     * @param channel the channel name to send payload to
     * @param payload the payload to be sent
     * @parma id the UUID to be attached to the response
     * @param targetUser target user name
     * @param headers optional message headers
     */
    void sendResponseMessageToTarget(String channel, Object payload, UUID id, String targetUser,
                                     MessageHeaders headers);

    /**
     * Listen for a command on sendChannel and return a single response via the generateHandler() method.
     * The returned value will be sent as a response message on the sendChannel.
     * Once a single response has been sent, no more command messages will be processed.
     *
     * @param sendChannel the channel to listen for requests
     * @param generateHandler function for generating responses based on the incoming message.
     */
    BusTransaction respondOnce(String sendChannel, Function<Message, Object> generateHandler);

    /**
     * Listen for a command on sendChannel and return a single response via the generateHandler() method.
     * The returned value will be sent as a response message on the return channel (defaults to
     * sendChannel if left blank). Once a single response has been sent, no more command
     * messages will be processed.
     *
     * @param sendChannel the channel to listen for requests
     * @param returnChannel the channel to send responses to (defaults to sendChannel if left blank
     * @param generateHandler function for generating responses based on the incoming message.
     */
    BusTransaction respondOnce(String sendChannel,
            String returnChannel,
            Function<Message, Object> generateHandler);

    /**
     * Listen for requests on sendChannel and return responses via the generateHandler method.
     * The returned value will be sent as a response message on the return channel
     * (defaults to sendChannel if left blank). The responder will continue to stream responses
     * to each command until the BusTransaction.unsubscribe() method is called.
     *
     * @param sendChannel the channel to listen for requests.
     * @param returnChannel the channel to send responses to (defaults to sendChannel if left blank)
     * @param generateHandler function for generating responses based on the incoming message.
     */
    BusTransaction respondStream(String sendChannel,
                                 String returnChannel,
                                 Function<Message, Object> generateHandler);

    /**
     * Listen for requests on sendChannel and return responses via the generateHandler method.
     * The returned value will be sent as a response message on the sendChannel.
     * blank). The responder will continue to stream responses to each command until
     * the BusTransaction.unsubscribe() method is called.
     *
     * @param sendChannel the channel to listen for requests.
     * @param generateHandler function for generating responses based on the incoming message.
     */
    BusTransaction respondStream(String sendChannel,
                                 Function<Message, Object> generateHandler);

    /**
     * Send a command payload to sendChannel and listen for a single response on sendChannel
     * The successHandler is used to process incoming responses. The handler will stop processing
     * any further responses after the first one.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestOnce(String sendChannel,
                               Object payload,
                               Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel and listen for a single response on sendChannel
     * The successHandler is used to process incoming responses. The handler will stop processing
     * any further responses after the first one.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestOnce(String sendChannel,
                               Object payload,
                               Consumer<Message> successHandler,
                               Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel and listen for a single response on returnChannel
     * (defaults to sendChannel if left blank). The successHandler is used to
     * process incoming responses.
     * The handler will stop processing any further responses after the first one.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestOnce(String sendChannel,
                               Object payload,
                               String returnChannel,
                               Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel and listen for a single response on returnChannel
     * (defaults to sendChannel if left blank). The successHandler and errorHandler methods are used to
     * process incoming responses. The handler will stop processing any further responses after the first one.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestOnce(String sendChannel,
                               Object payload,
                               String returnChannel,
                               Consumer<Message> successHandler,
                               Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel and listen for a single response on returnChannel
     * (defaults to sendChannel if left blank). The successHandler and errorHandler methods are used to
     * process incoming responses. The handler will stop processing any further responses after the first one.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param from optional name of the actor implementing (for logging)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestOnce(String sendChannel,
                               Object payload,
                               String returnChannel,
                               String from,
                               Consumer<Message> successHandler,
                               Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel with a message ID. Listens for a single response on sendChannel,
     * but only for a response with the same matching ID. Ideal for multi-message sessions where multiple consumers
     * are requesting at the same time on the same. The successHandler is used to
     * process incoming responses. It will stop processing any further responses after the first one.
     *
     * @param uuid the UUID of the message.
     * @param sendChannel the channel to send the command to
     * @param payload the payload you want to send.
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     Consumer<Message> successHandler);


    /**
     * Send a command payload to sendChannel with a message ID. Listens for a single response on sendChannel,
     * but only for a response with the same matching ID. Ideal for multi-message sessions where multiple consumers
     * are requesting at the same time on the same. The successHandler is used to
     * process incoming responses. It will stop processing any further responses after the first one.
     *
     * @param uuid the UUID of the message.
     * @param sendChannel the channel to send the command to
     * @param payload the payload you want to send.
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     * @return
     */
    BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler);


    /**
     * Send a command payload to sendChannel with a message ID. Listens for a single response on returnChannel,
     * but only for a response with the same matching ID. Ideal for multi-message sessions where multiple consumers
     * are requesting at the same time on the same.
     * (defaults to sendChannel if left blank). The successHandler is used to
     * process incoming responses. It will stop processing any further responses after the first one.
     *
     * @param uuid the UUID of the message.
     * @param sendChannel the channel to send the command to
     * @param payload the payload you want to send.
     * @param returnChannel the return channel to listen for responses on (defaults to send channel)
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     String returnChannel,
                                     Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel with a message ID. Listens for a single response on returnChannel,
     * but only for a response with the same matching ID. Ideal for multi-message sessions where multiple consumers
     * are requesting at the same time on the same.
     * (defaults to sendChannel if left blank). The successHandler and errorHandler are used to
     * process incoming responses. They will stop processing any further responses after the first one.
     *
     * @param uuid the UUID of the message.
     * @param sendChannel the channel to send the command to
     * @param payload the payload you want to send.
     * @param returnChannel the return channel to listen for responses on (defaults to send channel)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     String returnChannel,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel with a message ID. Listens for a single response on returnChannel,
     * but only for a response with the same matching ID. Ideal for multi-message sessions where multiple consumers
     * are requesting at the same time on the same.
     * (defaults to sendChannel if left blank). The successHandler and errorHandler are used to
     * process incoming responses. They will stop processing any further responses after the first one.
     *
     * @param uuid the UUID of the message.
     * @param sendChannel the channel to send the command to
     * @param payload the payload you want to send.
     * @param returnChannel the return channel to listen for responses on (defaults to send channel)
     * @param from options name of the actor implementing (for logging)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     String returnChannel,
                                     String from,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel and listen for responses on the same channel.
     * Any additional responses will continue to be handled by the successHandler consumer.
     * The successHandler is used to process incoming responses. It will
     * continue to trigger with each new response, until BusTransaction is unsubscribed.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestStream(String sendChannel,
                                 Object payload,
                                 Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel and listen for responses on returnChannel (defaults
     * to sendChannel if left blank). Any additional responses will continue to be handled by the
     * successHandler consumer. The successHandler is used to process incoming responses. It will
     * continue to trigger with each new response, until BusTransaction is unsubscribed.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestStream(String sendChannel,
                                 Object payload,
                                 String returnChannel,
                                 Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel and listen for responses on returnChannel (defaults
     * to sendChannel if left blank). Any additional responses will continue to be handled by the
     * successHandler and errorHandler consumers. The success and error handlers are used to
     * process incoming responses. They will continue to trigger with each new response, until
     * BusTransaction is unsubscribed.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestStream(String sendChannel,
                                 Object payload,
                                 String returnChannel,
                                 Consumer<Message> successHandler,
                                 Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel and listen for responses on returnChannel (defaults to
     * sendChannel if left blank). Any additional responses will continue to be handled by the
     * successHandler and errorHandler consumers. The success and error handlers are used to process
     * incoming responses. They will continue to trigger with each new response, until BusTransaction
     * is unsubscribed.
     *
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param from optional name of the actor implementing (for logging)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestStream(String sendChannel,
                                 Object payload,
                                 String returnChannel,
                                 String from,
                                 Consumer<Message> successHandler,
                                 Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel with and ID and listen for responses (also with that ID)
     * on the same channel. Any additional responses will continue
     * to be handled by the successHandler consumer. The successHandler is used to process incoming
     * responses. It will continue to trigger with each new response, until BusTransaction
     * is unsubscribed.
     *
     * @param uuid UUID of the message, can also be used as a filter for incoming messages.
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestStreamWithId(UUID uuid,
                                       String sendChannel,
                                       Object payload,
                                       Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel with and ID and listen for responses (also with that ID)
     * on returnChannel (defaults to sendChannel if left blank). Any additional responses will continue
     * to be handled by the successHandler consumer. The successHandler is used to process incoming
     * responses. It will continue to trigger with each new response, until BusTransaction
     * is unsubscribed.
     *
     * @param uuid UUID of the message, can also be used as a filter for incoming messages.
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param successHandler handler which will be invoked in case of success response
     */
    BusTransaction requestStreamWithId(UUID uuid,
                                       String sendChannel,
                                       Object payload,
                                       String returnChannel,
                                       Consumer<Message> successHandler);

    /**
     * Send a command payload to sendChannel with and ID and listen for responses (also with that ID)
     * on returnChannel (defaults to sendChannel if left blank). Any additional responses will
     * continue to be handled by the successHandler and errorHandler consumers. The success and
     * error handlers are used to process incoming responses. They will continue to trigger with
     * each new response, until BusTransaction is unsubscribed.
     *
     * @param uuid UUID of the message, can also be used as a filter for incoming messages.
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestStreamWithId(UUID uuid,
                                       String sendChannel,
                                       Object payload,
                                       String returnChannel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler);

    /**
     * Send a command payload to sendChannel with and ID and listen for responses (also with that ID)
     * on returnChannel (defaults to sendChannel if left blank). Any additional responses will
     * continue to be handled by the successHandler and errorHandler consumers. The success and
     * error handlers are used to process incoming responses. They will continue to trigger with
     * each new response, until BusTransaction is unsubscribed.
     *
     * @param uuid UUID of the message, can also be used as a filter for incoming messages.
     * @param sendChannel the channel to send the initial command to
     * @param payload the payload to be sent as the command
     * @param returnChannel the return channel to listen for responses on (defaults to sendChannel)
     * @param from optional name of the actor implementing (for logging)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestStreamWithId(UUID uuid,
                                       String sendChannel,
                                       Object payload,
                                       String returnChannel,
                                       String from,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler);

    /**
     * Listen to a channel for all requests, continue handling requests until
     * the BusTransaction is closed.
     *
     * @param channel the channel to listen to for requests
     * @param successHandler handler which will be invoked for request messages
     */
    BusTransaction listenRequestStream(String channel,
                                       Consumer<Message> successHandler);

    /**
     * Listen to a channel for all requests, continue handling requests until
     * the BusTransaction is closed.
     *
     * @param channel the channel to listen to for requests
     * @param successHandler handler which will be invoked for request messages
     * @param errorHandler handler which will be invoked in case of error messages
     */
    BusTransaction listenRequestStream(String channel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler);

    /**
     * Listen to a channel for all requests, continue handling requests until
     * the BusTransaction is closed.
     *
     * @param channel the channel to listen to for requests
     * @param successHandler handler which will be invoked for request messages
     * @param errorHandler handler which will be invoked in case of error messages
     * @param id optional ID of message you're looking for, will filter out all other messages
     */
    BusTransaction listenRequestStream(String channel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler,
                                       UUID id);

    /**
     * Listen for a single request on a channel, process then automatically stop handling any more.
     *
     * @param channel the channel to listen to for requests
     * @param successHandler handler which will be invoked for request messages
     */
    BusTransaction listenRequestOnce(String channel,
                                     Consumer<Message> successHandler);

    /**
     * Listen for a single request on a channel, process then automatically stop handling any more.
     *
     * @param channel the channel to listen to for requests
     * @param successHandler handler which will be invoked for request messages
     * @param errorHandler handler which will be invoked in case of error messages
     */
    BusTransaction listenRequestOnce(String channel,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler);

    /**
     * Listen for a single request on a channel, process then automatically stop handling any more.
     *
     * @param channel the channel to listen to for requests
     * @param successHandler handler which will be invoked for request messages
     * @param errorHandler handler which will be invoked in case of error messages
     * @param id optional ID of message you're looking for, will filter out all other messages
     */
    BusTransaction listenRequestOnce(String channel,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler,
                                     UUID id);

    /**
     * Listen for all responses on a channel. Continue to handle responses until the BusTransaction is closed.
     *
     * @param channel the channel to listen to for responses.
     * @param successHandler handler which will be invoked for response messages
     */
    BusTransaction listenStream(String channel,
                                Consumer<Message> successHandler);

    /**
     * Listen for all responses on a channel. Continue to handle responses until the BusTransaction is closed.
     *
     * @param channel the channel to listen to for responses.
     * @param successHandler handler which will be invoked for response messages
     * @param errorHandler handler which will be invoked in case of error messages
     */
    BusTransaction listenStream(String channel,
                                Consumer<Message> successHandler,
                                Consumer<Message> errorHandler);

    /**
     * Listen for all responses on a channel. Continue to handle responses until the BusTransaction is closed.
     *
     * @param channel the channel to listen to for responses.
     * @param successHandler handler which will be invoked for response messages
     * @param errorHandler handler which will be invoked in case of error messages
     * @param id optional ID of message you're looking for, will filter out all other messages
     */
    BusTransaction listenStream(String channel,
                                Consumer<Message> successHandler,
                                Consumer<Message> errorHandler,
                                UUID id);

    /**
     * Listen for a single response on a channel, process then automatically stop handling any more.
     *
     * @param channel channel to listen to for responses.
     * @param successHandler handler which will be invoked for response messages
     */
    BusTransaction listenOnce(String channel,
                              Consumer<Message> successHandler);

    /**
     * Listen for a single response on a channel, process then automatically stop handling any more.
     *
     * @param channel channel to listen to for responses.
     * @param successHandler handler which will be invoked for response messages
     * @param errorHandler handler which will be invoked in case of error messages
     */
    BusTransaction listenOnce(String channel,
                              Consumer<Message> successHandler,
                              Consumer<Message> errorHandler);

    /**
     * Send error payload to channel.
     *
     * @param channel the channel to send the payload to
     * @param payload the payload to be send
     */
    void sendErrorMessage(String channel, Object payload);

    /**
     * Send error payload to channel.
     *
     * @param channel the channel to send the payload to
     * @param payload the payload to be send
     * @param headers optional message headers
     */
    void sendErrorMessage(String channel, Object payload, MessageHeaders headers);

    /**
     * Send error payload to channel, with an ID.
     *
     * @param channel the channel to send the payload to
     * @param payload the payload to be send
     * @param id the UUID for the response
     */
    void sendErrorMessageWithId(String channel, Object payload, UUID id);

    /**
     * Send error payload to channel, with an ID.
     *
     * @param channel the channel to send the payload to
     * @param payload the payload to be send
     * @param id the UUID for the response
     * @param headers optional message headers
     */
    void sendErrorMessageWithId(String channel, Object payload, UUID id, MessageHeaders headers);

    /**
     * Send error payload to channel for the specified user.
     *
     * @param channel the channel to send the payload to
     * @param payload the payload to be send
     * @param id the UUID for the response
     * @param targetUser target user name
     */
    void sendErrorMessageToTarget(String channel, Object payload, UUID id, String targetUser);

    /**
     * Send error payload to channel for the specified user.
     *
     * @param channel the channel to send the payload to
     * @param payload the payload to be send
     * @param id the UUID for the response
     * @param targetUser target user name
     * @param headers optional message headers
     */
    void sendErrorMessageToTarget(String channel, Object payload, UUID id, String targetUser,
                                  MessageHeaders headers);

    /**
     * Close a channel. If the closer is the last subscriber, then the channel is destroyed.
     *
     * @param channel channel you want to close
     * @param from optional calling actor (for logging)
     */
    void closeChannel(String channel, String from);

    /**
     * Create a new asynchronous transaction that can be composed of bus requests. It will fire
     * all requests at once and return once they return.
     */
    Transaction createTransaction();

    /**
     * Create a new synchronous or asynchronous transaction that can be composed of bus requests. Asynchronous
     * transactions will all fire at once and return once all requests return. Synchronous transactions will
     * fire in sequence and only proceed to the next transaction event once the preceding response has returned.
     *
     * @param type type of transaction you want, synchronous or asynchronous.
     */
    Transaction createTransaction(Transaction.TransactionType type);

    /**
     * Create a new synchronous or asynchronous transaction that can be composed of bus requests. Asynchronous
     * transactions will all fire at once and return once all requests return. Synchronous transactions will
     * fire in sequence and only proceed to the next transaction event once the preceding response has returned.
     *
     * @param type type of transaction you want, synchronous or asynchronous.
     * @param name optional name of the transaction, helps you track progress in the console (if enabled)
     */
    Transaction createTransaction(Transaction.TransactionType type, String name);

    /**
     * Create a new synchronous or asynchronous transaction that can be composed of bus requests. Asynchronous
     * transactions will all fire at once and return once all requests return. Synchronous transactions will
     * fire in sequence and only proceed to the next transaction event once the preceding response has returned.
     *
     * @param type type of transaction you want, synchronous or asynchronous.
     * @param name optional name of the transaction, helps you track progress in the console (if enabled)
     * @param id provide an overriding UUID for your transaction requests
     * @return
     */
    Transaction createTransaction(Transaction.TransactionType type, String name, UUID id);

    /**
     * Create a new synchronous or asynchronous transaction that can be composed of bus requests. Asynchronous
     * transactions will all fire at once and return once all requests return. Synchronous transactions will
     * fire in sequence and only proceed to the next transaction event once the preceding response has returned.
     *
     * @param type type of transaction you want, synchronous or asynchronous.
     * @param id provide an overriding UUID for your transaction requests
     * @return
     */
    Transaction createTransaction(Transaction.TransactionType type, UUID id);

   /**
    * Register a new MessageBrokerConnector which will be used to extend the EventBus.
    * @param messageBrokerConnector, a {@link MessageBrokerConnector} with unique id.
    *
    * @return true if the register operation was successful. Will return false if
    * a message broker connector with the same id was already registered.
    */
    boolean registerMessageBroker(MessageBrokerConnector messageBrokerConnector);

   /**
    * Unregisters a MessageBrokerConnector instance from the bus.
    * @param messageBrokerId, the unique id of the MessageBrokerConnector to be removed.
    * @return true if the unregister operation was successful.
    */
    boolean unregisterMessageBroker(String messageBrokerId);

    /**
     * Mark a channel as galactic. After the channel is marked as galactic,
     * all requests to it will be forwarded to the MessageBrokerConnector associated with it.
     *
     * The MessageBrokerConnector must be registered in the EventBus via the registerMessageBroker()
     * API before calling the markChannelAsGalactic() API.
     *
     * @param channel, the name of the galactic channel.
     * @param config, the galactic channel configuration.
     */
    boolean markChannelAsGalactic(String channel, GalacticChannelConfig config);

   /**
    * Remove the GalacticChannelConfig associated with the channel and
    * stop forwarding requests to the MessageBrokerConnector.
    *
    * @param channel, the galactic channel to be marked as local.
    */
    boolean markChannelAsLocal(String channel);

    /**
     * Checks if a given channel is marked as galactic.
     * @param channel, channel name to be checked
     * @return True if the channel has been marked as galactic.
     */
    boolean isGalacticChannel(String channel);
}

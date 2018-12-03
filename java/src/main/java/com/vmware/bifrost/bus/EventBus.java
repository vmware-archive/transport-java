/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.vmware.bifrost.bus.model.Message;
import io.reactivex.functions.Consumer;

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
     * @param schema the schema of the payload
     */
    void sendRequestMessage(String channel, Object payload, JsonSchema schema);

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
     * @param schema the schema of the payload
     */
    void sendResponseMessage(String channel, Object payload, JsonSchema schema);

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
     * Listen for a command on sendChannel and return a single response via the generateHandler() method.
     * The returned value will be sent as a response message on the return channel (defaults to
     * sendChannel if left blank). Once a single response has been sent, no more command messages
     * will be processed.
     *
     * @param sendChannel the channel to listen for requests
     * @param returnChannel the channel to send responses to (defaults to sendChannel if left blank
     * @param schema the schema of the response message payload.
     * @param generateHandler function for generating responses based on the incoming message.
     */
    BusTransaction respondOnce(String sendChannel,
                               String returnChannel,
                               JsonSchema schema,
                               Function<Message, Object> generateHandler);

    /**
     * Listen for requests on sendChannel and return responses via the generateHandler method.
     * The returned value will be sent as a response message on the return channel (defaults to
     * sendChannel if left blank). The responder will continue to stream responses to each
     * command until the BusTransaction.unsubscribe() method is called.
     *
     * @param sendChannel the channel to listen for requests.
     * @param returnChannel the channel to send responses to (defaults to sendChannel if left blank)
     * @param schema the schema of the response message payload.
     * @param generateHandler function for generating responses based on the incoming message.
     */
    BusTransaction respondStream(String sendChannel,
                                 String returnChannel,
                                 JsonSchema schema,
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
     * @param schema the schema of the payload.
     * @param from optional name of the actor implementing (for logging)
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestOnce(String sendChannel,
                               Object payload,
                               String returnChannel,
                               JsonSchema schema,
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
     * @param schema the schema of the payload.
     * @param successHandler handler which will be invoked in case of success response
     * @param errorHandler handler which will be invoked in case of error response
     */
    BusTransaction requestStream(String sendChannel,
                                 Object payload,
                                 String returnChannel,
                                 JsonSchema schema,
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
     * @param schema the schema of the request payload.
     * @param successHandler handler which will be invoked for request messages
     * @param errorHandler handler which will be invoked in case of error messages
     */
    BusTransaction listenRequestStream(String channel,
                                       JsonSchema schema,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler);

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
     * @param schema the schema of the response payload.
     * @param successHandler handler which will be invoked for response messages
     * @param errorHandler handler which will be invoked in case of error messages
     */
    BusTransaction listenStream(String channel,
                                JsonSchema schema,
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
     * @param schema the schema of the payload
     */
    void sendErrorMessage(String channel, Object payload, JsonSchema schema);

    /**
     * Close a channel. If the closer is the last subscriber, then the channel is destroyed.
     *
     * @param channel channel you want to close
     * @param from optional calling actor (for logging)
     */
    void closeChannel(String channel, String from);
}

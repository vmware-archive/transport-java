package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright(c) VMware Inc. 2017
 */
@SuppressWarnings("unchecked")
public class MessageHandlerImpl<T> implements MessageHandler<T> {

    private boolean requestStream;
    private MessagebusService bus;
    private MessageObjectHandlerConfig config;
    private Logger logger;
    private Observable<Message> channel;
    private Disposable sub;

    public MessageHandlerImpl(
            boolean requestStream, MessageObjectHandlerConfig config, MessagebusService bus) {
        this.requestStream = requestStream;
        this.config = config;
        this.bus = bus;
        logger = LoggerFactory.getLogger(this.getClass());

    }

    public Disposable handle(Consumer<Message> successHandler) {
        return this.handle(successHandler, null);
    }

    private Consumer<Message> createHandler(Consumer<Message> success) {
        return this.createHandler(success, null);
    }

    private Consumer<Message> createHandler(Consumer<Message> success, Consumer<Message> failure) {
        return (Message message) -> {
            if (!message.isError()) {
                if (success != null) {
                    success.accept(message);
                }
            } else {
                if (failure != null) {
                    failure.accept(message);
                }
            }
        };
    }

    public Disposable handle(Consumer<Message> successHandler, Consumer<Message> errorHandler) {
        if (this.requestStream) {
            this.channel = this.bus.getRequestChannel(this.config.getReturnChannel(), this.getClass().getName());
        } else {
            this.channel = this.bus.getResponseChannel(this.config.getReturnChannel(), this.getClass().getName());
        }
        if (this.config.isSingleResponse()) {
            if (errorHandler != null) {
                this.sub = this.channel.take(1).subscribe(this.createHandler(successHandler, errorHandler));
            } else {
                this.sub = this.channel.take(1).subscribe(this.createHandler(successHandler));
            }
        } else {
            if (errorHandler != null) {
                this.sub = this.channel.subscribe(this.createHandler(successHandler, errorHandler));
            } else {
                this.sub = this.channel.subscribe(this.createHandler(successHandler));
            }
        }
        return sub;
    }

    @Override
    public void tick(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendResponse(this.config.getReturnChannel(), payload);
        }
    }

    @Override
    public void close() {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.sub.dispose();
        }
    }

    @Override
    public boolean isClosed() {
        return (this.sub != null && this.sub.isDisposed());
    }

}

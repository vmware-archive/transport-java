package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import com.vmware.bifrost.bus.model.MessageType;
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
    private Observable<Message> errors;
    private Disposable sub;
    private Disposable errorSub;

    public MessageHandlerImpl(
            boolean requestStream, MessageObjectHandlerConfig config, MessagebusService bus) {
        this.requestStream = requestStream;
        this.config = config;
        this.bus = bus;
        logger = LoggerFactory.getLogger(this.getClass());

    }

    @Override
    public Disposable handle(Consumer<Message> successHandler) {
        return this.handle(successHandler, null);
    }

    private Consumer<Message> createHandler(Consumer<Message> consumer) {
        return (Message message) -> {
            if (consumer != null) {
                consumer.accept(message);
            }
        };
    }

    @Override
    public Disposable handle(Consumer<Message> successHandler, Consumer<Message> errorHandler) {
        if (this.requestStream) {
            this.channel = this.bus.getRequestChannel(this.config.getReturnChannel(), this.getClass().getName());
        } else {
            this.channel = this.bus.getResponseChannel(this.config.getReturnChannel(), this.getClass().getName());
        }
        this.errors = this.bus.getErrorChannel(this.config.getReturnChannel(), this.getClass().getName());
        if (this.config.isSingleResponse()) {
            this.sub = this.channel.take(1).subscribe(this.createHandler(successHandler));
            this.errorSub = this.errors.take(1).subscribe(this.createHandler(errorHandler));
        } else {
            this.sub = this.channel.subscribe(this.createHandler(successHandler));
            this.errorSub = this.errors.subscribe(this.createHandler(errorHandler));
        }
        return this.sub;
    }

    @Override
    public void tick(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendRequest(this.config.getSendChannel(), payload);
        }
    }

    @Override
    public void error(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendError(this.config.getSendChannel(), payload);
        }
    }

    @Override
    public void close() {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.sub.dispose();
        }
        if (this.errorSub != null && !this.errorSub.isDisposed()) {
            this.errorSub.dispose();
        }
    }

    @Override
    public boolean isClosed() {
        return (this.sub != null && this.sub.isDisposed());
    }

    @Override
    public Observable<T> getObservable(MessageType type) {
        Observable<Message> obs = this.bus.getChannel(this.config.getReturnChannel(), this.getClass().getName());

        if(type.equals(MessageType.MessageTypeRequest))
           obs = this.bus.getRequestChannel(this.config.getReturnChannel(), this.getClass().getName());

        if(type.equals(MessageType.MessageTypeError))
            obs = this.bus.getErrorChannel(this.config.getReturnChannel(), this.getClass().getName());

        if(type.equals(MessageType.MessageTypeResponse))
            obs = this.bus.getResponseChannel(this.config.getReturnChannel(), this.getClass().getName());

        return this.generateObservableFromPayload(obs);
    }

    @Override
    public Observable<T> getObservable() {
        final Observable<Message> obs = this.bus.getChannel(this.config.getReturnChannel(), this.getClass().getName());
        return this.generateObservableFromPayload(obs);
    }

    private Observable<T> generateObservableFromPayload(Observable<Message> obs) {
        return obs.map(
                (Message msg) -> {
                    T payload = (T) msg.getPayload();
                    if (msg.isError()) {
                        throw new Exception(payload.toString());
                    } else {
                        return payload;
                    }
                }
        );
    }

}

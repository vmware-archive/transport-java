package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageResponderImpl<T> implements MessageResponder<T> {
    private boolean requestStream;
    private MessagebusService bus;
    private MessageObjectHandlerConfig config;
    private Logger logger;
    private Observable<Message> channel;
    private Disposable sub;

    public MessageResponderImpl(
            boolean requestStream, MessageObjectHandlerConfig config, MessagebusService bus) {
        this.requestStream = requestStream;
        this.config = config;
        this.bus = bus;
        this.logger = LoggerFactory.getLogger(this.getClass());

    }

    private Consumer<Message> createGenerator(Function<Message, T> supplier) {
        return (Message message) -> {
            if (supplier != null) {
                this.bus.sendResponse(this.config.getReturnChannel(), supplier.apply(message));
            }
        };
    }

    public Disposable generate(Function<Message, T> generator) {
        this.channel = this.bus.getRequestChannel(this.config.getSendChannel(), this.getClass().getName());
        if (this.config.isSingleResponse()) {
            this.sub = this.channel.take(1).subscribe(this.createGenerator(generator));
        } else {
            this.sub = this.channel.subscribe(this.createGenerator(generator));
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
    public void error(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendError(this.config.getReturnChannel(), payload);
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

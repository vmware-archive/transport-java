package com.vmware.transport.bus;

import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.model.MessageObjectHandlerConfig;
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
    private EventBus bus;
    private MessageObjectHandlerConfig config;
    private Observable<Message> channel;
    private Disposable sub;

    public MessageResponderImpl(MessageObjectHandlerConfig config, EventBus bus) {
        this.config = config;
        this.bus = bus;
    }

    private Consumer<Message> createGenerator(Function<Message, T> supplier) {
        return (Message message) -> {
            if (supplier != null) {
                String returnChannel = this.config.getReturnChannel();
                T response = supplier.apply(message);
                if (message.getId() != null) {
                    this.bus.sendResponseMessageWithId(returnChannel, response, message.getId());
                } else {
                    this.bus.sendResponseMessage(returnChannel, response);
                }
            }
            if (this.config.isSingleResponse()) {
                this.bus.closeChannel(this.config.getSendChannel(), this.getClass().getName());
            }
        };
    }

    public Disposable generate(Function<Message, T> generator) {
        this.channel = this.bus.getApi().getRequestChannel(this.config.getSendChannel(), this.getClass().getName());
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
            this.bus.sendResponseMessage(this.config.getReturnChannel(), payload);
        }
    }

    @Override
    public void error(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendErrorMessage(this.config.getReturnChannel(), payload);
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

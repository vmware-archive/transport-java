package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import com.vmware.bifrost.bus.model.MessageType;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright(c) VMware Inc. 2017
 */
@SuppressWarnings("unchecked")
public class MessageHandlerImpl<T> implements MessageHandler<T> {

    private boolean requestStream;
    private EventBus bus;
    private MessageObjectHandlerConfig config;
    private Logger logger;
    private Observable<Message> channel;
    private Observable<Message> errors;
    private Disposable sub;
    private Disposable errorSub;
    private Consumer<Void> onClose;

    public MessageHandlerImpl(
          boolean requestStream,
          MessageObjectHandlerConfig config,
          EventBus bus) {
        this(requestStream, config, bus, null);
    }

    public MessageHandlerImpl(
            boolean requestStream,
            MessageObjectHandlerConfig config,
            EventBus bus,
            Consumer<Void> onClose) {
        this.requestStream = requestStream;
        this.config = config;
        this.bus = bus;
        this.onClose = onClose;
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
            if (this.config.isSingleResponse()) {
                this.bus.closeChannel(this.config.getReturnChannel(), this.getClass().getName());
                // Stop processing messages after first message/error.
                this.close();
            }
        };
    }

    @Override
    public Disposable handle(Consumer<Message> successHandler, Consumer<Message> errorHandler) {
        if (this.requestStream) {
            this.channel = this.bus.getApi().getRequestChannel(this.config.getReturnChannel(), this.getClass().getName());
        } else {
            this.channel = this.bus.getApi().getResponseChannel(this.config.getReturnChannel(), this.getClass().getName());
        }
        this.errors = this.bus.getApi().getErrorChannel(
              this.config.getReturnChannel(),
              this.getClass().getName(),
              // Add only one channel reference per MessageHandler instance
              true);

        Predicate<? super Message> filterByMessageId = (Message message) ->
            this.config.getId() == null || this.config.getId().equals(message.getId());

        if (this.config.isSingleResponse()) {
            this.sub = this.channel.filter(filterByMessageId).take(1).subscribe(this.createHandler(successHandler));
            this.errorSub = this.errors.filter(filterByMessageId).take(1).subscribe(this.createHandler(errorHandler));
        } else {
            this.sub = this.channel.filter(filterByMessageId).subscribe(this.createHandler(successHandler));
            this.errorSub = this.errors.filter(filterByMessageId).subscribe(this.createHandler(errorHandler));
        }
        return this.sub;
    }

    @Override
    public void tick(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendRequestMessage(this.config.getSendChannel(), payload);
        }
    }

    @Override
    public void error(T payload) {
        if (this.sub != null && !this.sub.isDisposed()) {
            this.bus.sendErrorMessage(this.config.getSendChannel(), payload);
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
        if (onClose != null) {
            try {
                onClose.accept(null);
            } catch (Exception ex) {
                logger.warn("OnClose handler failed: ", ex);
            }
            // Make sure that onClose handler will be invoked only once.
            onClose = null;
        }
    }

    @Override
    public boolean isClosed() {
        return (this.sub != null && this.sub.isDisposed());
    }

    @Override
    public Observable<T> getObservable(MessageType type) {
        Observable<Message> obs = this.bus.getApi().getChannel(this.config.getReturnChannel(), this.getClass().getName());

        if(type.equals(MessageType.MessageTypeRequest))
           obs = this.bus.getApi().getRequestChannel(this.config.getReturnChannel(), this.getClass().getName());

        if(type.equals(MessageType.MessageTypeError))
            obs = this.bus.getApi().getErrorChannel(this.config.getReturnChannel(), this.getClass().getName());

        if(type.equals(MessageType.MessageTypeResponse))
            obs = this.bus.getApi().getResponseChannel(this.config.getReturnChannel(), this.getClass().getName());

        return this.generateObservableFromPayload(obs);
    }

    @Override
    public Observable<T> getObservable() {
        final Observable<Message> obs = this.bus.getApi().getChannel(this.config.getReturnChannel(), this.getClass().getName());
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

package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageType;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/*
 * Copyright(c) VMware Inc. 2017
 */

/**
 * MessageHandler will handle incoming responses on the configured return channel.
 * @param <T> The Type of the payload you want to tick or error (only if using stream handlers)
 */
public interface MessageHandler<T> {

    /**
     * Handle an inbound response message sent on configured inbound return channel.
     * @param successHandler Successful (non error) handler. Consumer will always be passed a Message object.
     * @see Disposable
     * @return Disposable subscription, automatically disposed when using requestOnce()
     */
    Disposable handle(Consumer<Message> successHandler);

    /**
     * Handle and inbound response message sent on configured inbound return channel.
     * @param successHandler  Successful (non error) handler. Consumer will always be passed a Message object.
     * @param errorHandler Error handler. Consumer will always be passed a Message object.
     * @see Disposable
     * @return Disposable subscription, automatically disposed when using requestOnce()
     */
    Disposable handle(Consumer<Message> successHandler, Consumer<Message> errorHandler);

    /**
     * Send a new payload of type T to configured send channel (only for requestStream())
     * @param payload payload you want to send.
     */
    void tick(T payload);

    /**
     * Send a new error payload of type T to configured send channel (only for requestStream())
     * @param payload payload you want to send.
     */
    void error(T payload);

    /**
     * Close/Dispose of subscription, automatically closed when using requestOnce()
     */
    void close();

    /**
     * Checks if subscription has been disposed
     * @return has been disposed or not.
     */
    boolean isClosed();

    /**
     * Return a raw Observable
     * @param type filter message types being delivered via onNext()
     * @see MessageType
     * @see Observable
     * @return Observable that ticks onNext(T);
     */
    Observable<T> getObservable(MessageType type);

    /**
     * Return a raw Observable that ticks all message types via onNext(T)
     * @see Observable
     * @return Observable that ticks onNext(T)
     */
    Observable<T> getObservable();

}

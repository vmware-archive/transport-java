package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.function.Supplier;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageHandlerImpl implements MessageHandler {

    private Subscription sub;
    private boolean requestStream;
    private MessagebusService bus;
    private MessageObjectHandlerConfig config;
    private Logger logger;
    private Observable<Message> channel;

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

    public Disposable handle(Consumer<Message> successHandler, Consumer<Throwable> errorHandler) {
        if (this.requestStream) {
            this.channel = this.bus.getRequestChannel(this.config.getReturnChannel(), this.getClass().getName());
        } else {
            this.channel = this.bus.getResponseChannel(this.config.getReturnChannel(), this.getClass().getName());
        }
        Disposable sub;
        if(this.config.isSingleResponse()) {
            if(errorHandler != null) {
                sub = this.channel.take(1).subscribe(successHandler, errorHandler);
            } else {
                sub = this.channel.take(1).subscribe(successHandler);
            }
            return sub;
        } else {
            if(errorHandler != null) {
                return this.channel.subscribe(successHandler, errorHandler);
            } else {
                return this.channel.subscribe(successHandler);
            }
        }

    }

    @Override
    public void tick(Supplier<Object> payload) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }


//    },
//    tick: (payload: any): void => {
//        if (_sub && !_sub.closed) {
//            this.sendRequest(handlerConfig.returnChannel, payload);
//        }
//    },
//    close: (): boolean => {
//        if (!handlerConfig.singleResponse) {
//            _sub.unsubscribe();
//            _sub = null;
//        }
//        return true;
//    },
//    isClosed(): boolean {
//        if (!_sub || _sub.closed) {
//            return true;
//        }
//        return false;
//    }
}

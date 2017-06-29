package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.MessageType;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Copyright(c) VMware Inc. 2017
 */

@SuppressWarnings("unchecked")
public class BusHandlerTransaction<T> implements BusTransaction, AsObservable<T> {
    private Disposable sub;
    private MessageHandler<T> handler;

    BusHandlerTransaction(Disposable sub, MessageHandler handler) {
        this.sub = sub;
        this.handler = handler;
    }

    @Override
    public void unsubscribe() {
        if (this.handler != null) {
            this.handler.close();
        }
        if (this.sub != null) {
            this.sub.dispose();
        }
    }

    @Override
    public boolean isSubscribed() {
        return this.sub != null && !this.sub.isDisposed();
    }

    @Override
    public void tick(Object payload) {
        if (this.handler != null)
            this.handler.tick((T) payload);
    }

    @Override
    public void error(Object payload) {
        if (this.handler != null)
            this.handler.error((T) payload);
    }

    @Override
    public Observable<T> getObservable(MessageType type) {
        if (this.handler != null)
            return this.handler.getObservable(type);
        return null;
    }

    @Override
    public Observable<T> getObservable() {
        if (this.handler != null)
            return this.handler.getObservable();
        return null;
    }
}

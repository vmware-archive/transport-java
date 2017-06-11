package com.vmware.bifrost.bus;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class BusHandlerTransaction implements BusTransaction {
    private Disposable sub;
    private MessageHandler handler;
    private Logger logger;

    public BusHandlerTransaction(Disposable sub, MessageHandler handler) {
        this.sub = sub;
        this.handler = handler;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void unsubscribe() {
        if (this.handler != null) {
            this.handler.close();
        }
        if (this.sub != null) {
            this.sub.dispose();
        }
    }

    public boolean isSubscribed() {
        if (this.sub != null) {
            return !this.sub.isDisposed();
        }
        return false;
    }

    public void tick(Object payload) {
        if (this.handler != null) {
            this.handler.tick(payload);
        }
    }

    public void error(Object payload) {
        if (this.handler != null) {
            this.handler.error(payload);
        }
    }

}

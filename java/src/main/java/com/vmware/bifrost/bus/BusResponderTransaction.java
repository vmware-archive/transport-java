package com.vmware.bifrost.bus;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class BusResponderTransaction implements BusTransaction {

    private Disposable sub;
    private MessageResponder responder;
    private Logger logger;

    public BusResponderTransaction(Disposable sub, MessageResponder responder) {
        this.sub = sub;
        this.responder = responder;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void unsubscribe() {
        if (this.responder != null) {
            this.responder.close();
        }
        if (this.sub != null) {
            this.sub.dispose();
        }
    }

    public boolean isSubscribed() {
        if(this.sub != null) {
            return !this.sub.isDisposed();
        }
        return false;
    }

    public void tick(Object payload) {
        if(this.responder != null) {
            this.responder.tick(payload);
        }
    }

    public void error(Object payload) {
        if (this.responder != null) {
            this.responder.error(payload);
        }
    }
}
package com.vmware.bifrost.bus;

/*
 * Copyright(c) VMware Inc. 2017
 */

import com.vmware.bifrost.bus.model.MessageType;
import io.reactivex.Observable;

/**
 * Can return a raw Observable from a channel.
 */
public interface AsObservable<T> {

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

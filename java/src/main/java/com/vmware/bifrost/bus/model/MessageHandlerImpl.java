package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.Channel;
import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.Observable;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;

/**
 * Copyright(c) VMware Inc. 2017
 */
public abstract class MessageHandlerImpl implements MessageHandler {

    private Subscription sub;
    private boolean requestStream;
    private MessagebusService bus;
    private MessageObjectHandlerConfig config;

    public MessageHandlerImpl(
            boolean requestStream, MessageObjectHandlerConfig config, MessagebusService bus) {
        this.requestStream = requestStream;
        this.config = config;
        this.bus = bus;
    }

    public Subscription handle(Consumer success) {
        return null;

    }

    public Subscription handle(Consumer success, Consumer error) {
        //Channel chan = Observable<Message>;
        if(this.requestStream) {
            //Observable<Message> chan = this.bus.getRequestChannel(this.config)
        }
        return null;

    }



//    handle: (success: Function, error?: Function): Subscription => {
//        let _chan:Observable<Message> ;
//        if (requestStream) {
//            _chan = this.getRequestChannel(handlerConfig.returnChannel, name);
//        } else {
//            _chan = this.getResponseChannel(handlerConfig.returnChannel, name);
//        }
//        _sub = _chan.subscribe(
//                (msg: Message) => {
//            let _pl = msg.payload;
//            if (_pl.hasOwnProperty('_sendChannel')) {
//                _pl = msg.payload.body;
//            }
//            if (msg.isError()) {
//                error(_pl);
//            } else {
//                success(_pl);
//            }
//            if (handlerConfig.singleResponse) {
//                _sub.unsubscribe();
//            }
//        },
//        (data: any) => {
//            if (error) {
//                error(data);
//            }
//            _sub.unsubscribe();
//        }
//                );
//        return _sub;
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

import React, { useState } from 'react';
import { useBifrost } from '../../../../react-bifrost';
import { PongRequestType, PongServiceChannel, PongServiceRequest, PongServiceResponse } from '../ts/ping-component/pong.service.model';

export default function PingComponent() {

    const bifrost = useBifrost();
    const [ pingResponse, setPingResponse ] = useState<string>('');

    function sendPingBasic() {
        const request: PongServiceRequest = {
            command: PongRequestType.Basic,
            message: 'basic ping'
        };

        sendPingRequest(request);
    }

    function sendPingFull() {
        const request: PongServiceRequest = {
            command: PongRequestType.Full,
            message: 'full ping'
        };

        sendPingRequest(request);
    }

    function sendPingRequest(request: PongServiceRequest) {
        bifrost.bus.requestOnce(PongServiceChannel, request)
            .handle(
                (response: PongServiceResponse) => {
                    setPingResponse(response.value);
                }
            );
    }

    return (
        <div>
            <button onClick={() => sendPingBasic()} className='btn btn-primary'>Ping (Basic)</button>
            <button onClick={() => sendPingFull()} className='btn btn-primary'>Ping (Full)</button>
            <br/>
            Response: {pingResponse}
        </div>
    );
}

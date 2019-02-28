import React, { useState, useEffect } from 'react';
import { useBifrost } from '../../../../react-bifrost';

export default function HelloWorld(props: any) {
  const [message, setMessage] = useState('');
  const bifrost = useBifrost();

  // define a channel to talk on.
  const myChannel = 'some-channel';

  useEffect(() => {
    // listen for requests on 'myChannel' and return a response.
    bifrost.bus.respondOnce(myChannel).generate((request: string) => {
        bifrost.log.info(`Request Received: ${request}, Sending Response...`);
        return 'world';
    });

    bifrost.log.info('Sending Request');

    // send request 'hello' on channel 'myChannel'.
    bifrost.bus.requestOnce(myChannel, 'hello').handle((response: string) => {
        setMessage(`hello ${response}`);
    });
  }, []);

  return <div>{message}</div>;
}

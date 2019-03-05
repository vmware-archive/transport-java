import React, { useContext } from 'react';
import { AbstractCore } from '@vmw/bifrost/core';

export class Bifrost extends AbstractCore {}

export const BifrostContext = React.createContext<any>(null);

interface ProviderProps extends React.Props {
  bifrost: Bifrost;
}

export const Provider: React.FC<ProviderProps> = props => (
  <BifrostContext.Provider value={props.bifrost}>
    {props.children}
  </BifrostContext.Provider>
);

export const useBifrost = () => useContext(BifrostContext);

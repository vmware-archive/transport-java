export enum AppStores {
    Docs = 'docs',
    FabricConnection = 'stores::fabric-connection'
}

export enum FabricConnectionStoreKey {
    State = 'state',
}

export enum FabricConnectionState {
    Connected = 'connected',
    Disconnected = 'disconnected'
}

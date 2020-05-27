export enum AppStores {
    Docs = 'docs',
    FabricConnection = 'stores::fabric-connection',
    Versions = 'stores::versions'
}

export enum FabricConnectionStoreKey {
    State = 'state',
}

export enum FabricVersionState {
    JavaSet = 'java-set',
    UiSet = 'ui-set'
}

export enum FabricConnectionStoreState {
    ConnectionStateUpdate = 'connectionStateUpdate'
}

export const BIFROST_METRICS_SERVICE_CHANNEL = 'bifrost-metrics-service';
export const BIFROST_METADATA_SERVICE_CHANNEL = 'bifrost-metadata-service';
export const GET_TS_LIB_DOWNLOADS_COUNT = 'ts-lib-download-count';
export const GET_TS_LIB_METADATA = 'ts-lib-metadata';
export const GET_TS_LIB_LATEST_VERSION = 'ts-lib-latest-version';


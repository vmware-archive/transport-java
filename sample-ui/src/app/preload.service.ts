import { Injectable } from '@angular/core';
import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { LogLevel } from '@vmw/bifrost/log';
import { BusStore, EventBus } from '@vmw/bifrost';
import {
    AppStores,
    BIFROST_METADATA_SERVICE_CHANNEL,
    BIFROST_METRICS_SERVICE_CHANNEL, FabricConnectionStoreState,
    FabricVersionState
} from '../constants';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { RestService } from '@vmw/bifrost/core/services/rest/rest.service';
import { environment } from '@appfab/environments/environment';
import { getDefaultFabricConnectionString } from './shared/utils';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';

export function PreloadServiceFactory(preloadService: PreloadService) {
    return () => preloadService.initialize();
}

@Injectable({providedIn: 'root'})
export class PreloadService {
    private bus: EventBus;
    private docsStore: BusStore<boolean>;
    private versionStore: BusStore<string>;
    private fabricConnectionStore: BusStore<{[key: string]: any}>;

    initialize(): Promise<boolean> {
        // boot event bus.
        this.bus = BusUtil.bootBusWithOptions(LogLevel.Debug, false, true);
        this.bus.api.enableMonitorDump(true);
        this.configureStores();
        this.populateStores();
        this.loadServices();

        return this.connectFabric();
    }

    configureStores(): void {
        this.docsStore = this.bus.stores.createStore(AppStores.Docs);
        this.versionStore = this.bus.stores.createStore<string>(AppStores.Versions);
        this.fabricConnectionStore = this.bus.stores.createStore<{[key: string]: any}>(AppStores.FabricConnection);
    }

    populateStores(): void {
        this.docsStore.put('ts', false, null);
        this.docsStore.put('java', false, null);
        this.docsStore.put('sm', false, null);
    }

    loadServices(): void {
        ServiceLoader.addService(RestService);
    }

    listenForConnectionStateChange(): void {
        // set an initialization value
        this.fabricConnectionStore.put(
            'connectionState',
            {
                connected: false,
                connectionClass: 'label-purple',
                connectionState: 'Connecting to Fabric'
            }, FabricConnectionStoreState.ConnectionStateUpdate);

        const connectedStream = this.bus.fabric.whenConnectionStateChanges(getDefaultFabricConnectionString());
        connectedStream.subscribe(
            (stateChange: FabricConnectionState) => {
                switch (stateChange) {
                    case FabricConnectionState.Connected:
                        this.fabricConnectionStore.put(
                            'connectionState',
                            {
                                connected: true,
                                connectionClass: 'label-success',
                                connectionState: 'Connected to Fabric'
                            },
                            FabricConnectionStoreState.ConnectionStateUpdate);
                        break;

                    case FabricConnectionState.Disconnected:
                        this.fabricConnectionStore.put(
                            'connectionState',
                            {
                                connected: false,
                                connectionClass: 'label-purple',
                                connectionState: 'Disconnected from Fabric'
                            },
                            FabricConnectionStoreState.ConnectionStateUpdate);
                        break;
                    case FabricConnectionState.Failed:
                        this.fabricConnectionStore.put(
                            'connectionState',
                            {
                                connected: true,
                                connectionClass: 'label-danger',
                                connectionState: 'Unable to connect to Fabric'
                            },
                            FabricConnectionStoreState.ConnectionStateUpdate);
                        break;
                }
            }
        );
    }

    connectFabric(): Promise<boolean> {
        return new Promise((resolve, reject) => {
            // set listener for when connection state changes
            this.listenForConnectionStateChange();

            // function called when connected to fabric
            const connectedHandler = (sessionId: string) => {
                this.bus.logger.info(`Connected to Application Fabric with sessionId ${sessionId}`, 'main.ts');
                this.bus.markChannelAsGalactic(BIFROST_METRICS_SERVICE_CHANNEL);
                this.bus.markChannelAsGalactic(BIFROST_METADATA_SERVICE_CHANNEL);
                this.bus.fabric.getFabricVersion(getDefaultFabricConnectionString()).subscribe(
                    (version: string) => {
                        this.versionStore.put('java', version, FabricVersionState.JavaSet);
                    }
                );
                resolve();
            };

            // function called when disconnected.
            const disconnectedHandler = () => {
                this.bus.logger.info('Disconnected from Application Fabric.', 'main.ts');
            };

            this.bus.fabric.connect(
                connectedHandler,
                disconnectedHandler,
                environment.fabricConn.host,
                environment.fabricConn.port,
                environment.fabricConn.endpoint);
        });
    }
}


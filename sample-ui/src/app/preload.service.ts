import { Injectable } from '@angular/core';
import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { LogLevel } from '@vmw/bifrost/log';
import { BusStore, EventBus } from '@vmw/bifrost';
import {
    AppStores,
    BIFROST_METADATA_SERVICE_CHANNEL,
    BIFROST_METRICS_SERVICE_CHANNEL,
    FabricVersionState
} from '../constants';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { RestService } from '@vmw/bifrost/core/services/rest/rest.service';
import { environment } from '@appfab/environments/environment';
import { getDefaultFabricConnectionString } from './shared/utils';

export function PreloadServiceFactory(preloadService: PreloadService) {
    return () => preloadService.initialize();
}

@Injectable({providedIn: 'root'})
export class PreloadService {
    private bus: EventBus;
    private docsStore: BusStore<boolean>;
    private versionStore: BusStore<string>;

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
    }

    populateStores(): void {
        this.docsStore.put('ts', false, null);
        this.docsStore.put('java', false, null);
        this.docsStore.put('sm', false, null);
    }

    loadServices(): void {
        ServiceLoader.addService(RestService);
    }

    connectFabric(): Promise<boolean> {
        return new Promise((resolve, reject) => {
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

            this.bus.fabric.connect(connectedHandler, disconnectedHandler);
        });
    }
}


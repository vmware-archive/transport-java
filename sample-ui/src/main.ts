import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { LogLevel } from '@vmw/bifrost/log';
import { BusStore } from '@vmw/bifrost';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { RestService } from '@vmw/bifrost/core/services/rest/rest.service';
import { AppStores, FabricConnectionState, FabricConnectionStoreKey } from './constants';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

// boot event bus.
const bus = BusUtil.bootBusWithOptions(LogLevel.Info, false, true);

let docsStore: BusStore<boolean>;

// configure stores.
configureStores();
populateStores();
loadServices();
connectFabric();


function configureStores() {
    docsStore = bus.stores.createStore(AppStores.Docs);
}

function populateStores() {
    docsStore.put('ts', false, null);
    docsStore.put('java', false, null);
}
function loadServices() {
    ServiceLoader.addService(RestService);
}

function connectFabric() {

    // function called when connected to fabric
    const connectedHandler = (sessionId: string) => {
        bus.logger.info(`Connected to Application Fabric with sessionId ${sessionId}`, 'main.ts');
    };

    // function called when disconnected.
    const disconnectedHandler = () => {
        bus.logger.info('Disconnected from Application Fabric.', 'main.ts');
    };

    bus.fabric.connect(connectedHandler, disconnectedHandler);
}

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { LogLevel } from '@vmw/bifrost/log';
import { BusStore } from '@vmw/bifrost';
import { PongService } from './app/documentation/bifrost/sample-code/ts/ping-component/pong.service';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { RestService } from '@vmw/bifrost/core/services/rest/rest.service';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

// boot event bus.
const bus = BusUtil.bootBusWithOptions(LogLevel.Verbose, false, true);

let docsStore: BusStore<boolean>;

// configure stores.
configureStores();
populateStores();
loadServices();


function configureStores() {
    docsStore = bus.stores.createStore('docs');
}

function populateStores() {
    docsStore.put('ts', false, null);
    docsStore.put('java', false, null);
}
function loadServices() {
    ServiceLoader.addService(RestService);
}

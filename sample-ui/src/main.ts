import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { LogLevel } from '@vmw/bifrost/log';
import { BusStore } from '@vmw/bifrost';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

// boot event bus.
const bus = BusUtil.bootBusWithOptions(LogLevel.Debug, false);
let docsStore: BusStore<boolean>;

// configure stores.
configureStores();
populateStores();


function configureStores() {
    docsStore = bus.stores.createStore('docs');
}

function populateStores() {
    docsStore.put('ts', false, null);
    docsStore.put('java', false, null);
}

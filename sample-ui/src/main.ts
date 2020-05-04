import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { LogLevel } from '@vmw/bifrost/log';
import { BusStore } from '@vmw/bifrost';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { RestService } from '@vmw/bifrost/core/services/rest/rest.service';
import {
    AppStores,
    BIFROST_METADATA_SERVICE_CHANNEL,
    BIFROST_METRICS_SERVICE_CHANNEL,
    FabricVersionState
} from './constants';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

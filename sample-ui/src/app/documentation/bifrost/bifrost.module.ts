import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { BifrostRoutingModule } from './bifrost-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { TsImportingComponent } from './ts-importing/ts-importing.component';
import { TsConfiguringComponent } from './ts-configuring/ts-configuring.component';
import { TsConfiguringAngularComponent } from './ts-configuring-angular/ts-configuring-angular.component';
import { TsInitializingComponent } from './ts-initializing/ts-initializing.component';
import { TsHelloworldComponent } from './ts-helloworld/ts-helloworld.component';
import { JavaImportingComponent } from './java-importing/java-importing.component';
import { JavaConfiguringComponent } from './java-configuring/java-configuring.component';
import { JavaInitializingComponent } from './java-initializing/java-initializing.component';
import { JavaHelloworldComponent } from './java-helloworld/java-helloworld.component';
import { HelloWorldComponent } from './sample-code/ts/helloworld/helloworld.component';
import { TryItOutComponent } from './sample-code/tryitout/tryitout.component';
import { TsAbstractionsComponent } from './ts-abstractions/ts-abstractions.component';
import { TsServicesComponent } from './ts-services/ts-services.component';
import { TsCallingServicesComponent } from './ts-calling-services/ts-calling-services.component';
import { PingComponent } from './sample-code/ts/ping-component/ping.component';
import { TsBroadcastVsDirectComponent } from './ts-broadcast-vs-direct/ts-broadcast-vs-direct.component';
import { TsTransactionsComponent } from './ts-transactions/ts-transactions.component';
import { PingTransactionComponent } from './sample-code/ts/ping-component/ping-transaction.component';
import { TsRestServiceComponent } from './ts-restservice/ts-restservice.component';
import { MyAPIComponent } from './sample-code/ts/restservice/myapi.component';
import { ClarityModule } from '@clr/angular';

@NgModule({
    declarations: [
        BifrostHomeComponent,
        TsImportingComponent,
        TsConfiguringComponent,
        TsConfiguringAngularComponent,
        TsInitializingComponent,
        TsHelloworldComponent,
        JavaImportingComponent,
        JavaConfiguringComponent,
        JavaInitializingComponent,
        JavaHelloworldComponent,
        HelloWorldComponent,
        PingComponent,
        PingTransactionComponent,
        MyAPIComponent,
        TryItOutComponent,
        TsAbstractionsComponent,
        TsServicesComponent,
        TsCallingServicesComponent,
        TsBroadcastVsDirectComponent,
        TsTransactionsComponent,
        TsRestServiceComponent,
    ],
    imports: [
        CommonModule,
        SharedModule,
        ClarityModule,
        BifrostRoutingModule
    ]
})
export class BifrostModule {
}

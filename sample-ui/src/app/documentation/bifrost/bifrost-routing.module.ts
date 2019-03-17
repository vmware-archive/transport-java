import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { TsImportingComponent } from './ts-importing/ts-importing.component';
import { TsConfiguringComponent } from './ts-configuring/ts-configuring.component';
import { TsConfiguringAngularComponent } from './ts-configuring-angular/ts-configuring-angular.component';
import { TsInitializingComponent } from './ts-initializing/ts-initializing.component';
import { TsHelloworldComponent } from './ts-helloworld/ts-helloworld.component';
import { JavaImportingComponent } from './java-importing/java-importing.component';
import { JavaConfiguringComponent } from './java-configuring/java-configuring.component';
import { JavaInitializingComponent } from './java-initializing/java-initializing.component';
import { JavaHelloworldComponent } from './java-helloworld/java-helloworld.component';
import { TsAbstractionsComponent } from './ts-abstractions/ts-abstractions.component';
import { TsServicesComponent } from './ts-services/ts-services.component';
import { TsCallingServicesComponent } from './ts-calling-services/ts-calling-services.component';
import { TsBroadcastVsDirectComponent } from './ts-broadcast-vs-direct/ts-broadcast-vs-direct.component';
import { TsTransactionsComponent } from './ts-transactions/ts-transactions.component';
import { TsRestServiceComponent } from './ts-restservice/ts-restservice.component';
import { TsConnectingToFabricComponent } from './ts-connecting-to-fabric/ts-connecting-to-fabric.component';
import { TsGalacticConversationsComponent } from './ts-galactic-conversations/ts-galactic-conversations.component';
import { TsLoggingComponent } from './ts-logging/ts-logging.component';
import { TsStoreBasicsComponent } from './ts-store-basics/ts-store-basics.component';
import { TsStoreAdvancedComponent } from './ts-store-advanced/ts-store-advanced.component';
import { TsDistributedIframesComponent } from './ts-distributed-iframes/ts-distributed-iframes.component';
import { JavaAbstractionsComponent } from './java-abstractions/java-abstractions.component';
import { JavaServicesComponent } from './java-services/java-services.component';
import { JavaTransactionsComponent } from './java-transactions/java-transactions.component';
import { JavaMessagingComponent } from './java-messaging/java-messaging.component';

const bifrostRoutes: Routes = [
    {path: '', component: BifrostHomeComponent, pathMatch: 'true'},
    {path: 'ts/importing', component: TsImportingComponent, pathMatch: 'true'},
    {path: 'ts/configuring', component: TsConfiguringComponent, pathMatch: 'true'},
    {path: 'ts/configuring-angular', component: TsConfiguringAngularComponent, pathMatch: 'true'},
    {path: 'ts/initializing', component: TsInitializingComponent, pathMatch: 'true'},
    {path: 'ts/helloworld', component: TsHelloworldComponent, pathMatch: 'true'},
    {path: 'ts/abstractions', component: TsAbstractionsComponent, pathMatch: 'true'},
    {path: 'ts/services', component: TsServicesComponent, pathMatch: 'true'},
    {path: 'ts/calling-services', component: TsCallingServicesComponent, pathMatch: 'true'},
    {path: 'ts/broadcast-vs-direct', component: TsBroadcastVsDirectComponent, pathMatch: 'true'},
    {path: 'ts/transactions', component: TsTransactionsComponent, pathMatch: 'true'},
    {path: 'ts/logging', component: TsLoggingComponent, pathMatch: 'true'},
    {path: 'ts/store-basics', component: TsStoreBasicsComponent, pathMatch: 'true'},
    {path: 'ts/store-advanced', component: TsStoreAdvancedComponent, pathMatch: 'true'},
    {path: 'ts/making-rest-calls', component: TsRestServiceComponent, pathMatch: 'true'},
    {path: 'ts/connecting-to-fabric', component: TsConnectingToFabricComponent, pathMatch: 'true'},
    {path: 'ts/galactic-conversations', component: TsGalacticConversationsComponent, pathMatch: 'true'},
    {path: 'ts/distributed-iframes', component: TsDistributedIframesComponent, pathMatch: 'true'},
    {path: 'java/importing', component: JavaImportingComponent, pathMatch: 'true'},
    {path: 'java/configuring', component: JavaConfiguringComponent, pathMatch: 'true'},
    {path: 'java/initializing', component: JavaInitializingComponent, pathMatch: 'true'},
    {path: 'java/helloworld', component: JavaHelloworldComponent, pathMatch: 'true'},
    {path: 'java/messaging', component: JavaMessagingComponent, pathMatch: 'true'},
    {path: 'java/abstractions', component: JavaAbstractionsComponent, pathMatch: 'true'},
    {path: 'java/services', component: JavaServicesComponent, pathMatch: 'true'},
    {path: 'java/transactions', component: JavaTransactionsComponent, pathMatch: 'true'},
];

@NgModule({
    imports: [RouterModule.forChild(bifrostRoutes)],
    exports: [RouterModule]
})
export class BifrostRoutingModule {
}

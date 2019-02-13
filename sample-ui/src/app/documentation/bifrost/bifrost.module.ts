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
    ],
    imports: [
        CommonModule,
        SharedModule,
        BifrostRoutingModule
    ]
})
export class BifrostModule {
}

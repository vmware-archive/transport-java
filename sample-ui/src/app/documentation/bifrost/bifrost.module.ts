import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportingComponent } from './importing/importing.component';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { BifrostRoutingModule } from './bifrost-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { ConfiguringAngularComponent } from './configuring-angular/configuring-angular.component';
import { ConfiguringJavaComponent } from './configuring-java/configuring-java.component';
import { InitializingComponent } from './initializing/initializing.component';
import { HelloworldTsComponent } from './helloworld-ts/helloworld-ts.component';
import { HelloworldJavaComponent } from './helloworld-java/helloworld-java.component';

@NgModule({
    declarations: [
        ImportingComponent,
        BifrostHomeComponent,
        ConfiguringAngularComponent,
        ConfiguringJavaComponent,
        InitializingComponent,
        HelloworldTsComponent,
        HelloworldJavaComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        BifrostRoutingModule
    ]
})
export class BifrostModule {
}

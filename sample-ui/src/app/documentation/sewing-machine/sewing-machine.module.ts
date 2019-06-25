import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home/home.component';
import { SharedModule } from '../../shared/shared.module';
import { ClarityModule } from '@clr/angular';
import { SewingMachineRoutingModule } from './sewing-machine-routing.module';
import { QuickstartComponent } from './quickstart/quickstart.component';
import { InitializerComponent } from './initializer/initializer.component';
import { LibswaggerComponent } from './libswagger/libswagger.component';

@NgModule({
    declarations: [
        HomeComponent,
        QuickstartComponent,
        InitializerComponent,
        LibswaggerComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        ClarityModule,
        SewingMachineRoutingModule
    ]
})
export class SewingMachineModule {
}

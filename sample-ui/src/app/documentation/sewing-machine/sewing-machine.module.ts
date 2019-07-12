import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home/home.component';
import { SharedModule } from '../../shared/shared.module';
import { ClarityModule } from '@clr/angular';
import { SewingMachineRoutingModule } from './sewing-machine-routing.module';
import { QuickstartComponent } from './quickstart/quickstart.component';
import { InitializerComponent } from './initializer/initializer.component';
import { LibswaggerComponent } from './libswagger/libswagger.component';
import { ApigenComponent } from './apigen/apigen.component';
import { ApigenOutputComponent } from './apigen-output/apigen-output.component';
import { ServGenComponent } from './servgen/servgen.component';
import { JsonSchemaComponent } from './json-schema/json-schema.component';
import { ServGenOverviewComponent } from "./servgen-overview/servgen-overview.component";
import { ServGenServiceComponent } from "./servgen-service/servgen-service.component";

@NgModule({
    declarations: [
        HomeComponent,
        QuickstartComponent,
        InitializerComponent,
        LibswaggerComponent,
        ApigenComponent,
        ApigenOutputComponent,
        ServGenComponent,
        ServGenOverviewComponent,
        JsonSchemaComponent,
        ServGenServiceComponent
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

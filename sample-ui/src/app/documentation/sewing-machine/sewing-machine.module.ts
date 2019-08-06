import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home/home.component';
import { SharedModule } from '../../shared/shared.module';
import { ClarityModule } from '@clr/angular';
import { ServGenOverviewComponent } from './servgen-overview/servgen-overview.component';
import { ServGenServiceComponent } from './servgen-service/servgen-service.component';
import { ArraysJsonComponent } from './servgen-specs/arrays-json/arrays-json.component';
import { ClassJsonComponent } from './servgen-specs/class-json/class-json.component';
import { MapsJsonComponent } from './servgen-specs/maps-json/maps-json.component';
import { MocksJsonComponent } from './servgen-specs/mocks-json/mocks-json.component';
import { ServiceJsonComponent } from './servgen-specs/service-json/service-json.component';
import { SetsJsonComponent } from './servgen-specs/sets-json/sets-json.component';
import { TestingJsonComponent } from './servgen-specs/testing-json/testing-json.component';
import { SewingMachineRoutingModule } from './sewing-machine-routing.module';
import { QuickstartComponent } from './quickstart/quickstart.component';
import { InitializerComponent } from './initializer/initializer.component';
import { LibswaggerComponent } from './libswagger/libswagger.component';
import { ApigenComponent } from './apigen/apigen.component';
import { ApigenOutputComponent } from './apigen-output/apigen-output.component';
import { ServGenComponent } from './servgen/servgen.component';
import { JsonSchemaComponent } from './json-schema/json-schema.component';

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
        ServGenServiceComponent,
        ServiceJsonComponent,
        ClassJsonComponent,
        MapsJsonComponent,
        ArraysJsonComponent,
        SetsJsonComponent,
        MocksJsonComponent,
        TestingJsonComponent,
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

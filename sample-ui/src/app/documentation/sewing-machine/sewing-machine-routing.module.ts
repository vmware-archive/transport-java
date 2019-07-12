import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { QuickstartComponent } from './quickstart/quickstart.component';
import { LibswaggerComponent } from './libswagger/libswagger.component';
import { ApigenComponent } from './apigen/apigen.component';
import { ApigenOutputComponent } from './apigen-output/apigen-output.component';
import { ServGenOverviewComponent } from "./servgen-overview/servgen-overview.component";
import { ServGenServiceComponent } from "./servgen-service/servgen-service.component";
import { ServGenComponent } from './servgen/servgen.component';
import { JsonSchemaComponent } from './json-schema/json-schema.component';


const sewingMachineRoutes: Routes = [
    {path: '', component: HomeComponent, pathMatch: 'true'},
    {path: 'quickstart', component: QuickstartComponent, pathMatch: 'true'},
    {path: 'libswagger', component: LibswaggerComponent, pathMatch: 'true'},
    {path: 'apigen', component: ApigenComponent, pathMatch: 'true'},
    {path: 'apigen-output', component: ApigenOutputComponent, pathMatch: 'true'},
    {path: 'servgen', component: ServGenComponent, pathMatch: 'true'},
    {path: 'servgen-overview', component: ServGenOverviewComponent, pathMatch: 'true'},
    {path: 'json-schema', component: JsonSchemaComponent, pathMatch: 'true'},
    {path: 'servgen-service', component: ServGenServiceComponent, pathMatch: 'true'}
];

@NgModule({
    imports: [RouterModule.forChild(sewingMachineRoutes)],
    exports: [RouterModule]
})
export class SewingMachineRoutingModule {
}

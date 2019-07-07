import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { QuickstartComponent } from './quickstart/quickstart.component';
import { LibswaggerComponent } from './libswagger/libswagger.component';
import { ApigenComponent } from './apigen/apigen.component';
import { ApigenOutputComponent } from './apigen-output/apigen-output.component';
import { ServGenComponent } from './servgen/servgen.component';


const sewingMachineRoutes: Routes = [
    {path: '', component: HomeComponent, pathMatch: 'true'},
    {path: 'quickstart', component: QuickstartComponent, pathMatch: 'true'},
    {path: 'libswagger', component: LibswaggerComponent, pathMatch: 'true'},
    {path: 'apigen', component: ApigenComponent, pathMatch: 'true'},
    {path: 'apigen-output', component: ApigenOutputComponent, pathMatch: 'true'},
    {path: 'servgen', component: ServGenComponent, pathMatch: 'true'}
];

@NgModule({
    imports: [RouterModule.forChild(sewingMachineRoutes)],
    exports: [RouterModule]
})
export class SewingMachineRoutingModule {
}

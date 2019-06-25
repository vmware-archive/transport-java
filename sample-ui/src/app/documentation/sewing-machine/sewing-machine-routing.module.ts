import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { QuickstartComponent } from './quickstart/quickstart.component';
import { LibswaggerComponent } from './libswagger/libswagger.component';


const sewingMachineRoutes: Routes = [
    {path: '', component: HomeComponent, pathMatch: 'true'},
    {path: 'quickstart', component: QuickstartComponent, pathMatch: 'true'},
    {path: 'libswagger', component: LibswaggerComponent, pathMatch: 'true'},
];

@NgModule({
    imports: [RouterModule.forChild(sewingMachineRoutes)],
    exports: [RouterModule]
})
export class SewingMachineRoutingModule {
}

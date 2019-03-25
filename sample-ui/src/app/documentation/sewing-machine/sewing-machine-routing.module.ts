import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { QuickstartComponent } from './quickstart/quickstart.component';


const sewingMachineRoutes: Routes = [
    {path: '', component: HomeComponent, pathMatch: 'true'},
    {path: 'quickstart', component: QuickstartComponent, pathMatch: 'true'},
];

@NgModule({
    imports: [RouterModule.forChild(sewingMachineRoutes)],
    exports: [RouterModule]
})
export class SewingMachineRoutingModule {
}

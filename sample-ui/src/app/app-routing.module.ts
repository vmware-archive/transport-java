import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DemosComponent } from './demos/demos.component';
import { WelcomeComponent } from './welcome/welcome.component';

const routes: Routes = [
    {path: 'documentation', loadChildren: './documentation/documentation.module#DocumentationModule'},
    {path: 'demos', component: DemosComponent},
    {path: '', component: WelcomeComponent}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}

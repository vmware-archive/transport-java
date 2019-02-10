import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DemosComponent } from './demos/demos.component';
import { WelcomeComponent } from './welcome/welcome.component';

const routes: Routes = [
    { path: 'documentation', loadChildren: './documentation/documentation.module#DocumentationModule' },
    { path: 'demos', component: DemosComponent },
    { path: '', component: WelcomeComponent }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}

/**

 const routes: Routes = [
 {
        path: 'documentation', component: DocumentationComponent,
        children: [
            { path: 'home', component: HomeComponent },
            { path: 'getting-started', component: GettingStartedComponent },
            { path: 'bifrost', component: BifrostHomeComponent },
            { path: 'bifrost/importing', component: ImportingComponent },
            { path: '',   redirectTo: '/documentation/home', pathMatch: 'full' },

        ]
    },
 { path: 'demos', component: DemosComponent },
 { path: '', component: WelcomeComponent }
 ];
*/

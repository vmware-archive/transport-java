import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DocumentationComponent } from './documentation/documentation.component';
import { DemosComponent } from './demos/demos.component';
import { HomeComponent } from './documentation/home/home.component';
import { GettingStartedComponent } from './documentation/getting-started/getting-started.component';

const routes: Routes = [
    {
        path: 'documentation', component: DocumentationComponent,
        children: [
            { path: 'home', component: HomeComponent },
            { path: 'getting-started', component: GettingStartedComponent },
            { path: '',   redirectTo: '/documentation/home', pathMatch: 'full' },

        ]
    },
    { path: 'demos', component: DemosComponent }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}

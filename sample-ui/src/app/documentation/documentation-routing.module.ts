import { RouterModule, Routes } from '@angular/router';
import { DocumentationComponent } from './documentation.component';
import { NgModule } from '@angular/core';
import { GettingStartedComponent } from './getting-started/getting-started.component';
import { HomeComponent } from './home/home.component';

const documentationRoutes: Routes = [
    {
        path: '',
        component: DocumentationComponent,
        children: [
            { path: '', component: HomeComponent },
            { path: 'getting-started', component: GettingStartedComponent },
            { path: 'bifrost', loadChildren: './bifrost/bifrost.module#BifrostModule' }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(documentationRoutes)],
    exports: [RouterModule]
})
export class DocumentationRoutingModule {}

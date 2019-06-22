import { RouterModule, Routes } from '@angular/router';
import { DocumentationComponent } from './documentation.component';
import { NgModule } from '@angular/core';
import { GettingStartedComponent } from './getting-started/getting-started.component';
import { HomeComponent } from './home/home.component';
import { ContributorsComponent } from './contributors/contributors.component';
import { DevelopersComponent } from './developers/developers.component';

const documentationRoutes: Routes = [
    {
        path: '',
        component: DocumentationComponent,
        children: [
            { path: '', component: HomeComponent },
            { path: 'getting-started', component: GettingStartedComponent },
            { path: 'contributors', component: ContributorsComponent },
            { path: 'developers', component: DevelopersComponent },
            { path: 'bifrost', loadChildren: () => import('./bifrost/bifrost.module').then(m => m.BifrostModule) },
            { path: 'sewing-machine', loadChildren: () => import('./sewing-machine/sewing-machine.module').then(m => m.SewingMachineModule) }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(documentationRoutes)],
    exports: [RouterModule]
})
export class DocumentationRoutingModule {}

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GettingStartedComponent } from './getting-started/getting-started.component';
import { DocumentationRoutingModule } from './documentation-routing.module';
import { HomeComponent } from './home/home.component';
import { DocumentationComponent } from './documentation.component';
import { SharedModule } from '../shared/shared.module';
import { ClarityModule } from '@clr/angular';
import { ContributorsComponent } from './contributors/contributors.component';
import { DevelopersComponent } from './developers/developers.component';

@NgModule({
    declarations: [
        GettingStartedComponent,
        HomeComponent,
        DocumentationComponent,
        ContributorsComponent,
        DevelopersComponent
    ],
    imports: [
        CommonModule,
        ClarityModule,
        SharedModule,
        DocumentationRoutingModule
    ]
})
export class DocumentationModule {
}

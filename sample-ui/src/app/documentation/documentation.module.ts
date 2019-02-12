import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GettingStartedComponent } from './getting-started/getting-started.component';
import { DocumentationRoutingModule } from './documentation-routing.module';
import { HomeComponent } from './home/home.component';
import { DocumentationComponent } from './documentation.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
    declarations: [
        GettingStartedComponent,
        HomeComponent,
        DocumentationComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        DocumentationRoutingModule
    ]
})
export class DocumentationModule {
}

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClarityModule } from '@clr/angular';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DocumentationComponent } from './documentation/documentation.component';
import { DemosComponent } from './demos/demos.component';
import { DocumentationModule } from './documentation/documentation.module';

@NgModule({
    declarations: [
        AppComponent,
        DocumentationComponent,
        DemosComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        ClarityModule,
        BrowserAnimationsModule,
        DocumentationModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}

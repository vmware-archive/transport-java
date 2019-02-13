import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClarityModule } from '@clr/angular';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DemosComponent } from './demos/demos.component';
import { DocumentationModule } from './documentation/documentation.module';
import { WelcomeComponent } from './welcome/welcome.component';
import { SharedModule } from './shared/shared.module';

@NgModule({
    declarations: [
        AppComponent,
        DemosComponent,
        WelcomeComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        ClarityModule,
        BrowserAnimationsModule,
        DocumentationModule,
        SharedModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}

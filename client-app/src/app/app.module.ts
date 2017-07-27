import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {MainComponentComponent} from './main-component/main-component.component';

import {MessagebusService, StompService} from '@vmw/bifrost';

@NgModule({
    declarations: [
        AppComponent,
        MainComponentComponent
    ],
    imports: [
        BrowserModule
    ],
    providers: [
        MessagebusService,
        StompService
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}

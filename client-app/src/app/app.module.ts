import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {MainComponentComponent} from './main-component/main-component.component';

import {MessagebusService, StompService} from '@vmw/bifrost';
import {ClarityModule} from "clarity-angular";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ChartsModule} from "ng2-charts";
import { MetricsChartComponent } from './metrics-chart/metrics-chart.component';
import { TaskBoxComponent } from './task-box/task-box.component';


@NgModule({
    declarations: [
        AppComponent,
        MainComponentComponent,
        MetricsChartComponent,
        TaskBoxComponent,
    ],
    imports: [
        BrowserModule,
        ClarityModule.forRoot(),
        BrowserAnimationsModule,
        ChartsModule
    ],
    providers: [
        MessagebusService,
        StompService
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}

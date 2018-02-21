import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ClarityModule } from "@clr/angular";

import { AppComponent } from './app.component';
import { MainComponentComponent } from './main-component/main-component.component';
import { MetricsChartComponent } from './metrics-chart/metrics-chart.component';
import { TaskBoxComponent } from './task-box/task-box.component';
import { PeerListComponent } from './peer-list/peer-list.component';
import { MessagebusService, StompService } from '@vmw/bifrost';


@NgModule({
  declarations: [
    AppComponent,
    MainComponentComponent
  ],
  imports: [
    BrowserModule,
    ClarityModule
  ],
  providers: [
    MessagebusService,
    StompService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

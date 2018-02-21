import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ClarityModule } from '@clr/angular';
import { AppComponent } from './app.component';
import { MainComponentComponent } from './main-component/main-component.component';
import { PeerListComponent } from './peer-list/peer-list.component';
import { MessagebusService, StompService } from '@vmw/bifrost';
import { VmwComponentsModule } from '@vmw/ngx-components';


@NgModule({
  declarations: [
    AppComponent,
    MainComponentComponent
  ],
  imports: [
    BrowserModule,
    ClarityModule,
    VmwComponentsModule.forRoot(),
  ],
  providers: [
    MessagebusService,
    StompService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

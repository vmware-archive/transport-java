import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UnderstandingUiComponent } from './understanding-ui/understanding-ui.component';
import { WhatIsTheFabricComponent } from './what-is-the-fabric/what-is-the-fabric.component';
import { TechTalk1Component } from './tech-talk1/tech-talk1.component';
import { TechTalk2Component } from './tech-talk2/tech-talk2.component';
import { HomeComponent } from './home/home.component';
import { VideoRoutingModule } from './videos-routing.module';
import { VideosComponent } from './videos.component';
import { SharedModule } from '../shared/shared.module';
import { ClarityModule } from '@clr/angular';

@NgModule({
    declarations: [
        UnderstandingUiComponent,
        WhatIsTheFabricComponent,
        TechTalk1Component,
        TechTalk2Component,
        HomeComponent,
        VideosComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        ClarityModule,
        VideoRoutingModule
    ]
})
export class VideosModule {
}

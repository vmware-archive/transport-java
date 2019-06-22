import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { UnderstandingUiComponent } from './understanding-ui/understanding-ui.component';
import { WhatIsTheFabricComponent } from './what-is-the-fabric/what-is-the-fabric.component';
import { VideosComponent } from './videos.component';
import { HomeComponent } from './home/home.component';

const videoRoutes: Routes = [
    {
        path: '',
        component: VideosComponent,
        children: [
            {path: '', component: HomeComponent},
            {path: 'understanding-ui', component: UnderstandingUiComponent},
            {path: 'what-is-the-fabric', component: WhatIsTheFabricComponent}
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(videoRoutes)],
    exports: [RouterModule]
})
export class VideoRoutingModule {
}

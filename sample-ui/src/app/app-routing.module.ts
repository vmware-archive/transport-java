import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DemosComponent } from './demos/demos.component';
import { WelcomeComponent } from './welcome/welcome.component';
import { ChattyChatComponent } from './documentation/bifrost/sample-code/ts/chatty-chat/chatty-chat.component';
import { UnderstandingUiComponent } from './videos/understanding-ui/understanding-ui.component';
import { WhatIsTheFabricComponent } from './videos/what-is-the-fabric/what-is-the-fabric.component';

const routes: Routes = [
    {path: 'documentation', loadChildren: () => import('./documentation/documentation.module').then(m => m.DocumentationModule)},
    {path: 'videos',  loadChildren: () => import('./videos/videos.module').then(m => m.VideosModule)},
    {path: 'demos', component: DemosComponent},
    {path: '', component: WelcomeComponent},
    {path: 'chatty-chat', component: ChattyChatComponent}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}

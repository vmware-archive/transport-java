import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { ImportingComponent } from './importing/importing.component';
import { ConfiguringAngularComponent } from './configuring-angular/configuring-angular.component';
import { ConfiguringJavaComponent } from './configuring-java/configuring-java.component';
import { InitializingComponent } from './initializing/initializing.component';
import { HelloworldJavaComponent } from './helloworld-java/helloworld-java.component';
import { HelloworldTsComponent } from './helloworld-ts/helloworld-ts.component';

const bifrostRoutes: Routes = [
    {path: '', redirectTo: 'home'},
    {path: 'home', component: BifrostHomeComponent},
    {path: 'importing', component: ImportingComponent, pathMatch: 'full'},
    {path: 'configuring-angular', component: ConfiguringAngularComponent, pathMatch: 'full'},
    {path: 'configuring-java', component: ConfiguringJavaComponent, pathMatch: 'full'},
    {path: 'initializing', component: InitializingComponent, pathMatch: 'full'},
    {path: 'helloworld-java', component: HelloworldJavaComponent, pathMatch: 'full'},
    {path: 'helloworld-ts', component: HelloworldTsComponent, pathMatch: 'full'},

];

@NgModule({
    imports: [RouterModule.forChild(bifrostRoutes)],
    exports: [RouterModule]
})
export class BifrostRoutingModule {
}

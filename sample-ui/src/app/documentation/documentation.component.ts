import { Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from './bifrost/base.bifrost.component';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { PongService } from './bifrost/sample-code/ts/ping-component/pong.service';
import { MyAPIService } from './bifrost/sample-code/ts/restservice/myapi.service';

@Component({
    selector: 'appfab-documentation',
    templateUrl: './documentation.component.html',
    styleUrls: ['./documentation.component.scss']
})
export class DocumentationComponent extends BaseBifrostComponent implements OnInit {
    constructor() {
        super('DocumentationComponent');
    }

    ngOnInit() {
        //ServiceLoader.addService(PongService);
        ServiceLoader.addService(MyAPIService);
        // this.tsDocsActive = String(this.areBifrostTsDocsActive());
        // this.javaDocsActive = String(this.areBifrostJavaDocsActive());
        this.setBifrostTsDocsActive(true);
    }
}

import { Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from './bifrost/base.bifrost.component';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { MyAPIService } from './bifrost/sample-code/ts/restservice/myapi.service';
import { PongService } from './bifrost/sample-code/ts/ping-component/pong.service';

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
        const pongService: PongService = ServiceLoader.addService(PongService);
        pongService.offline();

        ServiceLoader.addService(MyAPIService);
        // this.tsDocsActive = String(this.areBifrostTsDocsActive());
        // this.javaDocsActive = String(this.areBifrostJavaDocsActive());
        this.setBifrostTsDocsActive(true);
    }
}

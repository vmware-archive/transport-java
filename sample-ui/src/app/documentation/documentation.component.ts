import { Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from './base.docs.component';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { MyAPIService } from './bifrost/sample-code/ts/restservice/myapi.service';
import { PongService } from './bifrost/sample-code/ts/ping-component/pong.service';

@Component({
    selector: 'appfab-documentation',
    templateUrl: './documentation.component.html',
    styleUrls: ['./documentation.component.scss']
})
export class DocumentationComponent extends BaseDocsComponent implements OnInit {
    constructor() {
        super('DocumentationComponent');
    }

    ngOnInit() {
        const pongService: PongService = ServiceLoader.addService(PongService);
        pongService.offline();

        ServiceLoader.addService(MyAPIService);
        this.setBifrostTsDocsActive(false);
        this.setBifrostTsDocsActive(false);
        this.setSewingMachineDocsActive(false);
    }
}

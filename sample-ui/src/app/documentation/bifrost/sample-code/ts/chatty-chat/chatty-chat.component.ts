import { Component, OnInit } from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';

@Component({
    selector: 'chatty-chat',
    templateUrl: './chatty-chat.component.html',
    styleUrls: ['./chatty-chat.component.scss']
})
export class ChattyChatComponent extends AbstractBase implements OnInit {

    constructor() {
        super('ChattyChatComponent');
    }

    ngOnInit(): void {
    }
}

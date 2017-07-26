import {Component, OnInit} from '@angular/core';
import {
    MessagebusService,
    StompConfig,
    StompClient,
    StompParser,
    StompChannel,
    StompBusCommand,
    MessageHandler
} from '@vmw/bifrost';


@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent implements OnInit {
    private config: StompConfig;
    private connectionStream: MessageHandler;

    constructor(private bus: MessagebusService) {
        this.config = StompConfig.generate('/bifrost', 'localhost', 8080);
    }

    ngOnInit() {

        const command = StompParser.generateStompBusCommand(
            StompClient.STOMP_CONNECT,
            "test",
            "password",
            this.config);

        this.listenForConnection();

        this.bus.sendRequestMessage(StompChannel.connection, command);
        console.log('sending command');

    }

    listenForConnection() {
        this.connectionStream = this.bus.listenStream(StompChannel.status);
        this.connectionStream.handle(
            (command: StompBusCommand) => {
                switch (command.command) {
                    case StompClient.STOMP_CONNECTED:
                        console.log('connected!');
                        break;

                }
            }
        );
    }

}

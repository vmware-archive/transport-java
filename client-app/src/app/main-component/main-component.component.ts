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
import {LogLevel} from "@vmw/bifrost/log";

@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent implements OnInit {
    private config: StompConfig;
    private connectionStream: MessageHandler;
    public metricsChannelA: string = "metrics-a";
    public metricsChannelB: string = "metrics-b";

    public taskChannelA: string = "task-a";
    public taskChannelB: string = "task-b";

    public taskTitleA: string = "Asynchronous Task A";
    public taskTitleB: string = "Asynchronous Task B";

    messages: Array<string>;

    constructor(private bus: MessagebusService) {
        this.config = StompConfig.generate(
            '/bifrost',
            'localhost',
            8080,
            false,
            "test",
            "password"
        );

        // this.bus.silenceLog(false);
        // this.bus.setLogLevel(LogLevel.Debug);
        // this.bus.suppressLog(false);
        // this.bus.enableMonitorDump(true);
    }

    ngOnInit() {

        const command =
            StompParser.generateStompBusCommand(StompClient.STOMP_CONNECT, "", "", this.config);

        this.listenForConnection();
        this.bus.sendRequestMessage(StompChannel.connection, command);
    }

    listenForConnection() {
        this.connectionStream = this.bus.listenStream(StompChannel.status);
        this.connectionStream.handle(
            (command: StompBusCommand) => {
                switch (command.command) {
                    case StompClient.STOMP_CONNECTED:
                        this.bus.sendResponseMessage('bridge-ready', true);
                        break;

                }
            }
        );
    }
}


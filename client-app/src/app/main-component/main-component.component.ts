import {Component, OnInit} from '@angular/core';
import {
    MessagebusService,
    StompConfig,
    StompClient,
    StompParser,
    StompChannel,
    StompBusCommand,
    MessageHandler, Message
} from '@vmw/bifrost';
import {Observable} from "rxjs/Observable";
import {Subscription} from "rxjs/Subscription";
import {LogLevel} from "@vmw/bifrost/log";



@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent implements OnInit {
    private config: StompConfig;
    private connectionStream: MessageHandler;

    private streamSub: Subscription;
    private requestTimer: any;

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

        this.bus.silenceLog(false);
        this.bus.setLogLevel(LogLevel.Debug);
        this.bus.suppressLog(false);
        this.bus.enableMonitorDump(true);

    }

    ngOnInit() {

        const command =
            StompParser.generateStompBusCommand(StompClient.STOMP_CONNECT, "", "", this.config);

        this.listenForConnection();

        this.bus.sendRequestMessage(StompChannel.connection, command);
        console.log('sending command');
        this.messages = [];


    }

    sendRandomRequests() {
        this.requestTimer = setInterval(
            () => {
               this.bus.sendGalacticMessage("/app/kitty", { name: "chappy"});

            },
            3000
        );
    }

    listenForIncomingMessages(): void {
        const stream: Observable<Message> = this.bus.getGalacticChannel("kitty", "pop");
        this.streamSub = stream.subscribe(
            (msg: Message) => {
                this.messages.push(msg.payload.name);
                console.log("socket talked! ", msg.payload.name);
            }
        );
    }


    listenForConnection() {
        this.connectionStream = this.bus.listenStream(StompChannel.status);
        this.connectionStream.handle(
            (command: StompBusCommand) => {
                switch (command.command) {
                    case StompClient.STOMP_CONNECTED:
                        this.listenForIncomingMessages();
                        this.sendRandomRequests();
                        break;

                }
            }
        );
    }

}

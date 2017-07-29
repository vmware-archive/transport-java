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

    private longTask: Subscription;


    private resultArray: Array<number>;

    private streamSub: Subscription;
    private requestTimer: any;

    public lineChartData:Array<any>;

    public lineChartLabels:Array<any> = ['January', 'February', 'March', 'April', 'May', 'June', 'July','pop','cap','chop','palm','fresh '];
    public lineChartOptions:any = {
        responsive: true,
        animation: {
            duration: 200,
            easing : 'easeInOutQuad'
        }

    };
    public lineChartColors:Array<any> = [
        { // grey
            backgroundColor: 'rgba(148,159,177,0.2)',
            borderColor: 'rgba(148,159,177,1)',
            pointBackgroundColor: 'rgba(148,159,177,1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(148,159,177,0.8)'
        },
        { // dark grey
            backgroundColor: 'rgba(77,83,96,0.2)',
            borderColor: 'rgba(77,83,96,1)',
            pointBackgroundColor: 'rgba(77,83,96,1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(77,83,96,1)'
        },
        { // grey
            backgroundColor: 'rgba(148,159,177,0.2)',
            borderColor: 'rgba(148,159,177,1)',
            pointBackgroundColor: 'rgba(148,159,177,1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(148,159,177,0.8)'
        }
    ];
    public lineChartLegend:boolean = true;
    public lineChartType:string = 'line';



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

    buildResultArray() {
        let arr: Array<number> = [];
        for(let x = 0; x < 12; x++) {
            arr.push(Math.floor(Math.random() * 10));
        }
        this.resultArray = arr;
        return arr;
    }

    popResultArray(val: number) {
        this.resultArray.push(val);
        this.resultArray.shift();
        return this.resultArray;
    }

    buildResults(val: number) {
        this.lineChartData  = [
            {data: this.popResultArray(val), label: 'Series A'}
        ];
        console.log(this.lineChartData);
    }

    ngOnInit() {

        const command =
            StompParser.generateStompBusCommand(StompClient.STOMP_CONNECT, "", "", this.config);

        this.listenForConnection();

        this.bus.sendRequestMessage(StompChannel.connection, command);
        console.log('sending command');
        this.messages = [];

        this.resultArray = [];


       this.lineChartData  = [
            {data: this.buildResultArray(), label: 'Series A'}
        ];


    }

    sendRandomRequests() {
        this.requestTimer = setInterval(
            () => {
               this.bus.sendGalacticMessage("/app/metrics", { name: "chappy"});

            },
            200
        );
    }

    listenForIncomingMessages(): void {
        const stream: Observable<Message> = this.bus.getGalacticChannel("metrics", "pop");
        this.streamSub = stream.subscribe(
            (msg: Message) => {
                //this.messages.push(msg.payload.value);
                //this.single = [{"name": "Chewy", "value": msg.payload.value}];
                console.log("socket talked! ", msg.payload.value);
                //this.resultArray.push(Math.floor(Math.random() * 10));
                //this.resultArray.pop();
                this.buildResults(msg.payload.value);
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

    private taskProgress: number = 0;
    private taskCategory: string;
    private taskLabel: string;

    requestTask() {

        const stream: Observable<Message> = this.bus.getGalacticChannel("process-task", "pop");
        this.longTask = stream.subscribe(
            (msg: Message) => {
                console.log('task update came in: ', msg.payload);
                this.taskProgress = msg.payload.completedState;
                this.taskCategory = msg.payload.category;
                this.taskLabel = msg.payload.task;

                if(msg.payload.taskStatus == "Finished") {
                    this.longTask.unsubscribe();
                }
            }
        );

        this.bus.sendGalacticMessage("/app/process-task", { command: "start"});
    }


}

interface LineMetric {
    name: string;
    value: number;
}

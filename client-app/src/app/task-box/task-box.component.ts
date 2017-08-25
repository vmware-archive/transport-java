import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Message, MessagebusService } from '@vmw/bifrost';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

@Component({
    selector: 'task-box',
    templateUrl: './task-box.component.html',
    styleUrls: ['./task-box.component.css']
})
export class TaskBoxComponent implements OnInit, OnDestroy {

    @Input() title: string;
    @Input() channel: string;

    private longTask: Subscription;
    private running: boolean = false;

    constructor(private bus: MessagebusService) {
    }

    ngOnInit() {
    }

    ngOnDestroy() {
        this.bus.close(this.channel, "task-box");
    }

    private taskProgress: number = 0;
    private taskCategory: string;
    private taskLabel: string;

    requestTask() {
        this.running = true;
        const stream: Observable<Message> = this.bus.getGalacticChannel(this.channel, "task-box");
        this.longTask = stream.subscribe(
            (msg: Message) => {
                this.taskProgress = msg.payload.completedState;
                this.taskCategory = msg.payload.category;
                this.taskLabel = msg.payload.task;

                if(msg.payload.taskStatus == "Finished") {
                    this.longTask.unsubscribe();
                    setTimeout(
                        () => {
                            this.running = false;
                        },
                        1000
                    );
                }
            }
        );

        this.bus.sendGalacticMessage("/app/" + this.channel, { command: "start"});
    }
}

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Message, MessagebusService, MessageHandler } from '@vmw/bifrost';
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

    private taskHandler: MessageHandler;
    private running: boolean = false;

    private taskProgress: number = 0;
    private taskCategory: string;
    private taskLabel: string;


    constructor(private bus: MessagebusService) {
    }

    ngOnInit() {
        this.bus.listenOnce('bridge-ready')
            .handle(
                () => {
                    this.listenToMetrics();
                }
            )
    }

    private listenToMetrics() {
        this.taskHandler = this.bus.listenGalacticStream(this.channel);
        this.taskHandler.handle(
            (task: Task) => {
                this.running = true;
                this.taskProgress = task.completedState;
                this.taskCategory = task.category;
                this.taskLabel = task.task;

                if(task.taskStatus == "Finished") {
                    this.taskHandler.close();
                    setTimeout(
                        () => {
                            this.running = false;
                        },
                        1000
                    );
                }
            }
        );
    }

    ngOnDestroy() {
        this.bus.close(this.channel, "task-box");
    }
    requestTask() {
        this.running = true;
        this.bus.sendGalacticMessage("/pub/" + this.channel, { command: "start"});
    }
}

interface Task {
    completedState: number;
    category: string;
    task;
    taskStatus: string;
}

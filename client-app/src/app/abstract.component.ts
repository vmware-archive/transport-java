import { EventBus } from "@vmw/bifrost";

export abstract class AbstractComponent {

    protected bus: EventBus;
    
    constructor() {
        this.bus = window.AppEventBus;
    }
}
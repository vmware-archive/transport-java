import { EventBus } from "@vmw/bifrost";
import { ServiceApi } from "./service.mixin";

export abstract class AbstractComponent extends ServiceApi {

    ngOnInit() {
        this.bus.connectBridge(
            () => {
                this.tellEveryoneTheBridgeIsReady();
            },
            '/bifrost',
            '/topic',
            '/queue',
            1,
            'localhost',
            8080,
            '/pub'
        );
    }

    constructor() {
        super();   
    }

    private tellEveryoneTheBridgeIsReady(): void {
        this.bus.sendResponseMessage('bridge-ready', true);
    }

}
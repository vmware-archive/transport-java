import { BusUtil } from '@vmw/bifrost/util/bus.util';
import { environment } from '@appfab/environments/environment';

export function getDefaultFabricConnectionString(): string {
    return BusUtil.getFabricConnectionString(
        environment.fabricConn.host, environment.fabricConn.port, environment.fabricConn.endpoint);
}

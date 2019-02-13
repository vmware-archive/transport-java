/**
 * Copyright(c) VMware Inc. 2018
 */

import { ChannelName } from '@vmw/bifrost';

export const MyAPIServiceChannel: ChannelName = 'services::MyAPIService';

export interface MyAPIServiceRequest {
    id: number;
}

export interface MyAPIServiceResponse {
    message: string;
}

/**
 * Copyright(c) VMware Inc. 2019
 */

import { ChannelName } from '@vmw/bifrost';

export const PongServiceChannel: ChannelName = 'services::PongService';

export enum PongRequestType {
    Basic,
    Full
}

export interface PongServiceRequest {
    command: PongRequestType;
    message: string;
}

export interface PongServiceResponse {
    value: string;
}

/**
 * Copyright(c) VMware Inc. 2019
 */

import { ChannelName } from '@vmw/bifrost';
import { AbstractMessageObject } from '@vmw/bifrost/core';

export const PongServiceChannel: ChannelName = 'services-PongService';

export enum PongRequestType {
    Basic = 'basic',
    Full = 'full'
}

export interface PongServiceRequest extends AbstractMessageObject<PongRequestType, string> {
    request: PongRequestType;
    payload: string;
}

export interface PongServiceResponse extends AbstractMessageObject<PongRequestType, string> {
    payload: string;
}

/**
 * Copyright(c) VMware Inc. 2019
 */

export interface CloudServicesPage {
    id:  string,
    name: string,
    time_zone: string,
    updated_at: string,
    url: string
}

export interface CloudServicesStatus {
    description: string,
    indicator: string
}

export interface CloudServicesStatusResponse {
    page: CloudServicesPage,
    status: CloudServicesStatus
}
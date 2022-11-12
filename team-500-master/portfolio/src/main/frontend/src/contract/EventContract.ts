import {BaseEventContract} from "./BaseEventContract";

export interface EventContract extends BaseEventContract {
    projectId: string
    eventId: string
    orderNumber: number
}
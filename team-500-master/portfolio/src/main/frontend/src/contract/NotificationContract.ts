import {BaseNotificationContract} from "./BaseNotificationContract";

export interface NotificationContract extends BaseNotificationContract {
    id: string,
    userId: number,
    timeNotified: Date,
    seen: boolean
}
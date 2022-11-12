import {BaseDeadlineContract} from "./BaseDeadlineContract";

export interface DeadlineContract extends BaseDeadlineContract {
    projectId: string
    deadlineId: string
    orderNumber: number
}
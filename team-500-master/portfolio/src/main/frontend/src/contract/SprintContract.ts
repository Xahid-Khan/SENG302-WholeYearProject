import {BaseSprintContract} from "./BaseSprintContract";

export interface SprintContract extends BaseSprintContract {
    projectId: string
    sprintId: string
    orderNumber: number
}
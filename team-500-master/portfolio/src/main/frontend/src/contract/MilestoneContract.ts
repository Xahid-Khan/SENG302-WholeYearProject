 import {BaseMilestoneContract} from "./BaseMilestoneContract";

 export interface MilestoneContract extends BaseMilestoneContract {
     projectId: string
     milestoneId: string
     orderNumber: number
 }
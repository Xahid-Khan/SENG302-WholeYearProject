import {BaseProjectContract} from "./BaseProjectContract";
import {SprintContract} from "./SprintContract";
import {EventContract} from "./EventContract";
import {MilestoneContract} from "./MilestoneContract";
import {DeadlineStore} from "../page/monthly_planner/store/DeadlineStore";
import {DeadlineContract} from "./DeadlineContract";

export interface ProjectContract extends BaseProjectContract {
    id: string
    sprints: SprintContract[]
    events: EventContract[]
    milestones: MilestoneContract[]
    deadlines : DeadlineContract[]
}
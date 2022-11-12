/**
 * Store for a single (already loaded) project.
 */
import {ProjectContract} from "../../../contract/ProjectContract";
import {action, computed, makeObservable, observable, reaction} from "mobx";
import {SprintStore} from "./SprintStore";
import {DatetimeUtils} from "../../../util/DatetimeUtils";
import {LoadingPending} from "../../../util/network/loading_status";
import {EventStore} from "./EventStore";
import {MilestoneStore} from "./MilestoneStore";
import {DeadlineStore} from "./DeadlineStore";


/**
 * Store that contains all of the client state for a Project, including all the sprints contained within it.
 *
 * When sprint orderNumbers change, the Project will automatically renumber the remaining sprints.
 */
export class ProjectStore {
    readonly startDate: Date
    readonly endDate: Date
    readonly name: string

    sprints: SprintStore[]
    events : EventStore[]
    milesStones : MilestoneStore[]
    deadlines : DeadlineStore[]

    constructor(project: ProjectContract) {
        makeObservable(this, {
            sprints: observable,
            events: observable,
            milesStones: observable,
            deadlines: observable,

            sprintsSaving: computed,

            renumberSprintsFromUpdate: action
        })

        this.startDate = DatetimeUtils.networkStringToLocalDate(project.startDate)
        this.endDate = DatetimeUtils.networkStringToLocalDate(project.endDate)
        this.name = project.name

        this.sprints = observable.array(project.sprints.map(sprint => {
            const sprintStore = new SprintStore(sprint)
            sprintStore.setOrderNumberUpdateCallback(() => this.renumberSprintsFromUpdate(sprintStore))
            return sprintStore
        }))

        this.events = observable.array(project.events.map(event => {
            const eventStore = new EventStore(event)
            return eventStore
        }))

        this.milesStones = observable.array(project.milestones.map(milestone => {
            const milestoneStore = new MilestoneStore(milestone)
            return milestoneStore
        }))

        this.deadlines = observable.array(project.deadlines.map(deadline => {
            const deadlineStore = new DeadlineStore(deadline)
            return deadlineStore
        }))
    }

    /**
     * Computed value that captures whether at least one sprint is being saved.
     */
    get sprintsSaving(): boolean {
        for (const sprint of this.sprints) {
            if (sprint.saveSprintStatus instanceof LoadingPending) {
                return true
            }
        }
        return false
    }

    /**
     * Renumber and re-order the sprints array given that sprint has changed its orderNumber.
     *
     * Assumes that the sprints array was in order before the orderNumber update occurred.
     *
     * @param updatedSprint reference to the sprint that has had its orderNumber updated
     */
    renumberSprintsFromUpdate(updatedSprint: SprintStore) {
        const previousIndex = this.sprints.indexOf(updatedSprint)
        this.sprints.splice(previousIndex, 1)

        const newIndex = updatedSprint.orderNumber - 1
        this.sprints.splice(newIndex, 0, updatedSprint)

        for (let i=0; i < this.sprints.length; i ++) {
            if (this.sprints[i].id !== updatedSprint.id) {
                this.sprints[i].orderNumber = i + 1
            }
        }
    }
}
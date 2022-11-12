/**
 * This stores a representation of a milestone
 * In the future this store will also handle update notification from the server
 */

import {LoadingNotYetAttempted, LoadingStatus} from "../../../util/network/loading_status";
import {MilestoneContract} from "../../../contract/MilestoneContract";
import {makeObservable, observable} from "mobx";
import {DatetimeUtils} from "../../../util/DatetimeUtils";

/**
 * Store that contains all the client state for a Milestone.
 */

export class MilestoneStore {
    readonly name: string
    readonly description: string
    readonly id : string

    protected onOrderNumberUpdate : VoidFunction | undefined

    saveMilestoneStatus : LoadingStatus = new LoadingNotYetAttempted()

    orderNumber : number
    startDate : Date
    endDate : Date

    constructor(milestone: MilestoneContract) {
        makeObservable(this, {
            orderNumber: observable,
            startDate : observable,
            endDate : observable
        })
        this.id = milestone.milestoneId
        this.name = milestone.name
        this.description = milestone.description
        this.startDate = DatetimeUtils.networkStringToLocalDate(milestone.startDate)
        this.endDate = DatetimeUtils.networkStringToLocalDate(milestone.endDate)
        this.orderNumber = milestone.orderNumber
    }
}
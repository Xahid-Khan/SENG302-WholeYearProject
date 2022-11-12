/**
 * This stores a representation of a deadline
 * In the future this store will also handle update notification from the server
 */

import {LoadingNotYetAttempted, LoadingStatus} from "../../../util/network/loading_status";
import {makeObservable, observable} from "mobx";
import {DatetimeUtils} from "../../../util/DatetimeUtils";
import {DeadlineContract} from "../../../contract/DeadlineContract";

/**
 * Store that contains all the client state for a DeadLine.
 */

export class DeadlineStore {
    readonly name: string
    readonly description: string
    readonly id : string

    protected onOrderNumberUpdate : VoidFunction | undefined

    saveDeadlineStatus : LoadingStatus = new LoadingNotYetAttempted()

    orderNumber : number
    startDate : Date
    endDate : Date

    constructor(deadline: DeadlineContract) {
        makeObservable(this, {
            orderNumber: observable,
            startDate : observable,
            endDate : observable
        })
        this.id = deadline.deadlineId
        this.name = deadline.name
        this.description = deadline.description
        this.startDate = DatetimeUtils.networkStringToLocalDate(deadline.startDate)
        this.endDate = DatetimeUtils.networkStringToLocalDate(deadline.endDate)
        this.orderNumber = deadline.orderNumber
    }
}
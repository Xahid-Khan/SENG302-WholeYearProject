/**
 * This stores a representation of an event
 * In the future this store will also handle update notification from the server
 */

import {LoadingNotYetAttempted, LoadingStatus} from "../../../util/network/loading_status";
import {EventContract} from "../../../contract/EventContract";
import {makeObservable, observable} from "mobx";
import {DatetimeUtils} from "../../../util/DatetimeUtils";

/**
 * Store that contains all the client state for an Event. Also triggers a callback function when orderNumber os an Event
 * changes.
 */

export class EventStore {
    readonly name: string
    readonly description: string
    readonly id: string

    protected onOrderNumberUpdate : VoidFunction | undefined

    saveEventStatus : LoadingStatus = new LoadingNotYetAttempted()

    orderNumber : number
    startDate : Date
    endDate : Date

    constructor(event: EventContract) {
        makeObservable(this, {
            orderNumber: observable,
            startDate : observable,
            endDate : observable
        })
        this.id = event.eventId
        this.name = event.name
        this.description = event.description
        this.startDate = DatetimeUtils.networkStringToLocalDate(event.startDate)
        this.endDate = DatetimeUtils.networkStringToLocalDate(event.endDate)
        this.orderNumber = event.orderNumber
    }
}
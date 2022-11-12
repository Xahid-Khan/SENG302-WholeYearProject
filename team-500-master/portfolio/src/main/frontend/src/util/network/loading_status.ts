/**
 * Classes for tracking the status of network requests (or other asynchronous processes).
 *
 * Inspired by: https://github.com/tfinlay/tankcosc/blob/6acac0f412a5067592da9fa82d1e46386345317e/frontend/src/LoadingStatus.js
 */

export abstract class LoadingStatus {

}

export class LoadingNotYetAttempted extends LoadingStatus {

}

export class LoadingPending extends LoadingStatus {

}

export class LoadingDone extends LoadingStatus {

}

export class LoadingError extends LoadingStatus {
    readonly error: unknown

    constructor(error: unknown) {
        super()
        this.error = error
    }
}
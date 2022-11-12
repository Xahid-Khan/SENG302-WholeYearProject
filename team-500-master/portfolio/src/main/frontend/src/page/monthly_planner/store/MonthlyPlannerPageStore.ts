import {ProjectStore} from "./ProjectStore";
import {
    LoadingDone,
    LoadingError,
    LoadingNotYetAttempted,
    LoadingPending,
    LoadingStatus
} from "../../../util/network/loading_status";
import {action, makeObservable, observable, runInAction} from "mobx";
import {handleErrorResponse} from "../../../util/network/network_error_handler";


/**
 * Store for the MonthlyPlannerPage. Handles loading of the Project for the page, but in future this may also contain
 * other page-wide state.
 *
 * The ID of the page's project is parsed from the last segment of the page's path
 * (e.g. `/monthly-planner/5004ef2b-0e75-4481-b710-4968335cc53e`)
 */
export class MonthlyPlannerPageStore {
    readonly projectId: string | undefined

    project: ProjectStore | undefined
    projectLoadingStatus: LoadingStatus = new LoadingNotYetAttempted()

    constructor() {
        makeObservable(this, {
            project: observable,
            projectLoadingStatus: observable,

            fetchProject: action
        })

        try {
            this.projectId = MonthlyPlannerPageStore.parseProjectIdFromUrl()
        }
        catch (e) {
            this.projectLoadingStatus = new LoadingError(e)
            return
        }

        this.fetchProject().catch(()=>{})  // Explicitly ignore errors here. Errors will be stored in projectLoadingStatus
    }

    static parseProjectIdFromUrl() {
        const splitPath = window.location.pathname.split("/")
        return splitPath[splitPath.length - 1]
    }

    /**
     * Fetch the data of the project for this page, updating the `projectLoadingStatus` as appropriate.
     */
    async fetchProject() {
        if (this.projectId === undefined || this.projectLoadingStatus instanceof LoadingPending) {
            return
        }

        this.projectLoadingStatus = new LoadingPending()
        this.project = undefined

        try {
            const res = await fetch(`../api/v1/projects/${this.projectId}`)

            if (!res.ok) {
                await handleErrorResponse(res, {
                    primaryContext: "fetch project",
                    secondaryContext: "Please try again later."
                })
            }

            const data = await res.json()
            runInAction(() => {
                this.projectLoadingStatus = new LoadingDone()
                this.project = new ProjectStore(data)
            })
        }
        catch (e) {
            runInAction(() => {
                this.projectLoadingStatus = new LoadingError(e)
            })
            throw e
        }
    }
}


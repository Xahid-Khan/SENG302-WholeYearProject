/**
 * Handles switching between edit and view screens.
 */
class Sprint {
    constructor(containerElement, data, project, deleteCallback, sprintUpdateSavedCallback) {
        this.containerElement = containerElement;
        this.project = project;
        this.sprint = data;
        this.sprintUpdateSavedCallback = sprintUpdateSavedCallback;
        this.deleteCallback = deleteCallback;
        this.updateSprintLoadingStatus = LoadingStatus.NotYetAttempted;

        this.currentView = null;
        this.showViewer();
    }

    /**
     * Updates sprint according to newValue attributes.
     * @param newValue
     */
    async updateSprint(newValue) {
        if (this.updateSprintLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.updateSprintLoadingStatus = LoadingStatus.Pending;

        try {
            const response = await fetch(`api/v1/sprints/${this.sprint.sprintId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newValue)
            })

            if (!response.ok) {
                await ErrorHandlerUtils.handleNetworkError(response, "update sprint");
            }

            const newSprint = await response.json();
            this.sprintUpdateSavedCallback({
                ...newSprint,
                startDate: DatetimeUtils.networkStringToLocalDate(newSprint.startDate),
                endDate: DatetimeUtils.networkStringToLocalDate(newSprint.endDate),
                colour: newSprint.colour
            });
        }
        catch (ex) {
            this.updateSprintLoadingStatus = LoadingStatus.Error;

            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }

            ErrorHandlerUtils.handleUnknownNetworkError(ex, "update sprint");
        }
    }

    /**
     * Shows sprint editing view.
     */
    showEditor() {
        this.currentView?.dispose();
        this.currentView = new Editor(
            this.containerElement,
            "Edit sprint details:",
            this.sprint,
            this.showViewer.bind(this),
            this.updateSprint.bind(this),
            Editor.makeProjectSprintDatesValidator(this.project, this.sprint.sprintId),
            this.project
        );
    }

    /**
     * Refreshes view, disposing of the previous view and reloading it.
     */
    showViewer() {
        this.currentView?.dispose();
        this.currentView = new SprintView(this.containerElement, this.project.events, this.project.deadlines, this.project.milestones, this.project.sprints, this.sprint, this.deleteSprint.bind(this), this.showEditor.bind(this));
    }

    /**
     * Gets the sprint to explicitly destroy itself prior
     */
    dispose() {
        this.currentView.dispose();
    }

    /**
     * Handles deletion of sprint when making DELETE request.
     */
    async deleteSprint() {
        if (this.deleteLoadingStatus === LoadingStatus.Pending) {
            return;
        }
        this.deleteLoadingStatus = LoadingStatus.Pending;

        try {
            const response = await fetch(`api/v1/sprints/${this.sprint.sprintId}`, {
                method: 'DELETE'
            })
            if (!response.ok) {
                await ErrorHandlerUtils.handleNetworkError(response, "delete sprint");
            }

            this.deleteLoadingStatus = LoadingStatus.Done;
            this.deleteCallback(this.sprint.sprintId);
        } catch (ex) {
            this.deleteLoadingStatus = LoadingStatus.Error;

            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }

            ErrorHandlerUtils.handleUnknownNetworkError(ex, "delete sprint");
        }
    }
}
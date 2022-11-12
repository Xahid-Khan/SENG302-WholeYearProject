class EventObject {
    constructor(containerElement, data, project, deleteCallback, eventUpdateSavedCallback) {
        this.containerElement = containerElement;
        this.project = project;
        this.event = data;
        this.eventUpdateSavedCallback = eventUpdateSavedCallback;
        this.deleteCallback = deleteCallback;
        this.updateEventLoadingStatus = LoadingStatus.NotYetAttempted;

        this.currentView = null;
        this.showViewer();
    }

    /**
     * Updates event according to newValue attributes.
     * @param newValue
     */
    async updateEvent(newValue) {
        if (this.updateEventLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.updateEventLoadingStatus = LoadingStatus.Pending;

        try {
            const response = await fetch(`api/v1/events/${this.event.eventId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newValue)
            })

            if (!response.ok) {
                await ErrorHandlerUtils.handleNetworkError(response, "update event");
            }

            const newEvent = await response.json();
            this.eventUpdateSavedCallback({
                ...newEvent,
                startDate: DatetimeUtils.networkStringToLocalDate(newEvent.startDate),
                endDate: DatetimeUtils.networkStringToLocalDate(newEvent.endDate)
            });
        }
        catch (ex) {
            this.updateEventLoadingStatus = LoadingStatus.Error;

            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }

            ErrorHandlerUtils.handleUnknownNetworkError(ex, "update event");
        }
    }

    /**
     * Shows event editing view.
     */
    showEditor() {
        this.currentView?.dispose();
        this.currentView = new Editor(
            this.containerElement,
            "Edit event details:",
            this.event,
            this.showViewer.bind(this),
            this.updateEvent.bind(this),
            Editor.makeProjectEventDatesValidator(this.project),
            this.project,
            true
        );
    }

    /**
     * Refreshes view, disposing of the previous view and reloading it.
     */
    showViewer() {
        this.currentView?.dispose();
        this.currentView = new EventView(this.containerElement, this.project.sprints, this.event, this.deleteEvent.bind(this), this.showEditor.bind(this));
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
    async deleteEvent() {
        if (this.deleteLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.deleteLoadingStatus = LoadingStatus.Pending;

        try {
            const response = await fetch(`api/v1/events/${this.event.eventId}`, {
                method: 'DELETE'
            })
            if (!response.ok) {
                await ErrorHandlerUtils.handleNetworkError(response, "delete event");
            }

            this.deleteLoadingStatus = LoadingStatus.Done;
            this.deleteCallback(this.event.eventId);
        } catch (ex) {
            this.deleteLoadingStatus = LoadingStatus.Error;

            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }

            ErrorHandlerUtils.handleUnknownNetworkError(ex, "delete event");
        }
    }
}
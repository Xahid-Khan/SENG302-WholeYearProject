/**
 * Handles switching between the editor and view screens.
 */
class Project {
    constructor(containerElement, data, deleteCallback, createCallback) {
        this.containerElement = containerElement;
        this.project = data;

        this.currentView = null;
        this.showViewer();

        this.updateLoadingStatus = LoadingStatus.NotYetAttempted;

        this.deleteLoadingStatus = LoadingStatus.NotYetAttempted;
        this.deleteCallback = deleteCallback;

        this.createCallback = createCallback;

        this.currentlyEditing = false;
    }

    /**
     * Called when a sprint is updated or a new sprint is created within this project.
     *
     * Since sprints are ordered by orderNumber (derived from startDate), the dates and thus orderNumbers may have changed.
     * This method updates the orderNumbers and moves the new sprints into the correct new order.
     *
     * @param sprint to update or insert
     */
    onSprintUpdate(sprint) {
        // Delete the outdated sprint from the sprints array.
        // NB: Since this method is sometimes called with new sprints, a deletion is not guaranteed to occur here.
        for (let i=0; i < this.project.sprints.length; i++) {
            if (this.project.sprints[i].sprintId === sprint.sprintId) {
                this.project.sprints.splice(i, 1);
                break;
            }
        }

        // Insert the updated sprint.
        this.project.sprints.splice(sprint.orderNumber - 1, 0, sprint);

        // Update the orderNumbers of sprints after this one in the list.
        for (let i=sprint.orderNumber; i < this.project.sprints.length; i++) {
            this.project.sprints[i].orderNumber ++;
        }

        const showingEvents = this.currentView.showingEvents;
        const showingMilestones = this.currentView.showingMilestones;
        const showingDeadlines = this.currentView.showingDeadlines

        // Refresh the view
        this.showViewer();

        if (showingEvents) {
            this.currentView.toggleEvents();
        }
        if (showingMilestones) {
            this.currentView.toggleMilestones();
        }
        if (showingDeadlines) {
            this.currentView.toggleDeadlines()
        }
        this.currentView.toggleSprints();
    }

    onEventUpdate(event) {
        // Delete the outdated event from the events array.
        // NB: Since this method is sometimes called with new events, a deletion is not guaranteed to occur here.
        for (let j=0; j < this.project.events.length; j++) {
            if (this.project.events[j].eventId === event.eventId) {
                this.project.events.splice(j, 1);
                break;
            }
        }

        // Insert the updated event.
        this.project.events.splice(event.orderNumber - 1, 0, event);

        // Update the orderNumbers of events after this one in the list.
        for (let j=event.orderNumber; j < this.project.events.length; j++) {
            this.project.events[j].orderNumber ++;
        }

        const showingSprints = this.currentView.showingSprints;
        const showingMilestones = this.currentView.showingMilestones;
        const showingDeadlines = this.currentView.showingDeadlines;

        // Refresh the view
        this.showViewer();
        if (showingSprints) {
            this.currentView.toggleSprints();
        }
        if (showingMilestones) {
            this.currentView.toggleMilestones();
        }
        if (showingDeadlines) {
            this.currentView.toggleDeadlines()
        }
        this.currentView.toggleEvents();
    }

    onMilestoneUpdate(milestone) {
        // Delete the outdated event from the milestones array.
        // NB: Since this method is sometimes called with new milestones, a deletion is not guaranteed to occur here.
        for (let j=0; j < this.project.milestones.length; j++) {
            if (this.project.milestones[j].milestoneId === milestone.milestoneId) {
                this.project.milestones.splice(j, 1);
                break;
            }
        }
        // Insert the updated milestone.
        this.project.milestones.splice(milestone.orderNumber - 1, 0, milestone);

        // Update the orderNumbers of milestones after this one in the list.
        for (let j=milestone.orderNumber; j < this.project.milestones.length; j++) {
            this.project.milestones[j].orderNumber ++;
        }

        const showingSprints = this.currentView.showingSprints;
        const showingEvents = this.currentView.showingEvents;
        const showingDeadlines = this.currentView.showingDeadlines;

        // Refresh the view
        this.showViewer();
        if (showingSprints) {
            this.currentView.toggleSprints();
        }
        if (showingEvents) {
            this.currentView.toggleEvents();
        }
        if (showingDeadlines) {
            this.currentView.toggleDeadlines()
        }
        this.currentView.toggleMilestones();
    }

    onDeadlineUpdate(deadline) {
        // Delete the outdated event from the events array.
        // NB: Since this method is sometimes called with new events, a deletion is not guaranteed to occur here.
        for (let j=0; j < this.project.deadlines.length; j++) {
            if (this.project.deadlines[j].deadlineId === deadline.deadlineId) {
                this.project.deadlines.splice(j, 1);
                break;
            }
        }

        // Insert the updated event.
        this.project.deadlines.splice(deadline.orderNumber - 1, 0, deadline);

        // Update the orderNumbers of events after this one in the list.
        for (let j=deadline.orderNumber; j < this.project.deadlines.length; j++) {
            this.project.deadlines[j].orderNumber ++;
        }

        const showingSprints = this.currentView.showingSprints;
        const showingMilestones = this.currentView.showingMilestones;
        const showingEvents = this.currentView.showingEvents;

        // Refresh the view
        this.showViewer();
        if (showingSprints) {
            this.currentView.toggleSprints();
        }
        if (showingMilestones) {
            this.currentView.toggleMilestones();
        }
        if (showingEvents) {
            this.currentView.toggleEvents();
        }
        this.currentView.toggleDeadlines();
    }

    onEventEdit() {
        this.currentView.deadlinesContainer.style.display = "none";
        this.currentView.milestonesContainer.style.display = "none";
    }

    onDeadlineEdit() {
        this.currentView.eventsContainer.style.display = "none";
        this.currentView.milestonesContainer.style.display = "none";
    }

    onMilestoneEdit() {
        this.currentView.deadlinesContainer.style.display = "none";
        this.currentView.eventsContainer.style.display = "none";
    }

    /**
     * Gets the project to explicitly destroy itself .
     */
    dispose() {
        this.currentView.dispose();
    }


    /**
     * Handles showing of project or sprint editor.
     */
    showEditor() {
        this.currentlyEditing = true
        this.currentView?.dispose();
        this.currentView = new Editor(this.containerElement,
            "Edit project details:",
            this.project,
            this.showViewer.bind(this),
            this.updateProject.bind(this),
            Editor.makeProjectProjectDatesValidator(this.project),
            this.project);
    }

    /**
     * Refreshes view by disposing of current view and creating a new one.
     */
    showViewer() {
        this.currentView?.dispose();
        this.currentView = new ProjectView(this.containerElement, this.project, this.showEditor.bind(this), this.deleteAndCreateDefaultProject.bind(this), this.deleteSprint.bind(this), this.onSprintUpdate.bind(this), this.deleteEvent.bind(this), this.onEventUpdate.bind(this), this.deleteDeadline.bind(this), this.onDeadlineUpdate.bind(this), this.deleteMilestone.bind(this), this.onMilestoneUpdate.bind(this), this.onEventEdit.bind(this), this.onDeadlineEdit.bind(this), this.onMilestoneEdit.bind(this));
        if (this.currentlyEditing) {
            this.currentView.toggleSprints();
            this.currentView.toggleMilestones();
            this.currentView.toggleDeadlines();
            this.currentView.toggleEvents();
            this.currentlyEditing = false
        }
    }

    /**
     * Updates project details according to newProject attributes.
     * @param newProject
     */
    async updateProject(newProject) {
        if (this.updateLoadingStatus === LoadingStatus.Pending) {
            return;
        } else if (
            newProject.name === this.project.name
            && newProject.description === this.project.description
            && DatetimeUtils.areEqual(newProject.startDate, this.project.startDate)
            && DatetimeUtils.areEqual(newProject.endDate, this.project.endDate)
        ) {
            // There is nothing to update.
            this.showViewer();
            return;
        }

        this.updateLoadingStatus = LoadingStatus.Pending;

        try {
            const result = await fetch(`api/v1/projects/${this.project.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name: newProject.name,
                    description: newProject.description,
                    startDate: DatetimeUtils.localToNetworkString(newProject.startDate),
                    endDate: DatetimeUtils.localToNetworkString(newProject.endDate)
                })
            })

            if (!result.ok) {
                await ErrorHandlerUtils.handleNetworkError(result, "update project");
            }

            // Saved! Show the updated view screen.
            this.updateLoadingStatus = LoadingStatus.Done;
            this.project = {
                ...newProject,
                id: this.project.id,
                sprints: this.project.sprints,
                events: this.project.events,
                milestones: this.project.milestones,
                deadlines: this.project.deadlines
            };
            this.showViewer();
        } catch (ex) {
            this.updateLoadingStatus = LoadingStatus.Error;

            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }

            ErrorHandlerUtils.handleUnknownNetworkError(ex, "update project");
        }
    }

    async createDefaultProject() {
        let tomorrow = new Date()
        tomorrow.setMonth(tomorrow.getMonth() + 8)
        const defaultProject = {
            id: '__NEW_PROJECT_FORM',
            name: `Project ${new Date().getFullYear()}`,
            description: null,
            startDate: new Date(),
            endDate: tomorrow,
        };
        await this.createCallback(defaultProject)
        window.location.reload()
    }

    /**
     * Makes a delete request and then creates a new default project
     */
    async deleteAndCreateDefaultProject() {
        try {
            await this.deleteProject()
            await this.createDefaultProject()
        } catch(e) {
            throw e
        }
    }

    /**
     * Handles project deletion by making DELETE request.
     */
    async deleteProject() {
        if (this.deleteLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.deleteLoadingStatus = LoadingStatus.Pending;

        try {
            const response = await fetch(`api/v1/projects/${this.project.id}`, {
                method: 'DELETE'
            })

            if (!response.ok) {
                await ErrorHandlerUtils.handleNetworkError(response, "delete project");
            }

            this.deleteLoadingStatus = LoadingStatus.Done;
            this.deleteCallback(this.project.id);
        } catch (ex) {
            this.deleteLoadingStatus = LoadingStatus.Error;

            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }

            ErrorHandlerUtils.handleUnknownNetworkError(ex, "delete project");
        }

    }

    /**
     * Handles sprint deletion of sprint with sprintId and renumbers sprints once one has been deleted.
     * @param sprintId - sprint to be deleted
     */
    deleteSprint(sprintId) {
        for (let i=0; i < this.project.sprints.length; i++) {
            if (this.project.sprints[i].sprintId === sprintId) {
                this.project.sprints.splice(i, 1);
            }

        }

        for (let i=0; i < this.project.sprints.length; i++) {
            this.project.sprints[i].orderNumber = i + 1;
        }

        const showingEvents = this.currentView.showingEvents;
        const showingMilestones = this.currentView.showingMilestones;
        const showingDeadlines = this.currentView.showingDeadlines;
        this.showViewer();
        this.currentView.toggleSprints();
        if (showingEvents) {
            this.currentView.toggleEvents();
        }
        if (showingMilestones) {
            this.currentView.toggleMilestones();
        }
        if (showingDeadlines) {
            this.currentView.toggleDeadlines();
        }
    }

    deleteEvent(eventId) {
        for (let j=0; j < this.project.events.length; j++) {
            if (this.project.events[j].eventId === eventId) {
                this.project.events.splice(j, 1);
            }

        }

        for (let i=0; i < this.project.events.length; i++) {
            this.project.events[i].orderNumber = i + 1;
        }

        const showingSprints = this.currentView.showingSprints;
        const showingMilestones = this.currentView.showingMilestones;
        const showingDeadlines = this.currentView.showingDeadlines;

        this.showViewer();
        this.currentView.toggleEvents();

        if (showingSprints) {
            this.currentView.toggleSprints();
        }
        if (showingMilestones) {
            this.currentView.toggleMilestones();
        }
        if (showingDeadlines) {
            this.currentView.toggleDeadlines()
        }

    }

    deleteMilestone(milestoneId) {
        for (let j=0; j < this.project.milestones.length; j++) {
            if (this.project.milestones[j].milestoneId === milestoneId) {
                this.project.milestones.splice(j, 1);
            }

        }

        for (let i=0; i < this.project.milestones.length; i++) {
            this.project.milestones[i].orderNumber = i + 1;
        }

        const showingSprints = this.currentView.showingSprints;
        const showingEvents = this.currentView.showingEvents;
        const showingDeadlines = this.currentView.showingDeadlines;

        this.showViewer();
        this.currentView.toggleMilestones();

        if (showingSprints) {
            this.currentView.toggleSprints();
        }

        if (showingEvents) {
            this.currentView.toggleEvents();
        }

        if (showingDeadlines) {
            this.currentView.toggleDeadlines()
        }
    }

    deleteDeadline(deadlineId) {
        for (let j=0; j < this.project.deadlines.length; j++) {
            if (this.project.deadlines[j].deadlineId === deadlineId) {
                this.project.deadlines.splice(j, 1);
            }

        }

        for (let i=0; i < this.project.deadlines.length; i++) {
            this.project.deadlines[i].orderNumber = i + 1;
        }

        const showingSprints = this.currentView.showingSprints;
        const showingEvents = this.currentView.showingEvents;
        const showingMilestones = this.currentView.showingMilestones;

        this.showViewer();
        this.currentView.toggleDeadlines();

        if (showingSprints) {
            this.currentView.toggleSprints();
        }

        if (showingMilestones) {
            this.currentView.toggleMilestones();
        }

        if (showingEvents) {
            this.currentView.toggleEvents();
        }
    }

}
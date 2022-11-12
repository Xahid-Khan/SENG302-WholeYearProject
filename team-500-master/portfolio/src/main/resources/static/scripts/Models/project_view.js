/**
 * Handles view of the Projects and populating the HTML.
 *
 * Note: This class is very messy and unnecessarily complicated due to poor code being built on
 * and expanded causing the code to be worse and worse, without a properly set architecture. As
 * advised by Fabian, we are going to leave it but this, and its related files, such as event view
 * etc, could be very much improved
 */
class ProjectView {
    showingSprints = false;
    showingEvents = false;
    showingDeadlines = false;
    showingMilestones = false;

    addEventForm = null;
    addEventLoadingStatus = LoadingStatus.NotYetAttempted

    addSprintForm = null;
    addSprintLoadingStatus = LoadingStatus.NotYetAttempted;

    addDeadlineForm = null
    addDeadlineLoadingStatus = LoadingStatus.NotYetAttempted;

    addMilestoneForm = null
    addMilestoneLoadingStatus = LoadingStatus.NotYetAttempted;

    constructor(containerElement, project, editCallback, deleteCallback, sprintDeleteCallback, sprintUpdateCallback, eventDeleteCallback, eventUpdateCallback, deadlineDeleteCallback, deadlineUpdateCallback, milestoneDeleteCallback, milestoneUpdateCallback, eventEditCallback, deadlineEditCallback, milestoneEditCallback) {
        this.containerElement = containerElement;
        this.project = project;
        this.sprintContainer = null;
        this.sprints = new Map();
        this.eventContainer = null;
        this.events = new Map();
        this.milestoneContainer = null;
        this.milestones = new Map();
        this.deadlineContainer = null;
        this.deadlines = new Map();
        this.milestoneContainer = null;
        this.milestones = new Map();
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
        this.sprintDeleteCallback = sprintDeleteCallback;
        this.sprintUpdateCallback = sprintUpdateCallback;
        this.eventDeleteCallback = eventDeleteCallback;
        this.eventUpdateCallback = eventUpdateCallback;
        this.deadlineDeleteCallback = deadlineDeleteCallback;
        this.deadlineUpdateCallback = deadlineUpdateCallback
        this.milestoneDeleteCallback = milestoneDeleteCallback;
        this.milestoneUpdateCallback = milestoneUpdateCallback;
        this.eventEditCallback = eventEditCallback;
        this.deadlineEditCallback = deadlineEditCallback;
        this.milestoneEditCallback = milestoneEditCallback;
        this.modalDeleteContainer=document.getElementById(`modal-delete-open`);
        this.modalDeleteX=document.getElementById(`modal-delete-x`);
        this.modalDeleteCancel=document.getElementById(`modal-delete-cancel`);
        this.modalDeleteConfirm=document.getElementById(`modal-delete-confirm`);

        this.eventDiv = null;
        this.showingEventDiv = false;
        this.milestoneDiv = null;
        this.showingMilestoneDiv = false;
        this.deadlineDiv = null;
        this.showingDeadlineDiv = false;
        this.sprintDiv = null;
        this.showingSprintDiv = false;
        this.showingProjectDetails = false;

        this.constructAndPopulateView();
        this.wireView();
    }

    /**
     * Append a sprint element to the sprintElement and instantiate and store a sprint with the given data.
     */
    appendSprint(sprintData) {
        const sprintElement = document.createElement("div");
        sprintElement.classList.add(`sprint-view-${this.project.id}`)
        sprintElement.style.margin='15px 0 15px 0';
        this.sprintContainer.appendChild(sprintElement);

        this.sprints.set(sprintData.sprintId, new Sprint(sprintElement, sprintData, this.project, this.sprintDeleteCallback, this.sprintUpdateCallback));
    }

    appendEvent(eventData) {
        const eventElement = document.createElement("div")
        eventElement.classList.add(`event-view-${this.project.id}`)
        eventElement.style.margin='15px 0 15px 0';
        this.eventContainer.appendChild(eventElement);

        this.events.set(eventData.eventId, new EventObject(eventElement, eventData, this.project, this.eventDeleteCallback, this.eventUpdateCallback, this.eventEditCallback));
    }

    appendMilestone(milestoneData) {
        const milestoneElement = document.createElement("div")
        milestoneElement.classList.add(`milestone-view-${this.project.id}`)
        milestoneElement.style.margin='15px 0 15px 0';
        this.milestoneContainer.appendChild(milestoneElement);

        this.milestones.set(milestoneData.milestoneId, new Milestone(milestoneElement, milestoneData, this.project, this.milestoneDeleteCallback, this.milestoneUpdateCallback, this.milestoneEditCallback));
    }

    appendDeadline(deadlineData) {
        const deadlineElement = document.createElement("div")
        deadlineElement.classList.add(`deadline-view-${this.project.id}`)
        deadlineElement.style.margin='15px 0 15px 0';
        this.deadlineContainer.appendChild(deadlineElement);

        this.deadlines.set(deadlineData.deadlineId, new Deadline(deadlineElement, deadlineData, this.project, this.deadlineDeleteCallback, this.deadlineUpdateCallback, this.deadlineEditCallback));
    }
    handleModalClose(){
        document.getElementById('modal-open').style.display='none'
        document.getElementById('modal-open-container').style.height='0'
        document.getElementById('modal-open-container').style.width='0'
    }
    /**
     * Adds HTML in to the project container, with the main attributes of projects and sprints.
     */
    constructAndPopulateView() {
        this.containerElement.innerHTML = `
              <div class="project-title">
                  <span class="project-title-text">
                    <span id="project-title-text-${this.project.id}"></span> | <span id="project-startDate-${this.project.id}"></span> - <span id="project-endDate-${this.project.id}"></span>
                  </span>   
                  <span class="monthly-planner-redirect">
                      <button class="button monthly-planner-redirect-button" id="monthly-planner-redirect-button-${this.project.id}">View Monthly Planner</button>
                  </span>
                  <span>
                      <button class="button visibility-button toggle-project-details" id="toggle-project-details-${this.project.id}"><span class='material-icons'>visibility_off</span></button>
                  </span>
                  <span class="crud">
                      <button class="button icon-button project-delete-button" id="project-delete-button-${this.project.id}" data-privilege="course_admin"><span class="material-icons">clear</span></button>
                      <button class="button icon-button edit-project" id="project-edit-button-${this.project.id}" data-privilege="teacher"><span class="material-icons">edit</span></button>
                  </span>
                  
              </div>
              <div>
                  <div class="project-description" id="project-description-${this.project.id}"></div>
              </div>
              <div class="project-events">
                  <div class="events raised-card" id="events-container-${this.project.id}">
                  <div class="events-header">
                     <div class="events-section-title">
                        <span class="material-icons">event</span>
                        Events:
                     </div>
                     <div class="add-events">
                         <button class="button toggle-view-controls toggle-events visibility-button" id="toggle-event-button-${this.project.id}"><span class="material-icons">visibility</span></button>
                         <button class="button" id="add-event-button-${this.project.id}" data-privilege="teacher"> Add Event</button>
                     </div>
                  </div>
                  </div>
                  <div class="events raised-card" id="deadlines-container-${this.project.id}">
                    <div class="events-header">
                        <div class="events-section-title">
                            <span class="material-icons">timer</span>
                            Deadlines:
                        </div>
                        <div class="add-events">
                            <button class="button toggle-deadlines visibility-button" id="toggle-deadline-button-${this.project.id}"><span class='material-icons'>visibility</span></button>
                            <button class="button" id="add-deadline-button-${this.project.id}" data-privilege="teacher"> Add Deadline</button>
                        </div>
                    </div>
                  </div>
                  <div class="events raised-card" id="milestones-container-${this.project.id}">
                      <div class="events-header">
                        <div class="events-section-title"> 
                            <span class="material-icons">flag</span>
                            Milestones:
                        </div>
                        <div class="add-events">
                            <button class="button toggle-milestones visibility-button" id="toggle-milestone-button-${this.project.id}"><span class='material-icons'>visibility</span></button>
                            <button class="button" id="add-milestone-button-${this.project.id}" data-privilege="teacher"> Add Milestone</button>
                        </div>
                      </div>
                  </div>
              </div>
              <div class="sprints raised-card" id="sprints-container-${this.project.id}">
                <div class="events-header">
                    <div class="events-section-title">Sprints:</div>
                    <div class="add-events">
                        <button class="button toggle-sprints visibility-button" id="toggle-sprint-button-${this.project.id}"><span class='material-icons'>visibility</span></button>
                        <button class="button" id="add-sprint-button-${this.project.id}" data-privilege="teacher"> Add Sprint</button>
                    </div>
                </div>
              </div>
              
    
  </form>
</div>


              

            `;

        document.getElementById(`project-title-text-${this.project.id}`).innerText = this.project.name;
        document.getElementById(`project-description-${this.project.id}`).innerText = this.project.description;
        document.getElementById(`project-startDate-${this.project.id}`).innerText = DatetimeUtils.localToUserDMYWithoutTime(this.project.startDate);
        const displayedDate = new Date(this.project.endDate.valueOf());
        displayedDate.setDate(displayedDate.getDate()  - 1);
        document.getElementById(`project-endDate-${this.project.id}`).innerText = DatetimeUtils.localToUserDMYWithoutTime(displayedDate);

        this.addSprintButton = document.getElementById(`add-sprint-button-${this.project.id}`);
        this.toggleSprintsButton = document.getElementById(`toggle-sprint-button-${this.project.id}`);
        this.sprintsContainer = document.getElementById(`sprints-container-${this.project.id}`);
        this.sprintContainer = document.getElementById(`sprints-container-${this.project.id}`);

        this.addEventButton = document.getElementById(`add-event-button-${this.project.id}`);
        this.toggleEventsButton = document.getElementById(`toggle-event-button-${this.project.id}`);
        this.eventsContainer = document.getElementById(`events-container-${this.project.id}`);
        this.eventContainer = document.getElementById(`events-container-${this.project.id}`);

        this.addMilestoneButton = document.getElementById(`add-milestone-button-${this.project.id}`);
        this.toggleMilestonesButton = document.getElementById(`toggle-milestone-button-${this.project.id}`);
        this.milestonesContainer = document.getElementById(`milestones-container-${this.project.id}`);
        this.milestoneContainer = document.getElementById(`milestones-container-${this.project.id}`);

        this.addDeadlineButton = document.getElementById(`add-deadline-button-${this.project.id}`);
        this.toggleDeadlinesButton = document.getElementById(`toggle-deadline-button-${this.project.id}`);
        this.deadlinesContainer = document.getElementById(`deadlines-container-${this.project.id}`);
        this.deadlineContainer = document.getElementById(`deadlines-container-${this.project.id}`);


        for (let i = 0; i < this.project.sprints.length; i++) {
            this.appendSprint(this.project.sprints[i]);
        }

        for (let j = 0; j < this.project.events.length; j++) {
            this.appendEvent(this.project.events[j]);
        }

        for (let k = 0; k < this.project.milestones.length; k++) {
            this.appendMilestone(this.project.milestones[k]);
        }

        for (let k = 0; k < this.project.deadlines.length; k++) {
            this.appendDeadline(this.project.deadlines[k]);
        }
    }

    /**
     * Toggles hiding and showing of the sprints.
     */
    toggleSprints() {
        if (this.showingSprints) {
            // Hide the sprints
            this.sprintsContainer.style.display = "none";
        }
        else {
            // Show the sprints
            this.sprintsContainer.style.display = "block";
        }

        this.showingSprints = !this.showingSprints;
    }

    toggleEvents() {
        if (this.showingEvents) {
            // Hide the sprints
            this.eventsContainer.style.display = "none";
        }
        else {
            // Show the events
            this.eventContainer.style.display = "block";
        }

        this.showingEvents = !this.showingEvents;
    }

    toggleMilestones() {
        if (this.showingMilestones) {
            // Hide the sprints
            this.milestonesContainer.style.display = "none";
        }
        else {
            // Show the events
            this.milestonesContainer.style.display = "block";
        }

        this.showingMilestones = !this.showingMilestones;
    }

    toggleDeadlines() {
        if (this.showingDeadlines) {

            // Hide the sprints
            this.deadlinesContainer.style.display = "none";
        }
        else {
            // Show the events
            this.deadlinesContainer.style.display = "block";
        }

        this.showingDeadlines = !this.showingDeadlines;
    }
    
    /**
     * Opens the add sprint form.
     */
    openAddSprintForm() {
        if (this.addSprintForm !== null) {
            return;
        }

        const formContainerElement = document.createElement("div");
        formContainerElement.classList.add("events-view", "raised-card");
        formContainerElement.id = `create-sprint-form-container-${this.project.id}`;
        this.sprintsContainer.append(this.sprintsContainer.firstChild, formContainerElement);

        let defaultName = 1;
        let defaultStartDate = new Date(this.project.startDate.valueOf());

        if (this.project.sprints.length !== 0) {
            defaultName = this.project.sprints[(this.project.sprints.length - 1)].orderNumber + 1;
            defaultStartDate = new Date(this.project.sprints[(this.project.sprints.length - 1)].endDate.valueOf());
        }
        
        const defaultEndDate = new Date(defaultStartDate.valueOf());
        defaultEndDate.setDate(defaultEndDate.getDate() + 22);

        const defaultColour = "#000000";

        const defaultSprint = {
            id: `__NEW_SPRINT_FORM_${this.project.id}`,
            name: `Sprint ${defaultName}`,
            description: null,
            startDate: defaultStartDate,
            endDate: defaultEndDate,
            colour: defaultColour
        };

        this.addSprintForm = {
            container: formContainerElement,
            controller: new Editor(
                formContainerElement,
                "New sprint details:",
                defaultSprint,
                this.closeAddSprintForm.bind(this),
                this.submitAddSprintForm.bind(this),
                Editor.makeProjectSprintDatesValidator(this.project, null),
                this.project
            )
        };

        if (!this.showingSprints) {
            this.toggleSprints();
        }

    }

    /**
     * Closes the add sprint form.
     */
    closeAddSprintForm() {
        if (this.addSprintForm === null) {
            return;
        }

        this.addSprintForm.controller.dispose();
        this.sprintsContainer.removeChild(this.addSprintForm.container);
        this.addSprintForm = null;
    }

    /**
     * Submits the add sprint form, checking if this task is not being done currently (loading status).
     * @param sprint
     * @returns {Promise<void>}
     */
    async submitAddSprintForm(sprint) {
        if (this.addSprintLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.addSprintLoadingStatus = LoadingStatus.Pending;

        try {
            const res = await fetch(`api/v1/projects/${this.project.id}/sprints`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(sprint)
            });

            if (!res.ok) {
                await ErrorHandlerUtils.handleNetworkError(res, "creating project");
            }

            const newSprint = await res.json();
            this.sprintUpdateCallback({
                ...newSprint,
                startDate: DatetimeUtils.networkStringToLocalDate(newSprint.startDate),
                endDate: DatetimeUtils.networkStringToLocalDate(newSprint.endDate),
                colour: newSprint.colour
            });
        }
        catch (ex) {
            this.addSprintLoadingStatus = LoadingStatus.Error;
            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }
            ErrorHandlerUtils.handleUnknownNetworkError(ex, "creating project");
        }
    }

    openAddEventForm() {
        if (this.addEventForm !== null) {
            return;
        }

        const formContainerElement = document.createElement("div");
        formContainerElement.classList.add("events-view", "raised-card");
        formContainerElement.id = `create-event-form-container-${this.project.id}`;
        this.eventContainer.append(this.eventsContainer.firstChild, formContainerElement)

        const defaultEvent = {
            id: `__NEW_EVENT_FORM_${this.project.id}`,
            name: null,
            description: null,
            startDate: null,
            endDate: null
        };

        this.addEventForm = {
            container: formContainerElement,
            controller: new Editor(
                formContainerElement,
                "New event details:",
                defaultEvent,
                this.closeAddEventForm.bind(this),
                this.submitAddEventForm.bind(this),
                Editor.makeProjectEventDatesValidator(this.project),
                this.project,
                true
            )
        };

        if (!this.showingEvents) {
            this.toggleEvents();
        }
    }

    /**
     * Closes the add event form.
     */
    closeAddEventForm() {
        if (this.addEventForm === null) {
            return;
        }

        this.addEventForm.controller.dispose();
        this.eventsContainer.removeChild(this.addEventForm.container);
        this.addEventForm = null;
    }

    /**
     * Submits the add event form, checking if this task is not being done currently (loading status).
     * @param event
     * @returns {Promise<void>}
     */
    async submitAddEventForm(event) {
        if (this.addEventLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.addEventLoadingStatus = LoadingStatus.Pending;

        try {
            const res = await fetch(`api/v1/projects/${this.project.id}/events`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(event)
            });

            if (!res.ok) {
                await ErrorHandlerUtils.handleNetworkError(res, "creating project");
            }

            const newEvent = await res.json();
            this.eventUpdateCallback({
                ...newEvent,
                startDate: DatetimeUtils.networkStringToLocalDate(newEvent.startDate),
                endDate: DatetimeUtils.networkStringToLocalDate(newEvent.endDate)
            });
        }
        catch (ex) {
            this.addEventLoadingStatus = LoadingStatus.Error;
            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }
            ErrorHandlerUtils.handleUnknownNetworkError(ex, "creating project");
        }
    }

    openAddMilestoneForm() {
        if (this.addMilestoneForm !== null) {
            return;
        }

        const formContainerElement = document.createElement("div");
        formContainerElement.classList.add("events-view", "raised-card");
        formContainerElement.id = `create-milestone-form-container-${this.project.id}`;
        this.milestoneContainer.append(this.milestonesContainer.firstChild, formContainerElement)

        const defaultMilestone = {
            id: `__NEW_MILESTONE_FORM_${this.project.id}`,
            name: null,
            description: null,
            startDate: null,
            endDate: null
        };

        this.addMilestoneForm = {
            container: formContainerElement,
            controller: new Editor(
                formContainerElement,
                "New milestone details:",
                defaultMilestone,
                this.closeAddMilestoneForm.bind(this),
                this.submitAddMilestoneForm.bind(this),
                Editor.makeProjectMilestoneDatesValidator(this.project),
                this.project
            )
        };

        if (!this.showingMilestones) {
            this.toggleMilestones();
        }
    }

    /**
     * Closes the add milestone form.
     */
    closeAddMilestoneForm() {
        if (this.addMilestoneForm === null) {
            return;
        }

        this.addMilestoneForm.controller.dispose();
        this.milestonesContainer.removeChild(this.addMilestoneForm.container);
        this.addMilestoneForm = null;
    }

    /**
     * Submits the add milestone form, checking if this task is not being done currently (loading status).
     * @param milestone
     * @returns {Promise<void>}
     */
    async submitAddMilestoneForm(milestone) {
        if (this.addMilestoneLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.addMilestoneLoadingStatus = LoadingStatus.Pending;

        try {
            const res = await fetch(`api/v1/projects/${this.project.id}/milestones`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(milestone)
            });

            if (!res.ok) {
                await ErrorHandlerUtils.handleNetworkError(res, "creating project");
            }

            const newMilestone = await res.json();
            this.milestoneUpdateCallback({
                ...newMilestone,
                startDate: DatetimeUtils.networkStringToLocalDate(newMilestone.startDate),
                endDate: DatetimeUtils.networkStringToLocalDate(newMilestone.endDate)
            });
        }
        catch (ex) {
            this.addMilestoneLoadingStatus = LoadingStatus.Error;
            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }
            ErrorHandlerUtils.handleUnknownNetworkError(ex, "creating project");
        }
    }

    openAddDeadlineForm() {
        if (this.addDeadlineForm !== null) {
            return;
        }

        const formContainerElement = document.createElement("div");
        formContainerElement.classList.add("events-view", "raised-card");
        formContainerElement.id = `create-deadline-form-container-${this.project.id}`;
        this.deadlineContainer.append(this.deadlinesContainer.firstChild, formContainerElement)

        const defaultDeadline = {
            id: `__NEW_DEADLINE_FORM_${this.project.id}`,
            name: null,
            description: null,
            startDate: null,
            endDate: null
        };

        this.addDeadlineForm = {
            container: formContainerElement,
            controller: new Editor(
                formContainerElement,
                "New deadline details:",
                defaultDeadline,
                this.closeAddDeadlineForm.bind(this),
                this.submitAddDeadlineForm.bind(this),
                Editor.makeProjectDeadlineDatesValidator(this.project),
                this.project,
                true,
                false
            )
        };

        if (!this.showingDeadlines) {
            this.toggleDeadlines();
        }
    }

    /**
     * Closes the add deadline form.
     */
    closeAddDeadlineForm() {
        if (this.addDeadlineForm === null) {
            return;
        }

        this.addDeadlineForm.controller.dispose();
        this.deadlinesContainer.removeChild(this.addDeadlineForm.container);
        this.addDeadlineForm = null;
    }

    /**
     * Submits the add deadline form, checking if this task is not being done currently (loading status).
     * @param deadline
     * @returns {Promise<void>}
     */
    async submitAddDeadlineForm(deadline) {
        if (this.addDeadlineLoadingStatus === LoadingStatus.Pending) {
            return;
        }

        this.addDeadlineLoadingStatus = LoadingStatus.Pending;

        try {
            const res = await fetch(`api/v1/projects/${this.project.id}/deadlines`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(deadline)
            });

            if (!res.ok) {
                await ErrorHandlerUtils.handleNetworkError(res, "creating project");
            }

            const newDeadline = await res.json();
            this.deadlineUpdateCallback({
                ...newDeadline,
                startDate: DatetimeUtils.networkStringToLocalDate(newDeadline.startDate),
                endDate: DatetimeUtils.networkStringToLocalDate(newDeadline.endDate)
            });
        }
        catch (ex) {
            this.addDeadlineLoadingStatus = LoadingStatus.Error;
            if (ex instanceof PortfolioNetworkError) {
                throw ex;
            }
            ErrorHandlerUtils.handleUnknownNetworkError(ex, "creating project");
        }
    }

    showSprints() {
        this.toggleSprintsButton.innerHTML = this.showingSprintDiv ? "<span class='material-icons'>visibility</span>" : "<span class='material-icons'>visibility_off</span>"
        this.sprintDiv = document.getElementsByClassName(`sprint-view-${this.project.id}`)
        if (this.sprintDiv) {
            for (let i = 0; i < this.sprintDiv.length; i++) {
                this.sprintDiv[i].style.display === "none" ? this.sprintDiv[i].style.display = "block" : this.sprintDiv[i].style.display = "none"
            }        }
        this.showingSprintDiv = !this.showingSprintDiv
    }

    showEvents() {
         this.toggleEventsButton.innerHTML = this.showingEventDiv ? "<span class='material-icons'>visibility</span>" : "<span class='material-icons'>visibility_off</span>"
        this.eventDiv = document.getElementsByClassName(`event-view-${this.project.id}`)
        if (this.eventDiv) {
            for (let i = 0; i < this.eventDiv.length; i++) {
                this.eventDiv[i].style.display === "none" ? this.eventDiv[i].style.display = "block" : this.eventDiv[i].style.display = "none"
            }
        }
        this.showingEventDiv = !this.showingEventDiv
    }

    showDeadlines() {
        this.toggleDeadlinesButton.innerHTML = this.showingDeadlineDiv ? "<span class='material-icons'>visibility</span>" : "<span class='material-icons'>visibility_off</span>"
        this.deadlineDiv = document.getElementsByClassName(`deadline-view-${this.project.id}`)
        if (this.deadlineDiv) {
            for (let i = 0; i < this.deadlineDiv.length; i++) {
                this.deadlineDiv[i].style.display === "none" ? this.deadlineDiv[i].style.display = "block" : this.deadlineDiv[i].style.display = "none"
            }
        }
        this.showingDeadlineDiv = !this.showingDeadlineDiv
    }

    showMilestones() {
        this.toggleMilestonesButton.innerHTML = this.showingMilestoneDiv ? "<span class='material-icons'>visibility</span>" : "<span class='material-icons'>visibility_off</span>"
        this.milestoneDiv = document.getElementsByClassName(`milestone-view-${this.project.id}`)
        if (this.milestoneDiv) {
            for (let i = 0; i < this.milestoneDiv.length; i++) {
                this.milestoneDiv[i].style.display === "none" ? this.milestoneDiv[i].style.display = "block" : this.milestoneDiv[i].style.display = "none"
            }
        }
        this.showingMilestoneDiv = !this.showingMilestoneDiv
    }

    toggleProjectDetails() {
        this.toggleEvents();
        this.toggleDeadlines();
        this.toggleSprints();
        this.toggleMilestones();
        document.getElementById(`toggle-project-details-${this.project.id}`).innerHTML = this.showingProjectDetails ? "<span class='material-icons'>visibility_off</span>" : "<span class='material-icons'>visibility</span>";
        this.showingProjectDetails = !this.showingProjectDetails;
    }

    monthlyPlannerRedirect(projectId) {
        window.location.href = `monthly-planner/${projectId}`
    }
    openDeleteModal(){
        this.modalDeleteContainer.style.display='block';
        document.getElementById('modal-delete-body').innerText=
            'Are you sure you want to delete the project? A new project with default settings will be created in its place.'
        this.modalDeleteX.addEventListener("click",()=>this.cancelDeleteModal())
        this.modalDeleteCancel.addEventListener("click",()=>this.cancelDeleteModal())
        this.modalDeleteConfirm.addEventListener("click",()=>this.confirmDeleteModal())


    }
    cancelDeleteModal(){
        this.modalDeleteContainer.style.display='none';
        this.modalDeleteX.removeEventListener("click",()=>this.cancelDeleteModal())
        this.modalDeleteCancel.removeEventListener("click",()=>this.cancelDeleteModal())
        this.modalDeleteConfirm.removeEventListener("click",()=>this.confirmDeleteModal())

    }
    confirmDeleteModal(){
        this.modalDeleteContainer.style.display='none';
        this.modalDeleteX.removeEventListener("click",()=>this.cancelDeleteModal())
        this.modalDeleteCancel.removeEventListener("click",()=>this.cancelDeleteModal())
        this.modalDeleteConfirm.removeEventListener("click",()=>this.confirmDeleteModal())
        this.deleteCallback()
    }
    wireView() {
        document.getElementById(`project-edit-button-${this.project.id}`).addEventListener("click", () => this.editCallback());
        document.getElementById(`toggle-project-details-${this.project.id}`).addEventListener("click", () => this.toggleProjectDetails())
        document.getElementById(`project-delete-button-${this.project.id}`).addEventListener("click", () => this.openDeleteModal());
        document.getElementById(`monthly-planner-redirect-button-${this.project.id}`).addEventListener("click", () => this.monthlyPlannerRedirect(this.project.id));
        this.toggleSprintsButton.addEventListener('click', this.showSprints.bind(this));
        this.addSprintButton.addEventListener('click', this.openAddSprintForm.bind(this));
        this.toggleEventsButton.addEventListener('click', this.showEvents.bind(this));
        this.addEventButton.addEventListener('click', this.openAddEventForm.bind(this));
        this.toggleMilestonesButton.addEventListener('click', this.showMilestones.bind(this));
        this.addMilestoneButton.addEventListener('click', this.openAddMilestoneForm.bind(this));
        this.toggleDeadlinesButton.addEventListener('click', this.showDeadlines.bind(this));
        this.addDeadlineButton.addEventListener('click', this.openAddDeadlineForm.bind(this));
    }

    dispose() {

    }
}
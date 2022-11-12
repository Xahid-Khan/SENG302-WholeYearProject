class DeadlineView {
    expandedView = false;

    constructor(containerElement, sprints, deadline, deleteCallback, editCallback) {
        this.containerElement = containerElement;
        this.deadline = deadline;
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
        this.sprints = sprints;
        this.modalDeleteContainer=document.getElementById(`modal-delete-open`);
        this.modalDeleteX=document.getElementById(`modal-delete-x`);
        this.modalDeleteCancel=document.getElementById(`modal-delete-cancel`);
        this.modalDeleteConfirm=document.getElementById(`modal-delete-confirm`);

        this.constructView();
        this.wireView();
    }

    /**
     * Adds populated HTML to deadlineView.
     */
    constructView() {
        this.containerElement.innerHTML = `
    <div id = "${this.deadline.deadlineId}" class = "raised-card">
    <div class="colour-block" id="event-colour-block-${this.deadline.deadlineId}"></div>
        <div class="card-contents">
            <div class="crud">
                    <button class="icon-button deadline-controls" id="deadline-button-edit-${this.deadline.deadlineId}" data-privilege="teacher"><span class="material-icons">edit</span></button>
                    <button class="icon-button deadline-controls" id="deadline-button-delete-${this.deadline.deadlineId}" data-privilege="teacher"><span class="material-icons">clear</span></button>
                    <button class="button visibility-button toggle-deadline-details" id="toggle-deadline-details-${this.deadline.deadlineId}"><span class='material-icons'>visibility_off</span></button>
            </div>
            <div class="editing-live-update" id="event-form-${this.deadline.deadlineId}"></div>
            <div class="events-title">
                <span id="deadline-title-text-${this.deadline.deadlineId}" style="font-style: italic;"></span> | <span id="start-date-${this.deadline.deadlineId}"></span>
            </div>
            <div class="events-details" id="deadline-details-${this.deadline.deadlineId}">
                <label class="event-description-label" id="event-description-label-${this.deadline.deadlineId}"></label>
                <div class="event-description" id="deadline-description-${this.deadline.deadlineId}"></div>
                <div class="events-sprints" id="deadline-sprints-${this.deadline.deadlineId}"></div>
            </div>
        </div>
    </div>
    
    `;

        this.toggleButton = document.getElementById(`toggle-deadline-details-${this.deadline.deadlineId}`);
        this.descriptionLabel = document.getElementById(`event-description-label-${this.deadline.deadlineId}`);
        this.description = document.getElementById(`deadline-description-${this.deadline.deadlineId}`);
        this.details = document.getElementById(`deadline-details-${this.deadline.deadlineId}`);
        this.deadlineSprints = document.getElementById(`deadline-sprints-${this.deadline.deadlineId}`);

        document.getElementById(`deadline-title-text-${this.deadline.deadlineId}`).innerText = this.deadline.name;
        if(this.deadline.description.trim().length !== 0){
            this.descriptionLabel.innerText = "Description:\n";
        }
        this.description.innerText = this.deadline.description;
        this.deadlineSprints.innerHTML = this.getSprints();
        this.sprints.forEach((sprint) => {
            if (document.getElementById(`deadline-sprint-name-${this.deadline.deadlineId}-${sprint.sprintId}`)) {
                document.getElementById(`deadline-sprint-name-${this.deadline.deadlineId}-${sprint.sprintId}`).innerText = sprint.name + ':'
            }
        })
        document.getElementById(`start-date-${this.deadline.deadlineId}`).innerText = DatetimeUtils.localToDMYWithTime(this.deadline.startDate);
    }

    /**
     * Toggles expanded view and button for deadlines.
     */
    toggleExpandedView() {
        if (this.expandedView) {
            this.details.style.display = "none";
            this.toggleButton.innerHTML = "<span class='material-icons'>visibility_off</span>";
        }
        else {
            this.details.style.display = "block";
            this.toggleButton.innerHTML = "<span class='material-icons'>visibility</span>";
        }

        this.expandedView = !this.expandedView;
    }
    openDeleteModal(){
        this.modalDeleteContainer.style.display='block';
        document.getElementById('modal-delete-body').innerText=
            'Are you sure you want to delete the deadline?'
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
        Socket.saveEdit(this.deadline.deadlineId)
        window.removeEventListener('beforeunload', () => Socket.cancelEdit(this.entityId))
        this.deleteCallback()
    }

    wireView() {
        document.getElementById(`deadline-button-edit-${this.deadline.deadlineId}`).addEventListener('click', () => this.editCallback());
        document.getElementById(`deadline-button-delete-${this.deadline.deadlineId}`).addEventListener("click", () => this.openDeleteModal());

        this.toggleButton.addEventListener('click', this.toggleExpandedView.bind(this));
    }

    getSprints() {
        let html = "<label>Sprints in progress during this deadline: </label>";
        let foundSprints = false
        document.getElementById(`event-colour-block-${this.deadline.deadlineId}`).style.display="none";
        //Uses linear gradient to make the coloured line
        let gradient = "linear-gradient(45deg,"
        this.sprints.forEach(sprint => {
            if (this.deadline.startDate >= sprint.startDate && this.deadline.startDate <= sprint.endDate || this.deadline.endDate >= sprint.startDate && this.deadline.endDate <= sprint.endDate) {
                html += `
                <div class="event-sprint-container">
                    <div class="event-sprint-details">
                        <span> • </span>
                        <span id="deadline-sprint-name-${this.deadline.deadlineId}-${sprint.sprintId}" class="event-sprint-name"></span>
                        <span>${DatetimeUtils.localToUserDMY(sprint.startDate)}</span>
                        <span> - </span>
                        <span>${DatetimeUtils.localToUserDMY(sprint.endDate)}</span>
                    </div>
                    <div style="background-color: ${sprint.colour}" class="event-sprint-colour-block"></div>
                </div>`;
                foundSprints = true

                //Done twice to handle cases of single sprint. Displays block if a sprint contains the event
                gradient+=sprint.colour+","
                gradient+=sprint.colour+","
                document.getElementById(`event-colour-block-${this.deadline.deadlineId}`).style.display="block";
            }
        });
        //Splices the last comma out of the linear gradient so it compiles. Sets the line colour
        gradient=gradient.slice(0, -1) + ')';
        document.getElementById(`event-colour-block-${this.deadline.deadlineId}`).style.background=gradient;

        if (!foundSprints) {
            html = "<label>No sprints are overlapping with this deadline</label>"
        }
        return html;
    }

    dispose() {

    }
}


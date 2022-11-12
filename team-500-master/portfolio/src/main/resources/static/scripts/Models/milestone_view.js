class MilestoneView {
    expandedView = false;

    constructor(containerElement, sprints, milestone, deleteCallback, editCallback) {
        this.containerElement = containerElement;
        this.milestone = milestone;
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
     * Adds populated HTML to milestoneView.
     */
    constructView() {
        this.containerElement.innerHTML = `
    <div id = "${this.milestone.milestoneId}" class = "raised-card colour-block-card">
    <div class="colour-block" id="event-colour-block-${this.milestone.milestoneId}"></div>
        <div class="card-contents">
            <div class="crud">
                <button class="icon-button milestone-controls" id="milestone-button-edit-${this.milestone.milestoneId}" data-privilege="teacher"><span class="material-icons">edit</span></button>
                <button class="icon-button milestone-controls" id="milestone-button-delete-${this.milestone.milestoneId}" data-privilege="teacher"><span class="material-icons">clear</span></button>
                <button class="button visibility-button toggle-milestone-details" id="toggle-milestone-details-${this.milestone.milestoneId}"><span class='material-icons'>visibility_off</span></i></button>
            </div>
            <div class="editing-live-update" id="event-form-${this.milestone.milestoneId}"></div>
            <div class="events-title">
                <span id="milestone-title-text-${this.milestone.milestoneId}" style="font-style: italic;"></span> | <span id="start-date-${this.milestone.milestoneId}"></span>
        
            </div>
            <div class="events-details" id="milestone-details-${this.milestone.milestoneId}">
                <label class="event-description-label" id="event-description-label-${this.milestone.milestoneId}"></label>
                <div class="event-description" id="milestone-description-${this.milestone.milestoneId}"></div>
                <div class="events-sprints" id="milestone-sprints-${this.milestone.milestoneId}"></div>
            </div>
        </div>
    </div>
    `;

        this.toggleButton = document.getElementById(`toggle-milestone-details-${this.milestone.milestoneId}`);
        this.descriptionLabel = document.getElementById(`event-description-label-${this.milestone.milestoneId}`);
        this.description = document.getElementById(`milestone-description-${this.milestone.milestoneId}`);
        this.details = document.getElementById(`milestone-details-${this.milestone.milestoneId}`);
        this.milestoneSprints = document.getElementById(`milestone-sprints-${this.milestone.milestoneId}`);

        document.getElementById(`milestone-title-text-${this.milestone.milestoneId}`).innerText = this.milestone.name;
        if(this.milestone.description.trim().length !== 0){
            this.descriptionLabel.innerText = "Description:\n";
        }
        this.description.innerText = this.milestone.description;
        this.milestoneSprints.innerHTML = this.getSprints();
        this.sprints.forEach((sprint) => {
            if (document.getElementById(`milestone-sprint-name-${this.milestone.milestoneId}-${sprint.sprintId}`)) {
                document.getElementById(`milestone-sprint-name-${this.milestone.milestoneId}-${sprint.sprintId}`).innerText = sprint.name + ':'
            }
        })
        document.getElementById(`start-date-${this.milestone.milestoneId}`).innerText = DatetimeUtils.localToUserDMYWithoutTime(this.milestone.startDate);
    }

    /**
     * Toggles expanded view and button for milestones.
     */
    toggleExpandedView() {
        if (this.expandedView) {
            this.details.style.display = "none";
            this.toggleButton.innerHtml = "<span class='material-icons'>visibility_off</span>";
        } else {
            this.details.style.display = "block";
            this.toggleButton.innerHTML = "<span class='material-icons'>visibility</span>";
        }

        this.expandedView = !this.expandedView;
    }
    openDeleteModal(){
        this.modalDeleteContainer.style.display='block';
        document.getElementById('modal-delete-body').innerText=
            'Are you sure you want to delete the milestone?'
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
        Socket.saveEdit(this.milestone.milestoneId)
        window.removeEventListener('beforeunload', () => Socket.cancelEdit(this.entityId))
        this.deleteCallback()
    }

    wireView() {
        document.getElementById(`milestone-button-edit-${this.milestone.milestoneId}`).addEventListener('click', () => this.editCallback());
        document.getElementById(`milestone-button-delete-${this.milestone.milestoneId}`).addEventListener("click", () => this.openDeleteModal());

        this.toggleButton.addEventListener('click', this.toggleExpandedView.bind(this));
    }

    getSprints() {
        let html = "<label>Sprints in progress during this milestone: </label>";
        let foundSprints = false
        document.getElementById(`event-colour-block-${this.milestone.milestoneId}`).style.display="none";
        //Uses linear gradient to make the coloured line
        let gradient = "linear-gradient(45deg,"
        this.sprints.forEach(sprint => {
            if (this.milestone.startDate >= sprint.startDate && this.milestone.startDate <= sprint.endDate || this.milestone.endDate >= sprint.startDate && this.milestone.endDate <= sprint.endDate) {
                html += `
                <div class="event-sprint-container">
                    <div class="event-sprint-details">
                        <span> • </span>
                        <span id="milestone-sprint-name-${this.milestone.milestoneId}-${sprint.sprintId}" class="event-sprint-name"></span>
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
                document.getElementById(`event-colour-block-${this.milestone.milestoneId}`).style.display="block";
            }
        });
        //Splices the last comma out of the linear gradient so it compiles. Sets the line colour
        gradient=gradient.slice(0, -1) + ')';
        document.getElementById(`event-colour-block-${this.milestone.milestoneId}`).style.background=gradient;

        if (!foundSprints) {
            html += "<span>No sprints are overlapping with this milestone</span>"
        }
        return html;
    }

    dispose() {

    }
}
'use strict';

const LoadingStatus = {
  NotYetAttempted: "NotYetAttempted",
  Pending: "Pending",
  Done: "Done",
  Error: "Error"
};

class PortfolioNetworkError extends Error {
  constructor(message, ...args) {
    super(message, ...args);
  }
}

/**
 * Handles network errors.
 */
class ErrorHandlerUtils {
  static async handleNetworkError(response, context) {
    const body = await response.text();
    // Shows the errors element,
    //  then locates the specific error element to update.
    document.getElementById("errors").style.display = ""
    const errorOutput = document.getElementById("error");
    if (body) {
      errorOutput.textContent = `An error occurred. Server responded: ${body}`;
    }
    else {
      errorOutput.textContent = `An error occurred. Server responded: ${response.statusText} (${response.status}).`;
    }

    throw new PortfolioNetworkError(`A server error occurred when ${context}. Status: ${response.status} ${response.statusText}`);
  }

  static handleUnknownNetworkError(ex, context) {
    // Shows the errors element,
    //  then locates the specific error element to update.
    document.getElementById("errors").style.display = ""
    const errorOutput = document.getElementById("error");
    errorOutput.textContent = `An unknown error occurred. Please try again. ${ex}`
    throw new PortfolioNetworkError(`An unknown error occurred when ${context}. Status: ${response.status} ${response.statusText}`);
  }
}


const displayCharactersRemaining = (field, maxCharacters) => {
  let lengthField;
  // This is hacky, and should be solved when project_details.js is refactored.
  if (field.id.includes("name")) {
    lengthField = field.parentNode.childNodes.item(5)
  } else {
    lengthField = field.parentNode.childNodes.item(5)
  }
  lengthField.textContent = `${field.value.length} / ${maxCharacters}`;
}

/**
 * Manage the projects (creation and deletion and loading)
 */
class Application {

  addProjectForm = null;
  addProjectLoadingStatus = LoadingStatus.NotYetAttempted;

  constructor(containerElement) {
    this.projects = null;
    this.projectsLoadingState = LoadingStatus.NotYetAttempted;
    this.containerElement = containerElement;

    this.wireView();
  }

  wireView() {
  }

  /**
   * Submits a new project form if the response from POST request is valid.
   * @param project
   */
  async submitAddProjectForm(project) {
    if (this.addProjectLoadingStatus === LoadingStatus.Pending) {
      return;
    }

    this.addProjectLoadingStatus = LoadingStatus.Pending;

    try {
      const res = await fetch("api/v1/projects", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(project)
      });

      if (!res.ok) {
        await ErrorHandlerUtils.handleNetworkError(res, "create project");
      }

      const newProject = await res.json();
      this.appendProject(newProject, {
        prepend: true,
        scrollIntoView: true
      });
      this.addProjectLoadingStatus = LoadingStatus.Done;
      this.closeAddProjectForm();
    } catch (ex) {
      this.addProjectLoadingStatus = LoadingStatus.Error;

      if (ex instanceof PortfolioNetworkError) {
        throw ex;
      }

      ErrorHandlerUtils.handleUnknownNetworkError(ex, "create project");
    }
  }

  /**
   * Closes the add project form and refreshes the view.
   */
  closeAddProjectForm() {
  }

  /**
   * Opens the add project form, creating a new formContainer and a default project.
   */
  openAddProjectForm() {
  }

  /**
   * Clears all projects.
   */
  clearProjects() {
    if (this.projects) {
      this.projects.map(project => project.dispose());
      this.projects = null;
    }
  }

  /**
   * Append a project element to the containerElement and instantiate and store a Project with the given data.
   */
  appendProject(projectData, options) {
    const {prepend, scrollIntoView} = options ?? {};

    // Post-process the projectData
    projectData.startDate = DatetimeUtils.networkStringToLocalDate(projectData.startDate);
    projectData.endDate = DatetimeUtils.networkStringToLocalDate(projectData.endDate);
    projectData.sprints = projectData.sprints.map(sprint => ({
      ...sprint,
      startDate: DatetimeUtils.networkStringToLocalDate(sprint.startDate),
      endDate: DatetimeUtils.networkStringToLocalDate(sprint.endDate)
    }));
    projectData.events = projectData.events.map(event => ({
      ...event,
      startDate: DatetimeUtils.networkStringToLocalDate(event.startDate),
      endDate: DatetimeUtils.networkStringToLocalDate(event.endDate)
    }));
    projectData.milestones = projectData.milestones.map(milestone => ({
      ...milestone,
      startDate: DatetimeUtils.networkStringToLocalDate(milestone.startDate),
      endDate: DatetimeUtils.networkStringToLocalDate(milestone.endDate)
    }));
    projectData.milestones = projectData.milestones.map(milestone => ({
      ...milestone,
      startDate: DatetimeUtils.networkStringToLocalDate(milestone.startDate),
      endDate: DatetimeUtils.networkStringToLocalDate(milestone.endDate)
    }));
    projectData.deadlines = projectData.deadlines.map(deadline => ({
      ...deadline,
      startDate: DatetimeUtils.networkStringToLocalDate(deadline.startDate),
      endDate: DatetimeUtils.networkStringToLocalDate(deadline.endDate)
    }));

    // Construct base HTML
    const projectElement = document.createElement("div");
    projectElement.classList.add("project-view", "raised-card");
    projectElement.id = `project-view-${projectData.id}`;

    if (prepend) {
      this.containerElement.insertBefore(projectElement, this.containerElement.firstChild);
    } else {
      this.containerElement.appendChild(projectElement);
    }

    this.projects.set(projectData.id, new Project(projectElement, projectData, this.deleteProject.bind(this), this.submitAddProjectForm.bind(this)));

    if (scrollIntoView) {
      projectElement.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }
  }

  /**
   * Fetches projects by making a GET request.
   * @returns {Promise<void>}
   */
  async fetchProjects() {
    if (this.projectsLoadingState === LoadingStatus.Pending) {
      return;
    }

    this.projectsLoadingState = LoadingStatus.Pending;
    this.clearProjects();

    try {
      const result = await fetch('api/v1/projects');

      if (!result.ok) {
        await ErrorHandlerUtils.handleNetworkError(result, "get projects");
      }

      const data = await result.json();
      this.projects = new Map();
      data.map(project => this.appendProject(project));
      if (this.projects.size === 1) {
        // Automatically expand the project if only one project is loaded.
        this.projects.forEach((project) => {
          project.currentView.toggleProjectDetails();
        })
      }

      this.projectsLoadingState = LoadingStatus.Done;
    } catch (ex) {
      this.projectsLoadingState = LoadingStatus.Error;

      if (ex instanceof PortfolioNetworkError) {
        throw ex;
      }

      ErrorHandlerUtils.handleUnknownNetworkError(ex, "get projects");
    }
  }

  /**
   * Handles project deletion according to the given project ID.
   * @param projectId - project do be deleted
   */
  deleteProject(projectId) {
    const projectElement = document.getElementById(`project-view-${projectId}`)
    this.containerElement.removeChild(projectElement);
    this.projects.get(projectId).dispose();
    this.projects.delete(projectId);
  }

}

(() => {
  // Start
  const application = new Application(document.getElementById("project-list"));
  if (document.getElementsByClassName('studentMode').length === 0) {
    Socket.start("edit-project", "alert")
  }
  application.fetchProjects();
})()
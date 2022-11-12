package nz.ac.canterbury.seng302.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.portfolio.mapping.ProjectMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseProjectContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.ProjectContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;

import java.util.ArrayList;
import java.util.NoSuchElementException;


/**
 * A project service, which implements all the CRUD (Create, Read, Update, Delete) processes.
 */
@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * Create a Project Contract, return it with project ID number.
     * @param contract a contract received from application.
     * @return contart of the newly created project
     */
    public ProjectContract create(BaseProjectContract contract){
        var project = projectMapper.toEntity(contract);
        projectRepository.save(project);

        return projectMapper.toContract(project);
    }


    /**
     * Delete a Project Entity, return void.
     * @param projectId project ID of the project that needs to be deleted.
     */
    public void delete(String projectId) {
        projectRepository.deleteById(projectId);
    }


    /**
     * This method will fetch all the projects and return them in json file
     * @return a iterable list containing all the projects
     */
    public ArrayList<ProjectContract> allProjects(){
        Iterable<ProjectEntity> result = projectRepository.findAll();
        ArrayList<ProjectContract> allProjects = new ArrayList<ProjectContract>();

        for(ProjectEntity project : result) {
            allProjects.add(projectMapper.toContract(project));
        }

        return allProjects;
    }

    /**
     * This method will get a specific project given the id, if it exist in the database
     * else, it will throw an Exception
     * @param id which is a long
     * @throws NoSuchElementException is raised if project ID is not in database
     * @return project entity
     */
    public ProjectContract getById(String id) {
        return projectMapper.toContract(projectRepository.findById(id).orElseThrow());
    }


    /**
     * This method will update the current project details.
     * @param project contract of the new project
     * @param  id Type String of the project
     * @throws NoSuchElementException if the project does not exist
     */
    public void update(ProjectContract project, String id) {
        var projectEntity = projectRepository.findById(id).orElseThrow();
        projectEntity.setDescription(project.description());
        projectEntity.setName(project.name());
        projectEntity.setStartDate(project.startDate());
        projectEntity.setEndDate(project.endDate());

        projectRepository.save(projectEntity);
    }

}


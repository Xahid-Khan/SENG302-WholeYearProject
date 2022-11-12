package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.mapping.DeadlineMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseDeadlineContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.DeadlineContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.DeadlineEntity;
import nz.ac.canterbury.seng302.portfolio.repository.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.NoSuchElementException;

@Service
public class DeadlineService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DeadlineRepository deadlineRepository;

    @Autowired
    private DeadlineMapper deadlineMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get the deadline with the deadline ID
     *
     * @param deadlineId The deadline ID
     * @throws IllegalArgumentException If the deadline ID is invalid
     * @return The deadline contract with the deadline ID
     */
    public DeadlineContract get(String deadlineId) {
        var deadline= deadlineRepository.findById(deadlineId).orElseThrow();
        return deadlineMapper.toContract(deadline);
    }

    /**
     * Creates an deadline within a project and puts it in a sprint
     * if it falls within the sprint's start and end dates
     *
     * @return
     */
    public DeadlineContract createDeadline(String projectId, BaseDeadlineContract deadline) {
        var project = projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
        var entity = deadlineMapper.toEntity(deadline);

        project.addDeadline(entity);
        deadlineRepository.save(entity);
        projectRepository.save(project);

        return deadlineMapper.toContract(entity);
    }


    /**
     * Deletes an deadline, including removing it from its parent project.
     *
     * @param deadlineId to delete
     * @throws NoSuchElementException if the id is invalid
     */
    public void delete(String deadlineId) {
        var deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new NoSuchElementException("Invalid deadline ID"));
        var project = deadline.getProject();


        project.removeDeadline(deadline);
        deadlineRepository.deleteById(deadlineId);
        projectRepository.save(project);
    }

    /**
     * Updates an deadline using the DeadlineContract provided
     * @param deadlineId to update
     * @param deadline to update, with the new values
     */
    public void update(String deadlineId, BaseDeadlineContract deadline) {
        DeadlineEntity deadlineEntity = deadlineRepository.findById(deadlineId).orElseThrow();

        deadlineEntity.setName(deadline.name());
        deadlineEntity.setDescription(deadline.description());
        deadlineEntity.setStartDate(deadline.startDate());

        deadlineRepository.save(deadlineEntity);
        entityManager.clear();
    }

}
package nz.ac.canterbury.seng302.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import nz.ac.canterbury.seng302.portfolio.mapping.SprintMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseSprintContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.SprintContract;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.repository.SprintRepository;

/**
 * A service that manages CRUD operations for sprints.
 */
@Service
@Transactional
public class SprintService {
    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintMapper sprintMapper;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieve the sprint with the given ID.
     *
     * @param id of the contract to get
     * @throws NoSuchElementException if the id is invalid
     * @return Sprint with the given ID
     */
    public SprintContract get(String id) {
        var sprint = sprintRepository.findById(id).orElseThrow();
        return sprintMapper.toContract(sprint);
    }

    /**
     * Creates a SprintContract, returning it with its ID and order number.
     *
     * @param projectId to create the sprint in.
     * @param sprint to create
     * @throws NoSuchElementException if the project id is invalid
     * @return Sprint that was created, including the orderNumber generated.
     */
    public SprintContract create(String projectId, BaseSprintContract sprint) {
        var project = projectRepository.findById(projectId).orElseThrow();

        var entity = sprintMapper.toEntity(sprint);
        project.addSprint(entity);
        sprintRepository.save(entity);
        projectRepository.save(project);

        return sprintMapper.toContract(entity);
    }

    /**
     * Deletes a sprint, including removing it from its parent project.
     *
     * @param sprintId to delete
     * @throws NoSuchElementException if the id is invalid
     */
    public void delete(String sprintId) {
        var sprint = sprintRepository.findById(sprintId).orElseThrow();
        var project = sprint.getProject();

        project.removeSprint(sprint);
        sprintRepository.delete(sprint);
        projectRepository.save(project);
    }

    /**
     * Updates a sprint using the SprintContract data provided.
     *
     * @param sprintId to update
     * @param sprint to update, with the update fields filled.
     * @throws NoSuchElementException if the id is invalid
     */
    public void update(String sprintId, BaseSprintContract sprint) {
        var sprintEntity = sprintRepository.findById(sprintId).orElseThrow();

        sprintEntity.setName(sprint.name());
        sprintEntity.setDescription(sprint.description());
        sprintEntity.setStartDate(sprint.startDate());
        sprintEntity.setEndDate(sprint.endDate());
        sprintEntity.setColour(sprint.colour());

        sprintRepository.save(sprintEntity);

        // Flush and then detach the sprint to force orderNumber to be recalculated.
        // Thanks to: https://stackoverflow.com/a/26796980
        entityManager.flush();
        entityManager.clear();  // TODO: This clears *everything* from the cache. Look at options to only clear this sprint.
    }
}

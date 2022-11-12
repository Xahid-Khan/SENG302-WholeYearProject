package nz.ac.canterbury.seng302.portfolio.model.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.mapping.ProjectMapper;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.repository.SprintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SprintEntityTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @BeforeEach
    public void beforeEach() {
        sprintRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    public void testGetOrderNumberOnOrderedInsertion() {
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        projectRepository.save(project);

        var sprint = new SprintEntity("test sprint", "test description", Instant.ofEpochSecond(120), Instant.ofEpochSecond(360), "#fff");
        project.addSprint(sprint);
        sprintRepository.save(sprint);

        assertEquals(1, sprint.getOrderNumber());

        var sprint2 = new SprintEntity("test sprint", "test description", Instant.ofEpochSecond(420), Instant.ofEpochSecond(480), "#fff");
        project.addSprint(sprint2);
        sprintRepository.save(sprint2);

        assertEquals(1, sprint.getOrderNumber());
        assertEquals(2, sprint2.getOrderNumber());
    }

    @Test
    public void testGetOrderNumberOnOutOfOrderInsertion() {
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        projectRepository.save(project);

        var laterSprint = new SprintEntity("test sprint", "test description", Instant.ofEpochSecond(120), Instant.ofEpochSecond(360), "#fff");
        project.addSprint(laterSprint);
        sprintRepository.save(laterSprint);

        assertEquals(1, laterSprint.getOrderNumber());

        var earlierSprint = new SprintEntity("test sprint", "test description", Instant.ofEpochSecond(60), Instant.ofEpochSecond(120), "#fff");
        project.addSprint(earlierSprint);
        sprintRepository.save(earlierSprint);

        assertEquals(2, laterSprint.getOrderNumber());
        assertEquals(1, earlierSprint.getOrderNumber());
    }

    @Test
    public void testProjectToContractOrderNumberMapping() {
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        projectRepository.save(project);

        var laterSprint = new SprintEntity("test sprint later", "test description", Instant.ofEpochSecond(120), Instant.ofEpochSecond(360), "#fff");
        project.addSprint(laterSprint);
        sprintRepository.save(laterSprint);
        var earlierSprint = new SprintEntity("test sprint earlier", "test description", Instant.ofEpochSecond(60), Instant.ofEpochSecond(120), "#fff");
        project.addSprint(earlierSprint);
        sprintRepository.save(earlierSprint);

        var projectContract = projectMapper.toContract(project);
        var sprintContract1 = projectContract.sprints().get(0);
        var sprintContract2 = projectContract.sprints().get(1);

        assertEquals("test sprint earlier", sprintContract1.name());
        assertEquals(1, sprintContract1.orderNumber());

        assertEquals("test sprint later", sprintContract2.name());
        assertEquals(2, sprintContract2.orderNumber());
    }
}

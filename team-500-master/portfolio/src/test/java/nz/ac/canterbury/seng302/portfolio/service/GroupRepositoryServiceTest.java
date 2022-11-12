package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.mapping.GroupRepositoryMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.GroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseGroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.GroupRepositoryEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.GroupRepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to test the GroupRepositoryService class using mocking of the repository class
 */
@SpringBootTest
public class GroupRepositoryServiceTest {
    @InjectMocks
    private GroupRepositoryService groupRepositoryService;

    @Mock
    private GroupRepositoryRepository groupRepositoryRepository;

    @Mock
    private GroupRepositoryMapper groupRepositoryMapper;

    @Mock
    private SimpMessagingTemplate template;

    @Mock
    private PostService postService;

    private GroupRepositoryContract repoContract1;
    private GroupRepositoryEntity repoEntity1;
    private GroupRepositoryContract emptyRepoContract1;
    private GroupRepositoryEntity emptyRepoEntity1;
    private GroupRepositoryContract repo2;
    private BaseGroupRepositoryContract emptyRepo2;

    private List<GroupRepositoryEntity> repoList;
    private String id = "1";

    private List<GroupRepositoryContract> contractList;

    @BeforeEach
    void setup() {
        groupRepositoryRepository.deleteAll();
        repoContract1 = new GroupRepositoryContract(1, 1, "ABCTOKEN", "", "");
        repoEntity1 = new GroupRepositoryEntity(1, 1, "ABCTOKEN", "");
        emptyRepoContract1 = new GroupRepositoryContract(2, -1, "No token", "", "");
        emptyRepoEntity1 = new GroupRepositoryEntity(2);
        repoList = new ArrayList<>();
        repoList.add(new GroupRepositoryEntity(repoContract1.groupId(), repoContract1.groupId(), repoContract1.token(), repoContract1.alias()));


        repoList.forEach(repo -> {
            groupRepositoryRepository.save(repo);
        });
    }

    /**
     * Tests the add method of the GroupRepositoryService class with a valid group ID that does not exist
     */
    @Test
    void addValidRepoExpectPass() {
        //Tests existence check
        Mockito.when(groupRepositoryRepository.existsById(emptyRepoEntity1.getId())).thenReturn(false);
        //Tests save
        doReturn(emptyRepoEntity1).when(groupRepositoryRepository).save(emptyRepoEntity1);
        //Mocks the mapper in the return statement
        Mockito.when(groupRepositoryMapper.toContract(any())).thenReturn(emptyRepoContract1);


        var result = groupRepositoryService.add(emptyRepoContract1.groupId());
        assertEquals(emptyRepoContract1.groupId(), result.groupId());
    }

    /**
     * Tests adding the same repo twice
     */
    @Test
    void addValidRepoTwiceExpectFail() {
        //Tests existence check
        Mockito.when(groupRepositoryRepository.existsById("2")).thenReturn(false).thenReturn(true);
        //Tests save
        doReturn(emptyRepoEntity1).when(groupRepositoryRepository).save(emptyRepoEntity1);
        //Mocks the mapper in the return statement
        Mockito.when(groupRepositoryMapper.toContract(any())).thenReturn(emptyRepoContract1);


        var result = groupRepositoryService.add(emptyRepoContract1.groupId());
        assertEquals(emptyRepoContract1.groupId(), result.groupId());

        var resultTwice = groupRepositoryService.add(emptyRepoContract1.groupId());
        assertNotNull(resultTwice);
    }

    /**
     * Tests deleting a repo with a group id that exists
     */
    @Test
    void deleteARepoThatExistsExpectPass() {
        //When the id exists in the database then mock that the statement returns true
        Mockito.when(groupRepositoryRepository.existsById(id)).thenReturn(true);
        assertTrue(groupRepositoryService.delete(Integer.parseInt(id)));
    }

    /**
     * Tests deleting a repo with twice, the first valid the 2nd invalid
     */
    @Test
    void deleteARepoTwiceExpectFail() {
        //When the id exists in the database then mock that the statement returns true
        Mockito.when(groupRepositoryRepository.existsById(id)).thenReturn(true).thenReturn(false);
        assertTrue(groupRepositoryService.delete(Integer.parseInt(id)));
        assertFalse(groupRepositoryService.delete(Integer.parseInt(id)));
    }


    /**
     * Deletes a repository that doesn't exist
     */
    @Test
    void deleteARepoThatDoesNotExistExpectFail() {
        assertFalse(groupRepositoryService.delete(2));
    }


    /**
     * Tests updating a repository that doesn't exist
     */
    @Test
    void updateARepoThatDoesNotExistExpectFail() {
        Mockito.when(groupRepositoryRepository.existsById(id)).thenReturn(false);
        assertTrue(groupRepositoryService.update(Integer.parseInt(id), 11, "ABCTOKEN", ""));
    }

    /**
     * Tests updating a repository that does exist
     */

    @Test
    void updateARepoThatDoesExistExpectPass() {
        //Mocks the repository calls in the service class
        Mockito.when(groupRepositoryRepository.existsById(id)).thenReturn(true);
        Mockito.when(groupRepositoryRepository.findById(id)).thenReturn(Optional.of(emptyRepoEntity1));
        Mockito.when(groupRepositoryRepository.save(emptyRepoEntity1)).thenReturn(emptyRepoEntity1);

        boolean result = groupRepositoryService.update(Integer.parseInt(id), 11, "ABCTOKEN", "");
        assertTrue(result);
    }


    /**
     * Tests getting a repository that does exist
     */
    @Test
    void getValidRepoExpectPass() {
        //Tests existence check
        Mockito.when(groupRepositoryRepository.existsById(id)).thenReturn(false);
        //Tests get
        Mockito.when(groupRepositoryRepository.findById(id)).thenReturn(Optional.ofNullable(emptyRepoEntity1));
        //Mocks the mapper in the return statement
        Mockito.when(groupRepositoryMapper.toContract(any())).thenReturn(repoContract1);

        var result = groupRepositoryService.get(id);
        assertEquals("1", result.groupId().toString());
    }

    /**
     * Tests getting a repository that doesn't exist
     */
    @Test
    void getNonExistentRepoExpectFail() {
        //Tests existence check
        Mockito.when(groupRepositoryRepository.existsById(id)).thenReturn(true);
        //Tests get
        Mockito.when(groupRepositoryRepository.findById(id)).thenReturn(Optional.ofNullable(emptyRepoEntity1));
        //Mocks the mapper in the return statement
        Mockito.when(groupRepositoryMapper.toContract(any())).thenReturn(repoContract1);

        var result = groupRepositoryService.get(id);
        assertNull(result);
    }

    /**
     * Tests getting all repositories
     */
    @Test
    void getAllReposExpectPass() {
        //Tests existence check
        Mockito.when(groupRepositoryRepository.findAll()).thenReturn(List.of(emptyRepoEntity1, repoEntity1));
        //Mocks the mapper in the return statement
        Mockito.when(groupRepositoryMapper.toContract(any())).thenReturn(emptyRepoContract1).thenReturn(repoContract1);

        var result = groupRepositoryService.getAll();
        assertEquals(2, result.size());
        assertEquals("2", result.get(0).groupId().toString());
        assertEquals("1", result.get(1).groupId().toString());

    }
}

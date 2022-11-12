package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.database.GroupMemberRepository;
import nz.ac.canterbury.seng302.identityprovider.database.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static nz.ac.canterbury.seng302.identityprovider.service.AddingBaseGroups.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the GroupsServerService class
 */
@SpringBootTest
public class GroupsServiceTest {

    @Autowired
    private GroupsServerService groupsServerService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @BeforeEach
    private void clear() {
        groupRepository.deleteAll();
        groupMemberRepository.deleteAll();
    }

    /**
     * Helper method that attemps to create and then delete a group using the GroupsServerService
     * @param shortName the short name to create the group with
     * @param longName the long name to create the group with
     * @param expectSuccess true if the service should succeed in deletion, false if it should fail
     */
    private void createAndDeleteGroup(String shortName, String longName, boolean expectSuccess) {
        groupsServerService.createGroup(
                CreateGroupRequest.newBuilder()
                        .setShortName(shortName)
                        .setLongName(longName)
                        .build(),
                new StreamObserver<CreateGroupResponse>() {
                    @Override
                    public void onNext(CreateGroupResponse value) {
                        if(!value.getIsSuccess()){
                            assertTrue(false, "Test failed when trying to create group");
                        }
                        groupsServerService.deleteGroup(
                                DeleteGroupRequest
                                        .newBuilder()
                                        .setGroupId(value.getNewGroupId())
                                        .build(),
                                new StreamObserver<DeleteGroupResponse>() {
                                    @Override
                                    public void onNext(DeleteGroupResponse value) {
                                        assertEquals(expectSuccess, value.getIsSuccess());
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        assertTrue(false, "Test failed when trying to delete group");
                                    }

                                    @Override
                                    public void onCompleted() {
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Throwable t) {
                        assertTrue(false, "Test failed when trying to create group");
                    }

                    @Override
                    public void onCompleted() {
                    }
                }
        );
    }

    /**
     * Tests to ensure the teachers group cannot be deleted
     */
    @Test
    public void cannotDeleteTeachersGroup() {
        createAndDeleteGroup(TEACHERS_GROUP_SHORT_NAME,TEACHERS_GROUP_LONG_NAME, false);
    }

    /**
     * Tests to ensure the non group cannot be deleted
     */
    @Test
    public void cannotDeleteNonGroup() {
        createAndDeleteGroup(NON_GROUP_SHORT_NAME,NON_GROUP_LONG_NAME, false);
    }

    /**
     * Tests to ensure a regular group can be deleted
     */
    @Test
    public void canDeleteNormalGroup() {
        createAndDeleteGroup("Team 500","The best around", true);
    }

}

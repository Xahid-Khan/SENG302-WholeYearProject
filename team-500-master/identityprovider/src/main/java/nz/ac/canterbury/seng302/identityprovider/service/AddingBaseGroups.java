package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.database.GroupMemberModel;
import nz.ac.canterbury.seng302.identityprovider.database.GroupMemberRepository;
import nz.ac.canterbury.seng302.identityprovider.database.GroupModel;
import nz.ac.canterbury.seng302.identityprovider.database.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@Component
public class AddingBaseGroups {

    public static final String TEACHERS_GROUP_SHORT_NAME = "Teachers";
    public static final String TEACHERS_GROUP_LONG_NAME = "Teaching Team";
    public static final String NON_GROUP_SHORT_NAME = "Non Group";
    public static final String NON_GROUP_LONG_NAME = "Users without a group";

    @Autowired
    private GroupRepository groupRepository;


    @Autowired
    private GroupMemberRepository groupMemberRepository;

    /**
     * This method is listing to the application ready event, after application is ready it calls a function creates 2 groups
     */
    @EventListener({ApplicationReadyEvent.class})
    public void addUsersToDatabase() {
        createTeacherAndNonGroup();
    }


    /**
     * This method creates 2 groups.
     */
    public void createTeacherAndNonGroup() {
        if (groupRepository.findByShortName("Teachers") == null) {
            GroupModel teachers = new GroupModel(TEACHERS_GROUP_SHORT_NAME, TEACHERS_GROUP_LONG_NAME);
            GroupModel nonGroup = new GroupModel(NON_GROUP_SHORT_NAME, NON_GROUP_LONG_NAME);
            groupRepository.save(teachers);
            groupRepository.save(nonGroup);
            GroupMemberModel teacherGroupMembers = new GroupMemberModel(groupRepository.findByShortName(TEACHERS_GROUP_SHORT_NAME).getId(), List.of());
            groupMemberRepository.save(teacherGroupMembers);
            GroupMemberModel nonGroupMembers = new GroupMemberModel(groupRepository.findByShortName(NON_GROUP_SHORT_NAME).getId(), List.of());
            groupMemberRepository.save(nonGroupMembers);
        }
    }

}

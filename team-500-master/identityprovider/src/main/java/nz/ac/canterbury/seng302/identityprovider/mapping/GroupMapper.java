package nz.ac.canterbury.seng302.identityprovider.mapping;


import nz.ac.canterbury.seng302.identityprovider.database.GroupMemberRepository;
import nz.ac.canterbury.seng302.identityprovider.database.GroupModel;
import nz.ac.canterbury.seng302.identityprovider.database.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Provides utility methods for mapping between different representations of a GroupMember.
 */
@Component
public class GroupMapper {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    public GroupDetailsResponse toGroupDetailsResponse(GroupModel group) {
        //gets all users from ids in useRepository
        var groupMembersIds = groupMemberRepository.findById(group.getId()).get().getUserIds();
        var groupMembersIterable = userRepository.findAllById(groupMembersIds);
        ArrayList<UserResponse> groupMembers = new ArrayList<>();

        //adds all the users to a list and maps them to userResponse
        for (var user : groupMembersIterable) {
            groupMembers.add(userMapper.toUserResponse(user));
        }

        return GroupDetailsResponse.newBuilder()
                .setGroupId(group.getId())
                .setShortName(group.getShortName())
                .setLongName(group.getLongName())
                .addAllMembers(groupMembers)
                .build();

    }
}

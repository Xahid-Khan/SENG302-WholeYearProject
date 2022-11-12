package nz.ac.canterbury.seng302.identityprovider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RoleServiceTest {

  @Autowired private UserRepository userRepository;

  @Autowired private UserAccountService userAccountService;

  @Autowired private RolesServerService rolesServerService;

  @BeforeEach
  void addUser() {
    userRepository.deleteAll();

    userRepository.save(
        new UserModel(
            "Username",
            "hash",
            "FirstName",
            "MiddleName",
            "LastName",
            "NickName",
            "Bio",
            "Pronouns",
            "Email@Email.Email",
            List.of(UserRole.STUDENT)));
  }

  /** Adds a teacher role to a user. */
  @Test
  void addRolesToUser() {
    var response =
        rolesServerService.addRoleToUser(
            ModifyRoleOfUserRequest.newBuilder()
                .setUserId(userRepository.findAll().iterator().next().getId())
                .setRole(UserRole.TEACHER)
                .build());

    assertTrue(response.getIsSuccess());

    assertEquals(
        2,
        userRepository
            .findById(userRepository.findAll().iterator().next().getId())
            .getRoles()
            .size());
  }

  /** Adds a teacher role to a user, then removes it. */
  @Test
  void removeRolesFromUser() {
    rolesServerService.addRoleToUser(
        ModifyRoleOfUserRequest.newBuilder()
            .setUserId(userRepository.findAll().iterator().next().getId())
            .setRole(UserRole.TEACHER)
            .build());
    var response =
        rolesServerService.removeRoleFromUser(
            ModifyRoleOfUserRequest.newBuilder()
                .setUserId(userRepository.findAll().iterator().next().getId())
                .setRole(UserRole.TEACHER)
                .build());

    assertTrue(response.getIsSuccess());
    assertEquals(
        1,
        userRepository
            .findById(userRepository.findAll().iterator().next().getId())
            .getRoles()
            .size());
  }

  @Test
  void ensureNoDuplicateRoles() {
    var response =
        rolesServerService.addRoleToUser(
            ModifyRoleOfUserRequest.newBuilder()
                .setUserId(userRepository.findAll().iterator().next().getId())
                .setRole(UserRole.STUDENT)
                .build());

    assertFalse(response.getIsSuccess());
    assertEquals(
        1,
        userRepository
            .findById(userRepository.findAll().iterator().next().getId())
            .getRoles()
            .size());
  }

  @Test
  void ensureNoEmptyRoles() {
    var response =
        rolesServerService.removeRoleFromUser(
            ModifyRoleOfUserRequest.newBuilder()
                .setUserId(userRepository.findAll().iterator().next().getId())
                .setRole(UserRole.STUDENT)
                .build());

    assertFalse(response.getIsSuccess());
    assertEquals("Error: User must have at least 1 role.", response.getMessage());

    assertEquals(
        1,
        userRepository
            .findById(userRepository.findAll().iterator().next().getId())
            .getRoles()
            .size());
  }

  @Test
  void modifyRolesOfNonExistentUser() {
    var response =
        rolesServerService.addRoleToUser(
            ModifyRoleOfUserRequest.newBuilder().setUserId(-1).setRole(UserRole.TEACHER).build());
    assertFalse(response.getIsSuccess());
    assertEquals("Error: User does not exist.", response.getMessage());
  }
}

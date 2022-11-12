package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.NotificationService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the NotificationsController
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc ;

    @Autowired
    private NotificationsController controller;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private AuthStateService authStateService;

    @MockBean
    private NotificationService service;

    private final int USER_ID = 3;


    /**
     * Mocks the authentication service to return a valid student
     */
    @BeforeEach
    void setupBeforeEach() {
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
                UserResponse.newBuilder()
                        .setId(USER_ID)
                        .setUsername("testing")
                        .build()
        );
        Mockito.when(authStateService.getId(any(PortfolioPrincipal.class))).thenReturn(3);
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

    }

    /**
     * This test makes sure that controller is loaded and running.
     *
     @throws Exception if mockMvc fails
     */
    @Test
    void contextLoads() throws Exception {
        Assertions.assertNotNull(controller);
    }

    /**
     * verifies that when the notifications for a user are requested, an ok status is returned
     * @throws Exception
     */
    @Test
    void getAllTestValidId() throws Exception {
        Mockito.when(service.getAll(USER_ID)).thenReturn(null);
        mockMvc.perform(get("/api/v1/notifications/3"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(service).getAll(USER_ID);
    }

    /**
     * verifies that when the notifications for an invalid user are requested, a bad request status is returned
     * @throws Exception
     */
    @Test
    void getAllTestInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/abc"))
                .andExpect(status().isBadRequest());
    }


    /**
     * verifies that when a valid notification is posted, an ok status is returned, and it was created in the service layer
     * @throws Exception
     */
    @Test
    void createTestValid() throws Exception {
        var body = """
                {
                    "userId": 3,
                    "notifiedFrom": "you",
                    "description": "you have been notified"
                }
                """;
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(service).create(Mockito.any());
    }

    /**
     * verifies that when an invalid notification is posted, a bad request status is returned, and it was not created in the service layer
     * @throws Exception
     */
    @Test
    void createTestInvalid() throws Exception {
        var body = """
                {
                    "userId": "not",
                    "notifiedFrom": "you",
                    "description": "you have been notified"
                }
                """;
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        Mockito.verify(service, Mockito.never()).create(Mockito.any());
    }

    /**
     * verifies that when a valid post is made to mark all notifications as seen,
     * it is received and passed to the service layer
     * @throws Exception
     */
    @Test
    void markAsSeenTest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/seen/3"))
                .andExpect(status().isOk());
        Mockito.verify(service).setNotificationsSeen(3);
    }

    /**
     * verifies that when an invalid post is made to mark all notifications as seen,
     * it returns a bad request is not passed to the service layer
     * @throws Exception
     */
    @Test
    void markAsSeenInvalidTest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/seen/abc"))
                .andExpect(status().isBadRequest());
        Mockito.verify(service, Mockito.never()).setNotificationsSeen(any());
    }

}

package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.RegisterClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This class tests the GroupController functionality
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class GroupsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private RegisterClientService service;

    @MockBean
    private AuthenticateClientService authService;

    @BeforeEach
    public void beforeEach() {
        when(authService.authenticate(any(), any()))
                .thenReturn(
                        AuthenticateResponse.newBuilder()
                                .setUserId(0)
                                .setToken("test_token")
                                .setUsername("test_user")
                                .setFirstName("Test")
                                .setLastName("User")
                                .setEmail("test@example.com")
                                .setSuccess(true)
                                .setMessage("Login successful")
                                .build());
    }

}

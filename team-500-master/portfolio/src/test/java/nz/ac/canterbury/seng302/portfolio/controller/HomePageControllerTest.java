package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.SubscriptionService;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the HomePageController
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
public class HomePageControllerTest {

    @Autowired
    private MockMvc mockMvc ;

    @Autowired
    private HomePageController controller;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private AuthStateService authStateService;

    @MockBean
    private SubscriptionService service;

    private final int USER_ID = 3;
    private final SubscriptionContract contract = new SubscriptionContract(1, 1);


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
     * Helper method for submitting REST requests to /subscribe
     * @param body json body to submit
     * @throws Exception
     */
    private ResultActions mockPerformWithJSON(MockHttpServletRequestBuilder requestBuilder, String body) throws Exception {
        return mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * Tests to ensure that a valid subscription can be posted successfully
     * @throws Exception
     */
    @Test
    void subscribeValidTest() throws Exception {
        mockPerformWithJSON(post("/api/v1/subscribe"),"""
                {
                    "userId": 1,
                    "groupId": 1
                }
                """)
                .andExpect(status().isOk());
        Mockito.verify(service).subscribe(contract);
    }

    /**
     * Tests to ensure that an invalid subscription cannot be posted
     * @throws Exception
     */
    @Test
    void subscribeInvalidParamTypeTest() throws Exception {
        mockPerformWithJSON(post("/api/v1/subscribe"),"""
                {
                    "userId": "one",
                    "groupId": 1
                }
                """)
                .andExpect(status().isBadRequest());
        Mockito.verify(service, Mockito.never()).subscribe(contract);
    }


    /**
     * Tests to ensure that an invalid unsubscription cannot be posted
     * @throws Exception
     */
    @Test
    void unsubscribeInvalidParamTypeTest() throws Exception {
        mockPerformWithJSON(delete("/api/v1/unsubscribe"),"""
                {
                    "userId": "one",
                    "groupId": 1
                }
                """)
                .andExpect(status().isBadRequest());
        Mockito.verify(service, Mockito.never()).unsubscribe(contract);
    }

    /**
     * Tests that you can get All the groups a user is subscribed to
     * @throws Exception
     */
    @Test
    void getAllSubscriptionsValid() throws Exception {
        Mockito.when(service.getAllByUserId(5)).thenReturn(new ArrayList<Integer>());
        mockMvc.perform(get("/api/v1/subscribe/5"))
                .andExpect(status().isOk());
        Mockito.verify(service).getAllByUserId(5);
    }


}

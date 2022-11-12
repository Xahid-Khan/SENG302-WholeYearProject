package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.mapping.ConversationMapper;
import nz.ac.canterbury.seng302.portfolio.mapping.MessageMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.ConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.MessageContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.UserContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.ConversationService;
import nz.ac.canterbury.seng302.portfolio.service.MessageService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageController messageController;

    @MockBean
    private ConversationMapper conversationMapper;

    @MockBean
    private MessageMapper messageMapper;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private AuthStateService authStateService;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ConversationService conversationService;

    /**
     * Mocks the authentication service to return a valid student
     */
    @BeforeEach
    void setupBeforeEach() {
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
                UserResponse.newBuilder()
                        .setId(3)
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
        Assertions.assertNotNull(messageController);
    }

    /**
     * verifies that when the conversations for a user are requested with a valid format and no offset, an ok status is returned
     * @throws Exception
     */
    @Test
    void getAllConversationsTestValidNoOffset() throws Exception {
        Mockito.when(conversationService.getPaginatedConversations(3, 0, 20)).thenReturn(new PageImpl<>(List.of(new ConversationEntity(List.of(1)))));
        Mockito.when(conversationMapper.toContract(any())).thenReturn(new ConversationContract("1", null, new Timestamp(new Date().getTime()), new MessageContract("1", "1", 3, "John", "hey", new Timestamp(new Date().getTime())), null));
        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(conversationService).getPaginatedConversations(3, 0, 20);
    }

    /**
     * verifies that when the conversations for a user are requested with a valid format and an offset, an ok status is returned
     * @throws Exception
     */
    @Test
    void getAllConversationsTestValidOffset() throws Exception {
        Mockito.when(conversationService.getPaginatedConversations(3, 1, 20)).thenReturn(new PageImpl<>(List.of(new ConversationEntity(List.of(1)))));
        Mockito.when(conversationMapper.toContract(any())).thenReturn(new ConversationContract("1", null, new Timestamp(new Date().getTime()), new MessageContract("1", "1", 3, "John", "hey", new Timestamp(new Date().getTime())), null));
        mockMvc.perform(get("/api/v1/messages").param("offset", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(conversationService).getPaginatedConversations(3, 1, 20);
    }

    /**
     * verifies that when the messages for a user are requested with a valid format, an ok status is returned
     * @throws Exception
     */
    @Test
    void getAllMessagesTestValid() throws Exception {
        Mockito.when(messageService.getPaginatedMessages("1", 0, 20)).thenReturn(new PageImpl<>(List.of(new MessageEntity("hey", 1, "John"))));
        Mockito.when(conversationService.isInConversation(3, "1")).thenReturn(true);
        mockMvc.perform(get("/api/v1/messages/1"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(messageService).getPaginatedMessages("1", 0, 20);
    }

    /**
     *  Verifies that when a message is posted with the correct format, an ok status is returned
     * @throws Exception
     */
    @Test
    void sendMessageTestValid() throws Exception {
        Mockito.when(conversationService.isInConversation(3, "1")).thenReturn(true);
        var body = """
                {
                    "messageContent": "hey",
                    "sentBy": 3,
                    "senderName": "a"
                }
                """;
        mockMvc.perform(post("/api/v1/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(messageService).createMessage(any(), any());
    }

    /**
     *  Verifies that when a message is deleted with the correct format, an ok status is returned
     * @throws Exception
     */
    @Test
    void deleteMessageTestValid() throws Exception {
        Mockito.when(conversationService.isInConversation(3, "1")).thenReturn(true);
        mockMvc.perform(delete("/api/v1/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(messageService).deleteMessage("1");
    }

    /**
     *  Verifies that when a conversation is updated with the correct format, an ok status is returned
     * @throws Exception
     */
    @Test
    void updateConversationTestValid() throws Exception {
        Mockito.when(conversationService.isInConversation(3, "1")).thenReturn(true);
        mockMvc.perform(patch("/api/v1/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(conversationService).updateConversation(any(), any());
    }

    /**
     *  Verifies that when a conversation is created with the correct format, an ok status is returned
     * @throws Exception
     */
    @Test
    void createConversationTestValid() throws Exception {
        Mockito.when(conversationService.isInConversation(3, "1")).thenReturn(true);
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2]"))
                .andExpect(status().isOk())
                .andReturn();
        Mockito.verify(conversationService).createConversation(any());
    }

}

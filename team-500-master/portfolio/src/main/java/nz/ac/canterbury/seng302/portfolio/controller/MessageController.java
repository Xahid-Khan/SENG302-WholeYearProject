package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.mapping.ConversationMapper;
import nz.ac.canterbury.seng302.portfolio.mapping.MessageMapper;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.contract.ConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.MessageContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseMessageContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.ConversationService;
import nz.ac.canterbury.seng302.portfolio.service.MessageService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * The controller for the message endpoints.
 */
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController extends AuthenticatedController{

    @Autowired private MessageService messageService;

    @Autowired private ConversationService conversationService;

    @Autowired private MessageMapper messageMapper;

    @Autowired private ConversationMapper conversationMapper;

    @Autowired private UserAccountService userAccountService;

    /**
     * This is similar to autowiring, but apparently recommended more than field injection.
     *
     * @param authStateService   an AuthStateService
     * @param userAccountService a UserAccountService
     */
    protected MessageController(AuthStateService authStateService, UserAccountService userAccountService) {
        super(authStateService, userAccountService);
    }

    /**
     * Getting All Previous Conversations:
     * Getting Paginated Conversations
     * - GET /api/v1/messages
     * - Status 200 OK
     * - Authentication: Student
     * - Content Type: application/json
     * - Body: {
     * 	offset: int
     * 	limit: int
     *  }
     *  - Response: List<ConversationContract>
     *
     * @param principal Authentication Principal
     * @return List of ConversationContracts
     */
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<ConversationContract>> getAllPaginatedConversations(@AuthenticationPrincipal PortfolioPrincipal principal,@RequestParam("offset") Optional<Integer> offset) {
        try {
            if (offset.isPresent() && offset.get().toString().equals("undefined")) {
                offset = Optional.empty();
            }

            int offsetValue = offset.orElse(0);

            if (offsetValue < 0) {
                offsetValue = 0;
            }

            Page<ConversationEntity> conversationsPage = conversationService.getPaginatedConversations(getUserId(principal), offsetValue,20);
            // Convert page to list
            List<ConversationEntity> conversationModels = conversationsPage.getContent();
            // Convert list to list of contracts
            List<ConversationContract> contracts = conversationModels.stream().map(conversationMapper::toContract).toList();


            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Loading Paginated Messages
     * - GET /api/v1/messages/{conversationId}
     * - Status 200 OK
     * - Authentication: Conversation Member
     * - Content Type: application/json
     * - Body: {
     * 	offset: int
     * 	limit: int
     *  }
     * - Response: List<MessageContract>
     *
     * @param principal Authentication Principal
     * @param conversationId Conversation Id
     * @param offset The number of pages to skip
     * @return
     */
    @GetMapping(value = "/{conversationId}", produces = "application/json")
    public ResponseEntity<List<MessageContract>> getPaginatedMessages(@AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable("conversationId") String conversationId, @RequestParam("offset") Optional<Integer> offset) {
        try {
            //Authenticates that the user is a member of the conversation
            if (!conversationService.isInConversation(getUserId(principal), conversationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (offset.isPresent() && offset.get().toString().equals("undefined")) {
                offset = Optional.empty();
            }

            int offsetValue = offset.orElse(0);

            if (offsetValue < 0) {
                offsetValue = 0;
            }

            Page<MessageEntity> messagesPage = messageService.getPaginatedMessages(conversationId, offsetValue, 20);
            // Convert page to list
            List<MessageEntity> messageEntities = messagesPage.getContent();
            // Convert list to list of contracts
            List<MessageContract> contracts = messageEntities.stream().map(messageMapper::toContract).toList();

            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * This is the endpoint for creating a new conversation.
     *
     * Send Message:
     * - POST /api/v1/messages
     * - Status 200 OK
     * - Authentication: Student
     * - Content Type: application/json
     * - Body: userIds
     *
     * @param principal Authentication Principal
     * @return Response Entity
     */
    @PostMapping(value = "", produces = "application/json")
    public ResponseEntity<?> createConversation(@AuthenticationPrincipal PortfolioPrincipal principal, @RequestBody List<Integer> userIds) {
        try {

            ConversationContract response = conversationService.createConversation(new BaseConversationContract(userIds));
            return ResponseEntity.ok().build();

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * This is the endpoint for updating a conversation.
     *
     * Send Message:
     * - DELETE /api/v1/messages/{conversationId}
     * - Status 200 OK
     * - Body: userIds
     * - Authentication: Student
     * - Content Type: application/json
     *
     * @param principal Authentication Principal
     * @return Response Entity
     */
    @PatchMapping(value = "/{conversationId}", produces = "application/json")
    public ResponseEntity<?> updateConversation(@AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable String conversationId, @RequestBody List<Integer> userIds) {
        try {
            int userId = getUserId(principal);

            //if the user is not in the conversation, they cannot send a message
            if (!conversationService.isInConversation(userId, conversationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            ConversationContract response = conversationService.updateConversation(new BaseConversationContract(userIds), conversationId);

            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * This is the endpoint for creating a new message.
     *
     * Send Message:
     * - POST /api/v1/messages/{userIds}
     * - Status 200 OK
     * - Authentication: Student
     * - Content Type: application/json
     * - Body: message content
     *
     * @param principal Authentication Principal
     * @param messageContract The contract for the message to be created
     * @param conversationId The id of the conversation of the message
     * @return The created message
     */
    @PostMapping(value = "/{conversationId}", produces = "application/json")
    public ResponseEntity<?> sendMessage(@AuthenticationPrincipal PortfolioPrincipal principal, @RequestBody BaseMessageContract messageContract, @PathVariable String conversationId) {
        try {
            int userId = getUserId(principal);

            //if the user is not in the conversation, they cannot send a message
            if (!conversationService.isInConversation(userId, conversationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            MessageContract response = messageService.createMessage(conversationId, messageContract);

            return ResponseEntity.ok().build();

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Delete Message:
     * - DELETE /api/v1/messages/{userIds}
     * - Status 200 OK
     * - Authentication: Student
     * - Content Type: application/json
     * - Body: message id
     *
     */
    @DeleteMapping(value = "/{conversationId}", produces = "application/json")
    public ResponseEntity<?> deleteMessage(@AuthenticationPrincipal PortfolioPrincipal principal, @RequestBody String messageId, @PathVariable String conversationId) {
        try {
            int userId = getUserId(principal);

            //if the user is not in the conversation, they cannot send a message
            if (!conversationService.isInConversation(userId, conversationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            messageService.deleteMessage(messageId);
            return ResponseEntity.ok().build();

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping(value = "/all-users", produces = "application/json")
    public ResponseEntity<?> getAllUsers(){
        try {
            PaginatedUsersResponse paginatedUsers = userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.USERNAME, true);
            List<String> usernames = new ArrayList<>();
            List<String> userIds = new ArrayList<>();
            for (UserResponse user: paginatedUsers.getUsersList()) {
                usernames.add(user.getUsername());
                userIds.add(user.getId() + "");
            }
            Map<String, List<String>> result = new HashMap<>();
            result.put("usernames", usernames);
            result.put("userIds", userIds);
            return ResponseEntity.ok().body(result);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * This is the endpoint for a user reading all new messages.
     *
     * Send Message:
     * - POST /api/v1/messages/read/{conversationId}
     * - Status 200 OK
     * - Authentication: Student
     * - Content Type: application/json
     *
     * @param principal Authentication Principal
     * @param conversationId The id of the conversation of the message
     */
    @PostMapping(value = "/read/{conversationId}")
    public ResponseEntity<?> readMessage(@AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable String conversationId) {
        try {
            int userId = getUserId(principal);

            //if the user is not in the conversation, they cannot send a message
            if (!conversationService.isInConversation(userId, conversationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            conversationService.userReadMessages(userId, conversationId);

            return ResponseEntity.ok().build();

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}

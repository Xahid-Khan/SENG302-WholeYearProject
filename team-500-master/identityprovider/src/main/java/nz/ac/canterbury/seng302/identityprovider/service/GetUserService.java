package nz.ac.canterbury.seng302.identityprovider.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.Query;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.mapping.UserMapper;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GetUserService {
  private static final HashSet<String> validOrderByFieldNames = new HashSet<String>(
          List.of(new String[]{"name", "username", "nickname", "roles"}));

  @Autowired private UserRepository repository;

  @Autowired private UserMapper userMapper;

  @Autowired private SessionFactory sessionFactory;

  @Value("${spring.datasource.driverClassName}")
  private String dbDriverClassName;


  /**
   * This is a GRPC user serivce method that is beign over-ridden to get the user details and encase
   * them into a User Response body. if the user is not found the User response is set to null
   *
   * @param request the request containing the user ID to get
   * @return a UserResponse with the correct information if found
   */
  public UserResponse getUserAccountById(GetUserByIdRequest request) {
    int userId = request.getId();
    var userFound = repository.findById(userId);
    return userFound != null ? userMapper.toUserResponse(userFound) : null;
  }

  /**
   * GRPC service method that provides a list of user details with a caller-supplied sort order,
   * maximum length, and offset.
   *
   *
   * @param request parameters from the caller
   * @return a PaginatedUsersResponse filled in
   */
  public PaginatedUsersResponse getPaginatedUsers(GetPaginatedUsersRequest request) throws Exception {
    var orderByFields = request.getPaginationRequestOptions().getOrderBy().split("\\|", 2);

    if (orderByFields.length != 2) {
      throw new IllegalArgumentException("Please provide an orderBy field name, pipe symbol, followed by 'asc' or 'desc'.");
    }
    boolean ascending = orderByFields[1].equals("asc");
    var orderByField = orderByFields[0];

    var limit = request.getPaginationRequestOptions().getLimit();
    var offset = request.getPaginationRequestOptions().getOffset();

    // Validate inputs
    if (!validOrderByFieldNames.contains(orderByField) || limit <= 0 || offset < 0) {
      throw new IllegalArgumentException("Please provide a valid orderBy field name.");
    }

    try (Session session = sessionFactory.openSession()) {
      Query countQuery = session.createQuery("SELECT COUNT(u.id) FROM UserModel u");
      int totalCount = (int) (long) countQuery.getSingleResult();

      List<UserModel> resultList;
      if (orderByField.equals("roles")) {
        // Receive IDs in order by roles

        String groupConcatFunctionName;
        if (dbDriverClassName.contains("h2")) {
          groupConcatFunctionName = "string_agg";
        }
        else if (dbDriverClassName.contains("mariadb")) {
          groupConcatFunctionName = "group_concat";
        }
        else {
          throw new RuntimeException("GetUserService running with unknown database vendor. Cannot determine group_concat - equivalent function for this vendor.");
        }

        // Strictly hard-coded values are used, so this isn't a SQL injection vector.
        var queryString = String.format(
          "SELECT u.id FROM UserModel u JOIN u.roles r GROUP BY u.id ORDER BY %s ((case when r = ?1 then 'student' else (case when r=?2 then 'teacher' else 'course_administrator' end) end), ',') %s",
          groupConcatFunctionName,
          (ascending) ? "ASC" : "DESC"
        );

        var query = session.createQuery(queryString, Integer.class)
            .setParameter(1, UserRole.STUDENT)
            .setParameter(2, UserRole.TEACHER)
            .setFirstResult(offset)
            .setMaxResults(limit);

        // Get the UserModels for the IDs
        var idList = query.getResultList();
        session.close();  // Close the session early so the repository query will work.

        // findAllById doesn't guarantee result order, so we need to manually re-order the data to match idList.
        Map<Integer, UserModel> resultsById = new HashMap<>();
        repository.findAllById(idList).forEach(user -> resultsById.put(user.getId(), user));
        resultList = new ArrayList<>(idList.size());
        for (var userId : idList) {
          resultList.add(resultsById.get(userId));
        }
      }
      else {
        var queryOrderByAttributes = switch (orderByField) {
          case "name" -> List.of("firstName", "middleName", "lastName");
          case "username" -> List.of("username");
          case "nickname" -> List.of("nickname");
          default -> throw new IllegalArgumentException("Unsupported orderBy field");
        };

        var queryOrderByComponent = queryOrderByAttributes.stream()
            .map(component -> String.format("%s %s", component, ((ascending) ? "ASC" : "DESC")))
            .collect(Collectors.joining(", "));

        var query = session.createQuery("FROM UserModel ORDER BY " + queryOrderByComponent, UserModel.class)
            .setFirstResult(offset)
            .setMaxResults(limit);
        resultList = query.getResultList();
      }

      return PaginatedUsersResponse.newBuilder()
          .addAllUsers(resultList.stream().map(userMapper::toUserResponse).toList())
              .setPaginationResponseOptions(PaginationResponseOptions.newBuilder()
                      .setResultSetSize(totalCount)
          .build() ).build();
    }
  }
}

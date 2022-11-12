package nz.ac.canterbury.seng302.portfolio.model;

/**
 * Enumeration of the valid orderBy values for {@link nz.ac.canterbury.seng302.portfolio.service.UserAccountService#getPaginatedUsers(int, int, GetPaginatedUsersOrderingElement)}.
 */
public enum GetPaginatedUsersOrderingElement {
  NAME,
  USERNAME,
  NICKNAME,
  ROLES
}

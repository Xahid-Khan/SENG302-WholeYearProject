package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * The database representation of a Sprint.
 *
 * <p>Pair this with {@link nz.ac.canterbury.seng302.portfolio.repository.SprintRepository} to read
 * and write instances of this to the database.
 */
@Entity
@Table(name = "sprint")
public class SprintEntity extends PortfolioEntity {
  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProjectEntity project;

  @Column(nullable = false)
  private String name;

  @Column(length = 1024)
  private String description;

  @Column(nullable = false)
  private Instant startDate;

  @Column(nullable = false)
  private Instant endDate;

  @Column(nullable = false)
  private String colour;

  protected SprintEntity() {}

  /**
   * Creates a new Sprint entity.
   *
   * @param name the name of the sprint
   * @param description the description of the sprint
   * @param startDate the starting date of the sprint
   * @param endDate the ending date of the sprint
   * @param colour the displayed colour of the sprint
   */
  public SprintEntity(
      String name, String description, Instant startDate, Instant endDate, String colour) {
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.colour = colour;
  }

  @Override
  public String toString() {
    return String.format(
        "Sprint [id=%s, projectId=%s]", getId(), (this.project != null) ? project.getId() : "-1");
  }

  public ProjectEntity getProject() {
    return project;
  }

  public void setProject(ProjectEntity project) {
    this.project = project;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getStartDate() {
    return startDate;
  }

  public void setStartDate(Instant startDate) {
    this.startDate = startDate;
  }

  public Instant getEndDate() {
    return endDate;
  }

  public void setEndDate(Instant endDate) {
    this.endDate = endDate;
  }

  public String getColour() {
    return colour;
  }

  public void setColour(String colour) {
    this.colour = colour;
  }

  /**
   * Calculates the orderNumber of this sprint entity by searching through its project.
   *
   * @return the orderNumber of this sprint in the project
   */
  public long getOrderNumber() {
    var sprints = project.getSprints();
    for (int i = 0; i < sprints.size(); i++) {
      if (sprints.get(i).getId().equals(getId())) {
        return i + 1;
      }
    }

    throw new IllegalStateException(
        "this.project does not contain this sprint, so getOrderNumber is impossible.");
  }
}

package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

/** Entity class for events. */
@Entity
@Table(name = "event")
public class EventEntity extends PortfolioEntity {
  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProjectEntity project;

  @Length(message = "Name must be between 1 and 32 characters", min = 1, max = 32)
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", length = 1024)
  private String description;

  @NotNull
  @Column(name = "start_date")
  private Instant startDate;

  @NotNull
  @Column(name = "end_date")
  private Instant endDate;

  public ProjectEntity getProject() {
    return project;
  }

  public void setProject(ProjectEntity project) {
    this.project = project;
  }

  public @NotNull Instant getEndDate() {
    return endDate;
  }

  public void setEndDate(@NotNull Instant endDate) {
    this.endDate = endDate;
  }

  public @NotNull Instant getStartDate() {
    return startDate;
  }

  public void setStartDate(@NotNull Instant startDate) {
    this.startDate = startDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String title) {
    this.name = title;
  }

  protected EventEntity() {}

  /**
   * Creates a new event entity.
   *
   * @param name          the name of the event
   * @param description   the description of the event
   * @param startDate     the starting date of the event
   * @param endDate       the end date of the event
   */
  public EventEntity(
      String name, String description, @NotNull Instant startDate, @NotNull Instant endDate) {
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Calculates the orderNumber of this event entity by searching through its project.
   *
   * @return the orderNumber of this event in the project
   */
  public long getOrderNumber() {
    var events = project.getEvents();
    for (int i = 0; i < events.size(); i++) {
      if (events.get(i).getId().equals(getId())) {
        return i + 1;
      }
    }

    throw new IllegalStateException(
        "this.project does not contain this event, so getOrderNumber is impossible.");
  }
}

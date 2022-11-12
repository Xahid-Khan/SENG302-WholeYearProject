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

/** Entity class for milestones. */
@Entity
@Table(name = "milestone")
public class MilestoneEntity extends PortfolioEntity {
  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProjectEntity project;

  @Length(message = "Name must be between 1 and 32 characters", min = 1, max = 32)
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @NotNull
  @Column(name = "start_date")
  private Instant startDate;

  public ProjectEntity getProject() {
    return project;
  }

  public void setProject(ProjectEntity project) {
    this.project = project;
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

  protected MilestoneEntity() {}

  /**
   * Creates a new MilestoneEntity.
   *
   * @param name          the name of the milestone
   * @param description   the description of the milestone
   * @param startDate     the date on which the milestone occurs
   */
  public MilestoneEntity(String name, String description, @NotNull Instant startDate) {
    this.name = name;
    this.description = description;
    this.startDate = startDate;
  }

  /**
   * Calculates the orderNumber of this milestone entity by searching through its project.
   *
   * @return the orderNumber of this milestone in the project
   */
  public long getOrderNumber() {
    var milestones = project.getMilestones();
    for (int i = 0; i < milestones.size(); i++) {
      if (milestones.get(i).getId().equals(getId())) {
        return i + 1;
      }
    }

    throw new IllegalStateException(
        "this.project does not contain this event, so getOrderNumber is impossible.");
  }
}

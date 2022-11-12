package nz.ac.canterbury.seng302.portfolio;

import com.google.type.DateTime;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.*;

@SpringBootApplication
public class PortfolioApplication {

    @Autowired
    private ProjectRepository projectRepository;


    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void addDefaultProject() {
        int counter = 0;
        for (ProjectEntity project: projectRepository.findAll()) {
            counter++;
            break;
        }
        if (counter == 0) {
            String projectName = "Project " + LocalDate.now().getYear();
            ProjectEntity defaultProject = new ProjectEntity(projectName,
                    "",
                    LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(), // toInstant() converts the date to UTC
                    LocalDate.now().plusDays(1).plusMonths(8).atStartOfDay(ZoneId.systemDefault()).toInstant());
            projectRepository.save(defaultProject);
        }
    }

}



package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.SortingParameterEntity;
import org.springframework.data.repository.CrudRepository;

public interface SortingParametersRepository
    extends CrudRepository <SortingParameterEntity, Integer> {}

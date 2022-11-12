package nz.ac.canterbury.seng302.portfolio.service;

import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.model.entity.SortingParameterEntity;
import nz.ac.canterbury.seng302.portfolio.repository.SortingParametersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SortingParametersService {

  @Autowired
  private SortingParametersRepository sortingRepository;

  public void saveSortingParams(int userId, String sortingParam, boolean reverseOrder) {
    SortingParameterEntity sortingData = new SortingParameterEntity(userId, sortingParam,
        reverseOrder);
    if (checkExistance(userId)) {
      sortingRepository.deleteById(userId);
    }
    sortingRepository.save(sortingData);
  }

  public SortingParameterEntity getSortingParams(int userId) {
    try {
      SortingParameterEntity data = sortingRepository.findById(userId).get();
      return data;
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  public boolean checkExistance(int userId) {
    return sortingRepository.existsById(userId);
  }

}

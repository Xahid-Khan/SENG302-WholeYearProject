package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;
import nz.ac.canterbury.seng302.portfolio.model.entity.PortfolioEntity;

/**
 * * Interface used to denote that which is mappable, as well as ensure that mapping is actually
 * done. Generics are used where E is an entity type and C is a contract type.
 *
 * @param <E> the PortfolioEntity type
 * @param <B> EITHER the base contract, or the contract (ideally base contract, full if no base)
 * @param <C> the full contract
 */
public interface Mappable<
    E extends PortfolioEntity, B extends Contractable, C extends Contractable> {

  /**
   * Maps a contract to an entity.
   *
   * @param contract the contract (ideally a base contract) to convert to an entity
   * @return the entity representation of a contract
   */
  E toEntity(B contract);

  /**
   * Maps an entity to a contract.
   *
   * @param entity the entity to map to a contract
   * @return the contract representation of an entity
   */
  C toContract(E entity);
}

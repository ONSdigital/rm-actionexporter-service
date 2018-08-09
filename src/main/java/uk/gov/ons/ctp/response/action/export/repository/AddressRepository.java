package uk.gov.ons.ctp.response.action.export.repository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.Address;

/** JPA repository for Address entities */
@Repository
public interface AddressRepository extends BaseRepository<Address, UUID> {

  /**
   * Check repository for sampleunitrefpk existence
   *
   * @param sampleUnitRef to check for existence
   * @return boolean whether exists
   */
  boolean existsBySampleUnitRef(String sampleUnitRef);
}

package uk.gov.ons.ctp.response.action.export.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.Address;

/** JPA repository for Address entities */
@Repository
public interface AddressRepository extends BaseRepository<Address, UUID> {

  /**
   * Check repository for sampleunitrefpk existence
   *
   * @param sampleUnitRefpk to check for existence
   * @return boolean whether exists
   */
  @Query(
      value = "select exists(select 1 from actionexporter.address where sampleunitrefpk=:p_surpk)",
      nativeQuery = true)
  boolean tupleExists(@Param("p_surpk") String sampleUnitRefpk);
}

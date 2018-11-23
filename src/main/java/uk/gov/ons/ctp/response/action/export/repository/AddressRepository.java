package uk.gov.ons.ctp.response.action.export.repository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.Address;

/** JPA repository for Address entities */
@Repository
public interface AddressRepository extends BaseRepository<Address, UUID> {}

package uk.gov.ons.ctp.response.action.export.repository;

import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;

/** JPA repository for ActionRequest entities */
@Repository
public interface ActionRequestRepository extends BaseRepository<ActionRequestInstruction, UUID> {

  boolean existsByExportJobIdIsNull();

  @Modifying
  @Query(
      "UPDATE ActionRequestInstruction r SET r.exportJobId = :exportJobId "
          + "WHERE r.exportJobId IS NULL")
  void updateActionsWithExportJob(@Param("exportJobId") UUID exportJobId);

  Stream<ActionRequestInstruction> findByExportJobId(UUID exportJobId);

  /**
   * Check repository for actionId existence
   *
   * @param actionId to check for existence
   * @return boolean whether exists
   */
  boolean existsByActionId(UUID actionId);
}

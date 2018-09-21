package uk.gov.ons.ctp.response.action.export.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;

/** JPA repository for ActionRequest entities */
@Repository
public interface ActionRequestRepository extends BaseRepository<ActionRequestInstruction, UUID> {

  @Modifying
  @Transactional
  @Query(
      "UPDATE ActionRequestInstruction r SET r.exportJobId = :exportJobId "
          + "WHERE r.exportJobId IS NULL")
  void updateActionsWithExportJob(@Param("exportJobId") UUID exportJobId);

  int countByExportJobId(UUID exportJobId);

  Stream<ActionRequestInstruction> findByExportJobId(UUID exportJobId);

  @Query(
      "SELECT r.actionId FROM ActionRequestInstruction r WHERE r.responseRequired = "
          + "TRUE AND r.exportJobId = :exportJobId")
  List<UUID> retrieveResponseRequiredForJob(@Param("exportJobId") UUID exportJobId);

  /**
   * Check repository for actionId existence
   *
   * @param actionId to check for existence
   * @return boolean whether exists
   */
  boolean existsByActionId(UUID actionId);
}

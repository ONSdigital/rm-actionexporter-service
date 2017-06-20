package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Representation of a message being sent.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Slf4j
public class ExportMessage {

  private Map<String, List<UUID>> actionRequestIds = new HashMap<String, List<UUID>>();
  private Map<String, ByteArrayOutputStream> outputStreams = new HashMap<String, ByteArrayOutputStream>();

  /**
   * Return actionIds for actionRequests for key.
   *
   * @param key for which to get actionIds.
   * @return List of actionIds for key.
   */
  public List<String> getActionRequestIds(String key) {
    List<UUID> list = actionRequestIds.get(key);
    List<String> ids = new ArrayList<String>();

    for (UUID uuid : list) {
      ids.add(uuid.toString());
    }
    return ids;
  }

  /**
   * Checks if list are empty
   * @return boolean
   */
  public boolean isEmpty() {
      return actionRequestIds.isEmpty() || outputStreams.isEmpty();
  }

  /**
   * Return all actionIds.
   * @return List of all actionIds.
   */
  public List<String> getMergedActionRequestIdsAsStrings() {
    List<String> actionIds = new ArrayList<String>();

    actionRequestIds.forEach((key, mergeIds) -> {
        for (UUID uuid : mergeIds) {
        actionIds.add(uuid.toString());
      }
    });
    return actionIds;
  }

  /**
   * Return all outputStreams merged.
   * @return ByteArrayOutputStream.
   */
  public ByteArrayOutputStream getMergedOutputStreams() {

    ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();
    for (Map.Entry<String, ByteArrayOutputStream> outputStream : outputStreams.entrySet()) {
      try {
        mergedStream.write(outputStream.getValue().toByteArray());
      } catch (IOException ex) {
        log.error("Error merging ExportMessage ByteArrayOutputStreams: {}", ex.getMessage());
        return null;
      }
    }
    return mergedStream;
  }
}

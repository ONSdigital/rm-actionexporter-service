package uk.gov.ons.ctp.response.action.export.domain;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

/** Representation of a message being sent. */
@CoverageIgnore
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ExportMessage {
  private static final Logger log = LoggerFactory.getLogger(ExportMessage.class);

  private Map<String, List<UUID>> actionRequestIds = new HashMap<>();
  private Map<String, ByteArrayOutputStream> outputStreams = new HashMap<>();

  public ExportMessage merge(ExportMessage message) {
    actionRequestIds.putAll(message.getActionRequestIds());
    outputStreams.putAll(message.getOutputStreams());
    return this;
  }

  /**
   * Checks if list are empty
   *
   * @return boolean
   */
  public boolean isEmpty() {
    return actionRequestIds.isEmpty() || outputStreams.isEmpty();
  }

  /**
   * Return all actionIds.
   *
   * @return List of all actionIds.
   */
  public List<String> getMergedActionRequestIdsAsStrings() {
    List<String> actionIds = new ArrayList<>();

    actionRequestIds.forEach(
        (key, mergeIds) -> {
          for (UUID uuid : mergeIds) {
            actionIds.add(uuid.toString());
          }
        });
    return actionIds;
  }

  /**
   * Return all outputStreams merged.
   *
   * @return ByteArrayOutputStream.
   */
  public ByteArrayOutputStream getMergedOutputStreams() {
    ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();
    for (ByteArrayOutputStream outputStream : outputStreams.values()) {
      try {
        mergedStream.write(outputStream.toByteArray());
      } catch (IOException ex) {
        log.error("Error merging ExportMessage ByteArrayOutputStreams", ex);
        return null;
      }
    }
    return mergedStream;
  }
}

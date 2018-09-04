package uk.gov.ons.ctp.response.action.export.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ons.ctp.response.action.export.ByteArraySteamHelper.baosWithData;

import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class ExportMessageTest {

  @Test
  public void shouldMergeExportMessage() throws IOException {
    // Given
    Map<String, List<UUID>> ids =
        Collections.singletonMap("BSNOT", Collections.singletonList(UUID.randomUUID()));
    Map<String, ByteArrayOutputStream> streams =
        Collections.singletonMap("BSNOT", baosWithData("FirstFile"));
    ExportMessage exportMessage = new ExportMessage();
    exportMessage.getActionRequestIds().putAll(ids);
    exportMessage.getOutputStreams().putAll(streams);

    // When
    Map<String, List<UUID>> moreIds =
        Collections.singletonMap("BSPRENOT", Collections.singletonList(UUID.randomUUID()));
    Map<String, ByteArrayOutputStream> moreStreams =
        Collections.singletonMap("BSPRENOT", baosWithData("SecondFile"));
    ExportMessage anotherExportMessage = new ExportMessage(moreIds, moreStreams);
    ExportMessage merged = exportMessage.merge(anotherExportMessage);

    // Then
    assertThat(merged.getActionRequestIds()).containsKeys("BSNOT", "BSPRENOT");
    assertThat(merged.getActionRequestIds())
        .containsValues(ids.get("BSNOT"), moreIds.get("BSPRENOT"));
    assertThat(merged.getOutputStreams()).containsKeys("BSNOT", "BSPRENOT");
    assertThat(merged.getOutputStreams().get("BSNOT").toString()).isEqualTo("FirstFile");
    assertThat(merged.getOutputStreams().get("BSPRENOT").toString()).isEqualTo("SecondFile");
  }

  @Test
  public void shouldMergeIds() {
    // Given
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    Map<String, List<UUID>> ids =
        ImmutableMap.of(
            "BSNOT",
            Collections.singletonList(first),
            "BSPRENOT",
            Collections.singletonList(second));
    ExportMessage exportMessage = new ExportMessage(ids, null);

    // When
    List<String> allIds = exportMessage.getMergedActionRequestIdsAsStrings();

    // Then
    assertThat(allIds).contains(first.toString(), second.toString());
  }

  @Test
  public void shouldMergeStreams() throws IOException {
    // Given
    ByteArrayOutputStream first = baosWithData("first");
    ByteArrayOutputStream second = baosWithData("second");
    ExportMessage exportMessage =
        new ExportMessage(null, ImmutableMap.of("BSNOT", first, "BSPRENOT", second));

    // When
    ByteArrayOutputStream mergedStreams = exportMessage.getMergedOutputStreams();

    // Then
    assertThat(mergedStreams).hasToString("firstsecond");
  }
}

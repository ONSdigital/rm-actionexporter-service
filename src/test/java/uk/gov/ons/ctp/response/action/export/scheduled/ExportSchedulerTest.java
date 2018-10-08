package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.config.DataGrid;

@RunWith(MockitoJUnitRunner.class)
public class ExportSchedulerTest {

  @Mock private RedissonClient redissonClient;
  @Mock private AppConfig appConfig;
  @Mock private ExportProcessor exportProcessor;
  @InjectMocks private ExportScheduler exportScheduler;

  @Test
  public void shouldLockAndUnlock() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);

    // When
    exportScheduler.scheduleExport();

    // Verify
    InOrder inOrder = inOrder(mockLock, exportProcessor);
    inOrder.verify(mockLock).tryLock(anyLong(), any(TimeUnit.class));
    inOrder.verify(exportProcessor).processExport();
    inOrder.verify(mockLock).unlock();
  }

  @Test
  public void shouldUnlockWhenExceptionThrown() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);
    doThrow(RuntimeException.class).when(exportProcessor).processExport();

    // When
    exportScheduler.scheduleExport();

    // Verify
    InOrder inOrder = inOrder(mockLock, exportProcessor);
    inOrder.verify(mockLock).tryLock(anyLong(), any(TimeUnit.class));
    inOrder.verify(exportProcessor).processExport();
    inOrder.verify(mockLock).unlock();
  }
}

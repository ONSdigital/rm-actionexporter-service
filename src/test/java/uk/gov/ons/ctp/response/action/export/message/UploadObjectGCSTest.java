package uk.gov.ons.ctp.response.action.export.message;

import static org.mockito.Mockito.*;

import com.google.cloud.storage.*;
import java.io.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UploadObjectGCSTest {

  @Mock private Storage storage;
  @InjectMocks private UploadObjectGCS uploadObjectGCS;

  @Test
  public void shouldUploadFileToGCS() {
    ByteArrayOutputStream mockFile = new ByteArrayOutputStream();
    String bucket = "testBucket";
    String filename = "testFile.csv";
    boolean actualResponse = uploadObjectGCS.uploadObject(filename, bucket, mockFile);
    Assert.assertTrue(actualResponse);
  }

  @Test
  public void shouldErrorOutWhileUploadToGCS() {
    ByteArrayOutputStream mockFile = new ByteArrayOutputStream();
    String bucket = "testBucket";
    String filename = "testFile.csv";
    BlobId blobId = BlobId.of(bucket, filename);
    BlobInfo mockBlobInfo = BlobInfo.newBuilder(blobId).build();
    Blob blob = mock(Blob.class);
    when(storage.create(mockBlobInfo, mockFile.toByteArray())).thenThrow(StorageException.class);
    boolean actualResponse = uploadObjectGCS.uploadObject(filename, bucket, mockFile);
    Assert.assertFalse(actualResponse);
  }
}

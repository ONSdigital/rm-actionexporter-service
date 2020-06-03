package uk.gov.ons.ctp.response.action.export.message;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;

@RunWith(MockitoJUnitRunner.class)
public class UploadObjectGCSTest {

    @Mock private Storage storage;
    @InjectMocks private UploadObjectGCS uploadObjectGCS;

    @Test
    public void shouldUploadFileToGCS() {
        ByteArrayOutputStream mockFile = new ByteArrayOutputStream();
        String bucket = "testBucket";
        String filename = "testFile.csv";
        boolean actualResponse = uploadObjectGCS.uploadObject(filename,bucket,mockFile);
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
        boolean actualResponse = uploadObjectGCS.uploadObject(filename,bucket,mockFile);
        Assert.assertFalse(actualResponse);
    }
}

package uk.gov.ons.ctp.response.action.export.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.storage.*;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

@Component
public class UploadObjectGCS {
  private static final Logger log = LoggerFactory.getLogger(UploadObjectGCS.class);
  private final Storage storage;

  public UploadObjectGCS(Storage storage) {
    this.storage = storage;
  }

  public boolean uploadObject(String filename, String bucket, ByteArrayOutputStream data) {
    BlobId blobId = BlobId.of(bucket, filename);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    Boolean isSuccess = false;
    log.with("file_name", filename).with("bucket", bucket).info("Uploading to GCS bucket");
    try {
      storage.create(blobInfo, data.toByteArray());
      isSuccess = true;
      log.with("file_name", filename).with("bucket", bucket).info("Upload Successful!");
    } catch (StorageException exception) {
      log.with("exception", exception)
          .with("file_name", filename)
          .error("Error uploading the generated file to GCS");
    }
    return isSuccess;
  }
}

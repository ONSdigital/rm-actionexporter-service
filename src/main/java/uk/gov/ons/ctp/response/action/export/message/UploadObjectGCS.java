package uk.gov.ons.ctp.response.action.export.message;

import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UploadObjectGCS {
  private static final Logger log = LoggerFactory.getLogger(UploadObjectGCS.class);
  private final Storage storage;

  public UploadObjectGCS(Storage storage) {
    this.storage = storage;
  }

  public boolean uploadObject(String filename, String bucket, byte[] data) {
    BlobId blobId = BlobId.of(bucket, filename);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
    Boolean isSuccess = false;
    log.info("file_name: ", filename + " bucket: " + bucket + ", Uploading to GCS bucket");
    try {
      storage.create(blobInfo, data);
      isSuccess = true;
      log.info("file_name: "+ filename + " bucket: " + bucket + ", Upload Successful!");
    } catch (StorageException exception) {
      log.error(
          "file_name: " + filename  + " bucket: " + bucket + ", Error uploading the generated file to GCS", exception);
    }
    return isSuccess;
  }
}

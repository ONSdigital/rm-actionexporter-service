package uk.gov.ons.ctp.response.action.export.message;

import com.google.cloud.storage.*;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;

@Component
public class UploadObjectGCS {

  private AppConfig appConfig;

  private static final Logger log = LoggerFactory.getLogger(UploadObjectGCS.class);
  private final Storage storage;

  public UploadObjectGCS(Storage storage, AppConfig appConfig) {
    this.storage = storage;
    this.appConfig = appConfig;
  }

  public boolean uploadObject(String filename, String bucket, byte[] data) {
    String prefix = appConfig.getGcp().getBucket().getPrefix();
    String bucketFilename;
    if (!prefix.isEmpty()) {
      bucketFilename = prefix + File.separator + filename;
    } else {
      bucketFilename = filename;
    }

    BlobId blobId = BlobId.of(bucket, bucketFilename);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
    Boolean isSuccess = false;
    log.info("file_name: ", bucketFilename + " bucket: " + bucket + ", Uploading to GCS bucket");
    try {
      storage.create(blobInfo, data);
      isSuccess = true;
      log.info("file_name: " + bucketFilename + " bucket: " + bucket + ", Upload Successful!");
    } catch (StorageException exception) {
      log.error(
          "file_name: "
              + bucketFilename
              + " bucket: "
              + bucket
              + ", Error uploading the generated file to GCS",
          exception);
    }
    return isSuccess;
  }
}

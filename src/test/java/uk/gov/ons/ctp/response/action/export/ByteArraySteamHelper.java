package uk.gov.ons.ctp.response.action.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteArraySteamHelper {

  public static ByteArrayOutputStream baosWithData(String data) throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.write(data.getBytes());
    return stream;
  }
}

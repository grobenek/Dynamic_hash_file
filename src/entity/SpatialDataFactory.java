package entity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class SpatialDataFactory {
  public static byte[] toByteArray(SpatialData<?> spatialData) {
    return spatialData.toByteArray();
  }

  public static SpatialData<?> fromByteArray(byte[] byteArray) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {

      SpatialDataType className = SpatialDataType.values()[inputStream.readInt()];
      int bytesRead = 4;

      switch (className) {
        case PROPERTY -> {
          Property property = new Property();
          byte[] remainingBytes = Arrays.copyOfRange(byteArray, bytesRead, byteArray.length);
          property.fromByteArray(remainingBytes);
          return property;
        }
        case PARCEL -> {
          Parcel parcel = new Parcel();
          byte[] remainingBytes = Arrays.copyOfRange(byteArray, bytesRead, byteArray.length);
          parcel.fromByteArray(remainingBytes);
          return parcel;
        }
        default -> throw new IllegalStateException(
            "Cannot create Spatial data of type " + className);
      }

    } catch (IOException e) {
      throw new RuntimeException("Error during conversion from byte array.", e);
    }
  }
}

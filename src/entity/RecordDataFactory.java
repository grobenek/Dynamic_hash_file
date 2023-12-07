package entity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import structure.dynamichashfile.constant.ElementByteSize;
import structure.entity.record.Record;

public class RecordDataFactory {
  public static Record fromByteArray(byte[] byteArray) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {

      SpatialDataType className = SpatialDataType.values()[inputStream.readInt()];
      int bytesRead = ElementByteSize.intByteSize();

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

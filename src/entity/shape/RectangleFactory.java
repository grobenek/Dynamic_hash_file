package entity.shape;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RectangleFactory {
  public static Rectangle fromByteArray(byte[] byteArray) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
    DataInputStream inputStream = new DataInputStream(byteArrayInputStream);

    try {
      double firstPointWidthCoordinate = inputStream.readDouble();
      Direction firstPointWidth = Direction.valueOf(String.valueOf(inputStream.readChar()));
      double firstPointLengthCoordinate = inputStream.readDouble();
      Direction firstPointLength = Direction.valueOf(String.valueOf(inputStream.readChar()));

      double secondPointWidthCoordinate = inputStream.readDouble();
      Direction secondPointWidth = Direction.valueOf(String.valueOf(inputStream.readChar()));
      double secondPointLengthCoordinate = inputStream.readDouble();
      Direction secondPointLength = Direction.valueOf(String.valueOf(inputStream.readChar()));

      return new Rectangle(
          new GpsCoordinates(
              firstPointWidth,
              firstPointWidthCoordinate,
              firstPointLength,
              firstPointLengthCoordinate),
          new GpsCoordinates(
              secondPointWidth,
              secondPointWidthCoordinate,
              secondPointLength,
              secondPointLengthCoordinate));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}

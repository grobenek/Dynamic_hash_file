package entity;

import entity.shape.Rectangle;
import entity.shape.RectangleFactory;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import structure.dynamichashfile.LimitedString;

// TODO refactor,aby fromByteArray iba naplnilo triedu a nevytvaralo novu zbytocne
public class SpatialDataFactory {
  public static SpatialData fromByteArray(byte[] byteArray, boolean loadDataList) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {

      SpatialDataType className = SpatialDataType.values()[inputStream.readInt()];
      int identificationNumber = inputStream.readInt();

      LimitedString description = new LimitedString();
      int registrationNumber = Integer.MIN_VALUE;
      if (className == SpatialDataType.PROPERTY) {
        registrationNumber = inputStream.readInt();
        description =
            description.fromByteArray(inputStream.readNBytes(Property.getMaxDescriptionSize() + 4));
      } else {
        description =
            description.fromByteArray(inputStream.readNBytes(Parcel.getMaxDescriptionSize() + 4));
      }

      Rectangle shape =
          RectangleFactory.fromByteArray(inputStream.readNBytes(Rectangle.getByteArraySize()));

      // list deserialization
      List<SpatialData> relatedDataList = null;
      if (loadDataList) {
        int numberOfRelatedData = inputStream.readInt();
        relatedDataList = new ArrayList<>(numberOfRelatedData);

        for (int i = 0; i < numberOfRelatedData; i++) {
          if (className == SpatialDataType.PROPERTY) {
            SpatialDataType classType =
                SpatialDataType.values()[inputStream.readInt()]; // Skip class name
            int relatedIdentificationNumber = inputStream.readInt();
            LimitedString relatedDescription =
                description.fromByteArray(
                    inputStream.readNBytes(Parcel.getMaxDescriptionSize() + 4));
            Rectangle relatedShape =
                RectangleFactory.fromByteArray(
                    inputStream.readNBytes(Rectangle.getByteArraySize()));
            SpatialData property =
                new Parcel(relatedIdentificationNumber, relatedDescription, relatedShape);
            relatedDataList.add(property);
          } else if (className == SpatialDataType.PARCEL) {
            inputStream.readInt(); // Skip class name
            int relatedIdentificationNumber = inputStream.readInt();
            int relatedRegistrationNumber = inputStream.readInt();
            LimitedString relatedDescription =
                description.fromByteArray(
                    inputStream.readNBytes(Property.getMaxDescriptionSize() + 4));
            Rectangle relatedShape =
                RectangleFactory.fromByteArray(
                    inputStream.readNBytes(Rectangle.getByteArraySize()));
            SpatialData parcel =
                new Property(
                    relatedIdentificationNumber,
                    relatedRegistrationNumber,
                    relatedDescription,
                    relatedShape);
            relatedDataList.add(parcel);
          }
        }
      }

      return switch (className) {
        case PARCEL -> loadDataList
            ? new Parcel(identificationNumber, description, shape, relatedDataList)
            : new Parcel(identificationNumber, description, shape);
        case PROPERTY -> loadDataList
            ? new Property(
                identificationNumber, registrationNumber, description, shape, relatedDataList)
            : new Property(identificationNumber, registrationNumber, description, shape);
      };
    } catch (IOException e) {
      throw new RuntimeException("Error during conversion from byte array.", e);
    }
  }
}

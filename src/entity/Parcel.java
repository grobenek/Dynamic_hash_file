package entity;

import entity.shape.Rectangle;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import structure.dynamichashfile.LimitedString;
import structure.quadtree.IShapeData;

public class Parcel extends SpatialData<Property> implements IShapeData {
  private static final int MAX_RELATED_PROPERTY_LIST_SIZE = 5;
  private static final int MAX_DESCRIPTION_SIZE = 11;

  public Parcel(
      int identificationNumber,
      String description,
      Rectangle shape,
      List<Property> relatedDataList) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PROPERTY_LIST_SIZE,
        relatedDataList);
  }

  public Parcel(
      int identificationNumber,
      LimitedString description,
      Rectangle shape,
      List<Property> relatedDataList) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PROPERTY_LIST_SIZE,
        relatedDataList);
  }

  public Parcel(int identificationNumber, String description, Rectangle shape) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PROPERTY_LIST_SIZE);
  }

  public Parcel(int identificationNumber, LimitedString description, Rectangle shape) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PROPERTY_LIST_SIZE);
  }

  public Parcel(int identificationNumber, String description) {
    super(identificationNumber, MAX_DESCRIPTION_SIZE, description, MAX_RELATED_PROPERTY_LIST_SIZE);
  }

  public Parcel() {}

  public static int getMaxPropertyListSize() {
    return MAX_RELATED_PROPERTY_LIST_SIZE;
  }

  public static int getMaxDescriptionSize() {
    return MAX_DESCRIPTION_SIZE;
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(SpatialDataType.PARCEL.ordinal());
      outputStream.writeInt(getIdentificationNumber());
      outputStream.write(getDescription().toByteArray());
      outputStream.write(getShapeOfData().toByteArray());

      outputStream.writeInt(getRelatedDataList().size());
      for (SpatialData spatialData : getRelatedDataList()) {
        outputStream.writeInt(spatialData.getIdentificationNumber());
      }

      return byteArrayOutputStream.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  @Override
  public void fromByteArray(byte[] byteArray) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {

      setMaximumDescriptionSize(MAX_DESCRIPTION_SIZE);
      setMaximumRelatedDataListSize(MAX_RELATED_PROPERTY_LIST_SIZE);

      setIdentificationNumber(inputStream.readInt());

      LimitedString description = new LimitedString();

      description.fromByteArray(
          inputStream.readNBytes(
              getMaxDescriptionSize() + LimitedString.getStaticElementsByteSize()));
      setDescription(description);

      Rectangle shape = new Rectangle();
      shape.fromByteArray(inputStream.readNBytes(Rectangle.getByteArraySize()));
      setShape(shape);

      // list deserialization
      int numberOfRelatedData = inputStream.readInt();
      List<Property> relatedDataList = new ArrayList<>(numberOfRelatedData);

      for (int i = 0; i < numberOfRelatedData; i++) {
        int identificationNumber = inputStream.readInt();
        relatedDataList.add(new Property(identificationNumber, -1, ""));
      }

      setRelatedDataList(relatedDataList);

    } catch (IOException e) {
      throw new RuntimeException("Error during conversion from byte array.", e);
    }
  }

  @Override
  public String toString() {
    return super.toString("Parcel");
  }
}

package entity;

import entity.shape.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import structure.dynamichashfile.LimitedString;
import structure.quadtree.IShapeData;

public class Parcel extends SpatialData implements IShapeData {
  private static final int MAX_RELATED_PROPERTY_LIST_SIZE = 5;
  private static final int MAX_DESCRIPTION_SIZE = 11;

  public Parcel(
      int identificationNumber,
      String description,
      Rectangle shape,
      List<SpatialData> relatedDataList) {
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
      List<SpatialData> relatedDataList) {
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
  public String toString() {
    return super.toString("Parcel");
  }
}

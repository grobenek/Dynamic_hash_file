package entity;

import entity.shape.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import structure.dynamichashfile.LimitedString;
import structure.quadtree.IShapeData;

public class Property extends SpatialData implements IShapeData {
  public static final int MAX_DESCRIPTION_SIZE = 15;
  private static final int MAX_RELATED_PARCEL_LIST_SIZE = 6;
  private int registrationNumber;

  public Property(
      int identificationNumber,
      int registrationNumber,
      String description,
      Rectangle shape,
      List<SpatialData> relatedDataList) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PARCEL_LIST_SIZE,
        relatedDataList);

    this.registrationNumber = registrationNumber;
  }

  public Property(
      int identificationNumber,
      int registrationNumber,
      LimitedString description,
      Rectangle shape,
      List<SpatialData> relatedDataList) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PARCEL_LIST_SIZE,
        relatedDataList);

    this.registrationNumber = registrationNumber;
  }

  public Property(
      int identificationNumber, int registrationNumber, String description, Rectangle shape) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PARCEL_LIST_SIZE);
    this.registrationNumber = registrationNumber;
  }

  public Property(
      int identificationNumber,
      int registrationNumber,
      LimitedString description,
      Rectangle shape) {
    super(
        identificationNumber,
        MAX_DESCRIPTION_SIZE,
        description,
        shape,
        MAX_RELATED_PARCEL_LIST_SIZE);
    this.registrationNumber = registrationNumber;
  }

  public Property(int identificationNumber, int registrationNumber, String description) {
    super(identificationNumber, MAX_DESCRIPTION_SIZE, description, MAX_RELATED_PARCEL_LIST_SIZE);
    this.registrationNumber = registrationNumber;
  }

  public Property(int identificationNumber, int registrationNumber, LimitedString description) {
    super(identificationNumber, MAX_DESCRIPTION_SIZE, description, MAX_RELATED_PARCEL_LIST_SIZE);
    this.registrationNumber = registrationNumber;
  }

  public static int getMaxParcelListSize() {
    return MAX_RELATED_PARCEL_LIST_SIZE;
  }

  public static int getMaxDescriptionSize() {
    return MAX_DESCRIPTION_SIZE;
  }

  @Override
  public String toString() {
    return super.toString("Property");
  }

  public int getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(int registrationNumber) {
    this.registrationNumber = registrationNumber;
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      setBeingSerialized(true);

      outputStream.writeInt(SpatialDataType.PROPERTY.ordinal());
      outputStream.writeInt(getIdentificationNumber());
      outputStream.writeInt(registrationNumber);
      outputStream.write(getDescription().toByteArray());
      outputStream.write(getShapeOfData().toByteArray());

      outputStream.writeInt(getRelatedDataList().size());
      for (SpatialData spatialData : getRelatedDataList()) {
        outputStream.write(
            spatialData.toByteArray()); //TODO INFINITE LOOP
      }

      setBeingSerialized(false);
      return byteArrayOutputStream.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }
}

package entity;

import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import structure.dynamichashfile.LimitedString;
import structure.dynamichashfile.Record;
import structure.quadtree.IShapeData;

public class Property extends SpatialData<Parcel> implements IShapeData {
  public static final Property DUMMY_INSTANCE;
  private static final int MAX_DESCRIPTION_SIZE = 15;
  private static final int MAX_RELATED_PARCEL_LIST_SIZE = 6;
  private static final int BYTE_SIZE = 103;

  static {
    Rectangle rectangle =
        new Rectangle(
            new GpsCoordinates(Direction.S, Integer.MIN_VALUE, Direction.W, Integer.MIN_VALUE),
            new GpsCoordinates(Direction.N, Integer.MIN_VALUE, Direction.E, Integer.MIN_VALUE));

    DUMMY_INSTANCE = new Property(Integer.MIN_VALUE, Integer.MIN_VALUE, "DUMMY", rectangle);
  }

  private int registrationNumber;

  public Property(
      int identificationNumber,
      int registrationNumber,
      String description,
      Rectangle shape,
      List<Parcel> relatedDataList) {
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
      List<Parcel> relatedDataList) {
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

  public Property() {}

  public static int getMaxParcelListSize() {
    return MAX_RELATED_PARCEL_LIST_SIZE;
  }

  public static int getMaxDescriptionSize() {
    return MAX_DESCRIPTION_SIZE;
  }

  public static Record getDummyInstance() {
    return DUMMY_INSTANCE;
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
  public int getByteSize() {
    return BYTE_SIZE;
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(SpatialDataType.PROPERTY.ordinal());
      outputStream.writeInt(getIdentificationNumber());
      outputStream.writeInt(registrationNumber);
      outputStream.write(getDescription().toByteArray());
      outputStream.write(getShapeOfData().toByteArray());

      outputStream.writeInt(getRelatedDataList().size());
      for (SpatialData<?> spatialData : getRelatedDataList()) {
        outputStream.writeInt(spatialData.getIdentificationNumber());
      }

      for (int i = 0; i < MAX_RELATED_PARCEL_LIST_SIZE - getRelatedDataList().size(); i++) {
        outputStream.writeInt(Integer.MIN_VALUE);
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
      setMaximumRelatedDataListSize(MAX_RELATED_PARCEL_LIST_SIZE);

      setIdentificationNumber(inputStream.readInt());

      setRegistrationNumber(inputStream.readInt());

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
      List<Parcel> relatedDataList = new ArrayList<>(numberOfRelatedData);

      for (int i = 0; i < numberOfRelatedData; i++) {
        int identificationNumber = inputStream.readInt();
        relatedDataList.add(new Parcel(identificationNumber, ""));
      }

      // read relatedList zombie data
      for (int i = 0; i < Property.getMaxParcelListSize() - numberOfRelatedData; i++) {
        inputStream.readInt();
      }

      setRelatedDataList(relatedDataList);

    } catch (IOException e) {
      throw new RuntimeException("Error during conversion from byte array.", e);
    }
  }
}

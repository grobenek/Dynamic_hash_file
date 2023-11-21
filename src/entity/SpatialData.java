package entity;

import entity.shape.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import structure.dynamichashfile.IConvertableToBytes;
import structure.dynamichashfile.Record;
import structure.quadtree.IShapeData;

public abstract class SpatialData<T extends IShapeData> implements IShapeData, IConvertableToBytes {
  private int identificationNumber;
  private String description;
  private List<SpatialData> relatedDataList;
  private Rectangle shape;

  public SpatialData(int identificationNumber, String description, Rectangle shape) {
    this.identificationNumber = identificationNumber;
    this.description = description;
    this.shape = shape;

    this.relatedDataList = new ArrayList<>();
  }

  public SpatialData(int identificationNumber, String description) {
    this.identificationNumber = identificationNumber;
    this.description = description;

    this.relatedDataList = new ArrayList<>();
  }

  public int getIdentificationNumber() {
    return identificationNumber;
  }

  public void setIdentificationNumber(int identificationNumber) {
    this.identificationNumber = identificationNumber;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<SpatialData> getRelatedDataList() {
    return relatedDataList;
  }

  public void setRelatedDataList(List<SpatialData> relatedDataList) {
    this.relatedDataList = relatedDataList;
  }

  public void addRelatedData(SpatialData data) {
    relatedDataList.add(data);
  }

  public void removeRelatedData(SpatialData data) {
    relatedDataList.remove(data);
  }

  public void setShape(Rectangle shape) {
    this.shape = shape;
  }

  @Override
  public Rectangle getShapeOfData() {
    return shape;
  }

  public String toString(String className) {
    StringBuilder sb = new StringBuilder();
    relatedDataList.forEach(
        data -> {
          sb.append("identificationNumber=")
              .append(data.identificationNumber)
              .append(" ")
              .append("Description: ")
              .append(data.getDescription())
              .append(" ")
              .append(data.getShapeOfData());
        });

    return className
        + "{"
        + "identificationNumber="
        + identificationNumber
        + ", description='"
        + description
        + '\''
        + ", shape="
        + shape
        + '}'
        + ", relatedDataList=\n"
        + sb
        + "\n";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SpatialData<?>)) {
      return false;
    }
    SpatialData<?> castedObj = (SpatialData<?>) obj;

    return castedObj.getDescription().equals(description)
        && castedObj.identificationNumber == identificationNumber
        && ((castedObj.relatedDataList == null && relatedDataList == null)
            || (castedObj.relatedDataList != null
                && castedObj.relatedDataList.equals(relatedDataList)))
        && ((castedObj.shape == null && shape == null)
            || (castedObj.shape != null && castedObj.shape.equals(shape)));
  }

  @Override
  public int hashCode() {
    return Objects.hash(identificationNumber, description, relatedDataList, shape);
  }

  @Override
  public byte[] toByteArray() {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

    try {
      outputStream.writeInt(identificationNumber);
      outputStream.writeBytes(description);

      return byteArrayOutputStream.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  @Override
  public Record fromByteArray() {
    return null;
  }
}

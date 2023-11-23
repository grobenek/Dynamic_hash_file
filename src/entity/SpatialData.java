package entity;

import entity.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import structure.dynamichashfile.IConvertableToBytes;
import structure.dynamichashfile.LimitedString;
import structure.quadtree.IShapeData;

public abstract class SpatialData<T extends SpatialData<?>> implements IShapeData, IConvertableToBytes {
  private int maximumRelatedDataListSize;
  private int maximumDescriptionSize;
  private int identificationNumber;
  private LimitedString description;
  private List<T> relatedDataList;
  private Rectangle shape;

  public SpatialData(
      int identificationNumber,
      int maximumDescriptionSize,
      String description,
      Rectangle shape,
      int maximumRelatedDataListSize,
      List<T> relatedDataList) {

    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
    this.identificationNumber = identificationNumber;
    this.description = new LimitedString(maximumDescriptionSize, description);
    this.shape = shape;
    this.maximumDescriptionSize = maximumDescriptionSize;

    if (relatedDataList.size() > maximumRelatedDataListSize) {
      throw new IllegalArgumentException(
          "Cannot set related data list with "
              + relatedDataList.size()
              + "! Exceeded maximum list size of "
              + maximumRelatedDataListSize
              + "!");
    }

    this.relatedDataList = relatedDataList;
  }

  public SpatialData(
      int identificationNumber,
      int maximumDescriptionSize,
      LimitedString description,
      Rectangle shape,
      int maximumRelatedDataListSize,
      List<T> relatedDataList) {

    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
    this.identificationNumber = identificationNumber;
    this.description = description;
    this.shape = shape;
    this.maximumDescriptionSize = maximumDescriptionSize;

    if (relatedDataList.size() > maximumRelatedDataListSize) {
      throw new IllegalArgumentException(
          "Cannot set related data list with "
              + relatedDataList.size()
              + "! Exceeded maximum list size of "
              + maximumRelatedDataListSize
              + "!");
    }

    this.relatedDataList = relatedDataList;
  }

  public SpatialData(
      int identificationNumber,
      int maximumDescriptionSize,
      String description,
      Rectangle shape,
      int maximumRelatedDataListSize) {
    this.identificationNumber = identificationNumber;
    this.description = new LimitedString(maximumDescriptionSize, description);
    this.shape = shape;
    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
    this.maximumDescriptionSize = maximumDescriptionSize;

    this.relatedDataList = new ArrayList<>();
  }

  public SpatialData(
      int identificationNumber,
      int maximumDescriptionSize,
      LimitedString description,
      Rectangle shape,
      int maximumRelatedDataListSize) {
    this.identificationNumber = identificationNumber;
    this.description = description;
    this.shape = shape;
    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
    this.maximumDescriptionSize = maximumDescriptionSize;

    this.relatedDataList = new ArrayList<>();
  }

  public SpatialData(
      int identificationNumber,
      int maximumDescriptionSize,
      String description,
      int maximumRelatedDataListSize) {
    this.identificationNumber = identificationNumber;
    this.description = new LimitedString(maximumDescriptionSize, description);
    this.maximumDescriptionSize = maximumDescriptionSize;

    this.relatedDataList = new ArrayList<>();
    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
  }

  public SpatialData(
      int identificationNumber,
      int maximumDescriptionSize,
      LimitedString description,
      int maximumRelatedDataListSize) {
    this.identificationNumber = identificationNumber;
    this.description = description;
    this.maximumDescriptionSize = maximumDescriptionSize;

    this.relatedDataList = new ArrayList<>();
    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
  }

  /**
   * Default constructor used to create dummy instance for loading from byteArray
   */
  public SpatialData() {}

  public int getMaximumRelatedDataListSize() {
    return maximumRelatedDataListSize;
  }

  public void setMaximumRelatedDataListSize(int maximumRelatedDataListSize) {
    this.maximumRelatedDataListSize = maximumRelatedDataListSize;
  }

  public int getMaximumDescriptionSize() {
    return maximumDescriptionSize;
  }

  public void setMaximumDescriptionSize(int maximumDescriptionSize) {
    this.maximumDescriptionSize = maximumDescriptionSize;
  }

  public int getIdentificationNumber() {
    return identificationNumber;
  }

  public void setIdentificationNumber(int identificationNumber) {
    this.identificationNumber = identificationNumber;
  }

  public LimitedString getDescription() {
    return description;
  }

  public void setDescription(LimitedString description) {
    this.description = description;
  }

  public List<T> getRelatedDataList() {
    return relatedDataList;
  }

  public void setRelatedDataList(List<T> relatedDataList) {
    this.relatedDataList = relatedDataList;
  }

  public void addRelatedData(T data) {
    if (relatedDataList.size() >= maximumRelatedDataListSize) {
      throw new IllegalStateException("Cannot add more relatedData to SpatialData: " + this);
    }

    relatedDataList.add(data);
  }

  public void removeRelatedData(T data) {
    relatedDataList.remove(data);
  }

  public void setShape(Rectangle shape) {
    this.shape = shape;
  }

  @Override
  public Rectangle getShapeOfData() {
    return shape;
  }

  public int getMaxRelatedDataListSize() {
    return maximumRelatedDataListSize;
  }

  public String toString(String className) {
    StringBuilder sb = new StringBuilder();
    relatedDataList.forEach(
        data -> {
          sb.append("identificationNumber=")
              .append(data.getIdentificationNumber())
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
    if (!(obj instanceof SpatialData)) {
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
  public abstract byte[] toByteArray();

  @Override
  public abstract void fromByteArray(byte[] byteArray);
}

package entity;

import entity.shape.Rectangle;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import structure.dynamichashfile.entity.LimitedString;
import structure.dynamichashfile.entity.record.Record;
import structure.quadtree.IShapeData;

public abstract class SpatialData<T extends SpatialData<?>> extends Record implements IShapeData {
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

  /** Default constructor used to create dummy instance for loading from byteArray */
  public SpatialData() {}

  public SpatialData(int identificationNumber) {
    this.identificationNumber = identificationNumber;
  }

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

  public void setDescription(String description) {
    this.description = new LimitedString(maximumDescriptionSize, description);
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

  @Override
  public BitSet hash() {
    BitSet bitSet = new BitSet(12);
    char[] hash =
        Integer.toBinaryString(identificationNumber % 4096)
            .toCharArray(); // TODO neskor poskusat na lepsej hashovacke
    for (int i = 0; i < hash.length; i++) {
      if (hash[i] == '1') {
        bitSet.set(i);
      }
    }
    return bitSet;
  }

  @Override
  public int getMaxHashSize() {
    return 3;
  }

  public String toString(String className) {
    if (relatedDataList == null) {
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
          + ", relatedDataList=[]"
          + "\n hash: "
          + hash().toString();
    }

    StringBuilder sb = new StringBuilder();
    relatedDataList.forEach(
        data -> {
          sb.append("identificationNumber=")
              .append(data.getIdentificationNumber())
              .append(" ")
              .append("Description: '")
              .append(data.getDescription())
              .append("'")
              .append("Shape: ")
              .append(data.getShapeOfData() != null ? getShapeOfData() : "-");
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
        + ", relatedDataList=[\n"
        + sb
        + "]\n"
        + "hash: "
        + hash().toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SpatialData)) {
      return false;
    }
    SpatialData<?> castedObj = (SpatialData<?>) obj;

    //    return castedObj.getDescription().equals(description)
    //        && castedObj.identificationNumber == identificationNumber
    //        && ((castedObj.shape == null && shape == null)
    //            || (castedObj.shape != null && castedObj.shape.equals(shape)));
    return identificationNumber == castedObj.getIdentificationNumber();
    //        && ((castedObj.shape == null && shape == null)
    //            || (castedObj.shape != null && castedObj.shape.equals(shape)));
  }
}

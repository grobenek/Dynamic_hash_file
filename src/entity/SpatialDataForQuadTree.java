package entity;

import entity.shape.Rectangle;
import structure.quadtree.IShapeData;

public abstract class SpatialDataForQuadTree implements IShapeData {
  private final int identificationNumber;
  private final Rectangle shape;

  public SpatialDataForQuadTree(int identificationNumber, Rectangle shape) {
    this.identificationNumber = identificationNumber;
    this.shape = shape;
  }

  public int getIdentificationNumber() {
    return identificationNumber;
  }

  @Override
  public Rectangle getShapeOfData() {
    return shape;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SpatialDataForQuadTree)) {
      return false;
    }

    return shape.equals(((SpatialDataForQuadTree) obj).shape)
        && identificationNumber == ((SpatialDataForQuadTree) obj).identificationNumber;
  }

  @Override
  public String toString() {
    return toString(getClass().getSimpleName());
  }

  public String toString(String className) {
    return className
        + "{"
        + "identificationNumber="
        + identificationNumber
        + ", shape="
        + shape
        + '}';
  }
}

package entity;

import entity.shape.Rectangle;
import structure.quadtree.IShapeData;

public class ParcelForQuadTree implements IShapeData {
  private final int identificationNumber;
  private final Rectangle shape;

  public ParcelForQuadTree(int identificationNumber, Rectangle shape) {
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
    if (!(obj instanceof ParcelForQuadTree)) {
      return false;
    }

    return shape.equals(((ParcelForQuadTree) obj).shape)
        && identificationNumber == ((ParcelForQuadTree) obj).identificationNumber;
  }

  @Override
  public String toString() {
    return "ParcelForQuadTree{"
        + "identificationNumber="
        + identificationNumber
        + ", shape="
        + shape
        + '}';
  }
}

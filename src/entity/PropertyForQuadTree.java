package entity;

import entity.shape.Rectangle;
import structure.quadtree.IShapeData;

public class PropertyForQuadTree implements IShapeData {
  private final int identificationNumber;
  private final Rectangle shape;

  public PropertyForQuadTree(int identificationNumber, Rectangle shape) {
    this.identificationNumber = identificationNumber;
    this.shape = shape;
  }

  public int getIdentificationNumber() {
    return identificationNumber;
  }

  @Override
  public String toString() {
    return "PropertyForQuadTree{"
        + "identificationNumber="
        + identificationNumber
        + ", shape="
        + shape
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PropertyForQuadTree)) {
      return false;
    }

    return shape.equals(((PropertyForQuadTree) obj).shape)
        && identificationNumber == ((PropertyForQuadTree) obj).identificationNumber;
  }

  @Override
  public Rectangle getShapeOfData() {
    return shape;
  }
}

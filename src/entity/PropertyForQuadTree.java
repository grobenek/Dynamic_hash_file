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
  public Rectangle getShapeOfData() {
    return shape;
  }
}

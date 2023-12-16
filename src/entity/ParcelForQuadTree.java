package entity;

import entity.shape.Rectangle;

public class ParcelForQuadTree extends SpatialDataForQuadTree {
  public ParcelForQuadTree(int identificationNumber, Rectangle shape) {
    super(identificationNumber, shape);
  }
}

package entity;

import entity.shape.Rectangle;
import structure.quadtree.IShapeData;

public class Property extends SpatialData<Parcel> implements IShapeData {

  public Property(int identificationNumber, String description, Rectangle shape) {
    super(identificationNumber, description, shape);
  }

  public Property(int identificationNumber, String description) {
    super(identificationNumber, description);
  }

  @Override
  public String toString() {
    return super.toString("Property");
  }
}

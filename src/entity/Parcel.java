package entity;

import entity.shape.Rectangle;
import structure.quadtree.IShapeData;

public class Parcel extends SpatialData<Property> implements IShapeData {

  public Parcel(int identificationNumber, String description, Rectangle shape) {
    super(identificationNumber, description, shape);
  }

  public Parcel(int identificationNumber, String description) {
    super(identificationNumber, description);
  }

  @Override
  public String toString() {
    return super.toString("Parcel");
  }
}

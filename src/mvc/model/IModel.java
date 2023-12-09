package mvc.model;

import entity.Parcel;
import entity.Property;
import entity.shape.Rectangle;

public interface IModel extends AutoCloseable {
  Property findProperty(int propertyIdentificationNumber);

  Parcel findParcel(int parcelIdentificationNumber);

  void insertProperty(int registrationNumber, String description, Rectangle shape);

  void insertParcel(String description, Rectangle shape);

  void removeProperty(int propertyIdentificationNumber);

  void removeParcel(int parcelIdentificationNumber);

  void initializePropertyQuadTree(int height, Rectangle shape);
  void initializeParcelQuadTree(int height, Rectangle shape);
  void initializePropertyDynamicHashFile(String pathToMainFile, String pathToOverflowFile, int mainFileBlockingFactor, int overflowFileBlockingFactor, int maxHeight);
  void initializeParcelDynamicHashFile(String pathToMainFile, String pathToOverflowFile, int mainFileBlockingFactor, int overflowFileBlockingFactor, int maxHeight);
}

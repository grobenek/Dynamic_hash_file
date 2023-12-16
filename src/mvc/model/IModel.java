package mvc.model;

import entity.Parcel;
import entity.Property;
import entity.shape.Rectangle;
import mvc.view.observable.IStructuresWrapperObservable;

public interface IModel extends AutoCloseable, IStructuresWrapperObservable {
  Property findProperty(int propertyIdentificationNumber);

  Parcel findParcel(int parcelIdentificationNumber);

  void insertProperty(int registrationNumber, String description, Rectangle shape);

  void insertParcel(String description, Rectangle shape);

  void removeProperty(int propertyIdentificationNumber);

  void removeParcel(int parcelIdentificationNumber);

  void editProperty(Property propertyToEdit, Property editedProperty);

  void editParcel(Parcel parcelToEdit, Parcel editedParcel);

  void initializePropertyQuadTree(int height, Rectangle shape);

  void initializeParcelQuadTree(int height, Rectangle shape);

  void initializePropertyDynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int mainFileBlockingFactor,
      int overflowFileBlockingFactor);

  void initializeParcelDynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int mainFileBlockingFactor,
      int overflowFileBlockingFactor);

  void generateData(int numberOfProperties, int numberOfParcels);

  String getPropertyOverflowSequenceString();

  String getParcelOverflowSequenceString();
}

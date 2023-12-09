package mvc.view;

import entity.Parcel;
import entity.Property;
import entity.shape.Rectangle;

import javax.swing.*;

public interface IMainWindow {
  Property findProperty(int propertyIdentificationNumber);

  Parcel findParcel(int parcelIdentificationNumber);

  void insertProperty(int registrationNumber, String description, Rectangle shape);

  void insertParcel(String description, Rectangle shape);

  void removeProperty(Property property);

  void removeParcel(Parcel parcel);

  void editProperty(Property property);

  void editParcel(Parcel parcel);

  void generateData(int numberOfProperties, int numberOfParcels);

  void showPopupMessage(String message);

  JFrame getJFrameObject();
}

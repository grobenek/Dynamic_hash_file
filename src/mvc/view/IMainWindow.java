package mvc.view;

import entity.Parcel;
import entity.Property;
import javax.swing.*;
import mvc.view.observable.IObserver;
import structure.dynamichashfile.DynamicHashFile;

public interface IMainWindow extends IObserver {
  void findProperty(int propertyIdentificationNumber);

  void findParcel(int parcelIdentificationNumber);

  void removeProperty(int propertyIdentificationNumber);

  void removeParcel(int parcelIdentificationNumber);

  void editProperty(int identificationNumber);

  void editParcel(int identificationNumber);

  void generateData(int numberOfProperties, int numberOfParcels);

  void initializeBothDynamicHashFiles();

  void setParcelDynamicHashFileInfo(DynamicHashFile<Parcel> dynamicHashFile);

  void setPropertyDynamicHashFileInfo(DynamicHashFile<Property> dynamicHashFile);

  void showPopupMessage(String message);

  JFrame getJFrameObject();
}

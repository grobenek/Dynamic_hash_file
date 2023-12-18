package mvc.controller;

import entity.Parcel;
import entity.Property;
import entity.shape.Rectangle;
import java.io.IOException;
import mvc.model.IModel;
import mvc.view.IMainWindow;
import mvc.view.observable.IObservable;
import mvc.view.observable.IStructuresWrapperObservable;
import structure.dynamichashfile.DynamicHashFile;

public class Controller implements IController {
  private final IModel model;
  private IMainWindow view;

  public Controller(IModel model) {
    this.model = model;
    model.attach(this);
  }

  @Override
  public Property findProperty(int propertyIdentificationNumber) {
    try {
      return model.findProperty(propertyIdentificationNumber);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
      return null;
    }
  }

  @Override
  public Parcel findParcel(int parcelIdentificationNumber) {
    try {
      return model.findParcel(parcelIdentificationNumber);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
      return null;
    }
  }

  @Override
  public void insertProperty(int registrationNumber, String description, Rectangle shape) {
    try {
      model.insertProperty(registrationNumber, description, shape);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void insertParcel(String description, Rectangle shape) {
    try {
      model.insertParcel(description, shape);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void removeProperty(int propertyIdentificationNumber) {
    try {
      model.removeProperty(propertyIdentificationNumber);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void removeParcel(int parcelIdentificationNumber) {
    try {
      model.removeParcel(parcelIdentificationNumber);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void editProperty(Property propertyToEdit, Property editedProperty) {
    try {
      model.editProperty(propertyToEdit, editedProperty);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void editParcel(Parcel parcelToEdit, Parcel editedParcel) {
    try {
      model.editParcel(parcelToEdit, editedParcel);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void initializePropertyQuadTree(int height, Rectangle shape) {
    try {
      model.initializePropertyQuadTree(height, shape);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void initializeParcelQuadTree(int height, Rectangle shape) {
    try {
      model.initializeParcelQuadTree(height, shape);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void initializePropertyDynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int mainFileBlockingFactor,
      int overflowFileBlockingFactor) {
    try {
      model.initializePropertyDynamicHashFile(
          pathToMainFile, pathToOverflowFile, mainFileBlockingFactor, overflowFileBlockingFactor);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void initializeParcelDynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int mainFileBlockingFactor,
      int overflowFileBlockingFactor) {
    try {
      model.initializeParcelDynamicHashFile(
          pathToMainFile, pathToOverflowFile, mainFileBlockingFactor, overflowFileBlockingFactor);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  public void setView(IMainWindow view) {
    this.view = view;
    this.view.initializeBothDynamicHashFiles();
  }

  @Override
  public void generateData(int numberOfProperties, int numberOfParcels) {
    try {
      model.generateData(numberOfProperties, numberOfParcels);
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public String getPropertyOverflowSequenceString() {
    try {
      return model.getPropertyOverflowSequenceString();
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
      return "";
    }
  }

  @Override
  public String getParcelOverflowSequenceString() {
    try {
      return model.getParcelOverflowSequenceString();
    } catch (Exception e) {
      view.showPopupMessage(e.getLocalizedMessage());
      return "";
    }
  }

  @Override
  public void saveToFile() {
    try {
      model.saveToFile();
    } catch (IOException e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void loadFromFile() {
    try {
      model.loadFromFile();
    } catch (IOException e) {
      view.showPopupMessage(e.getLocalizedMessage());
    }
  }

  @Override
  public void update(IObservable observable) {
    if (!(observable instanceof IStructuresWrapperObservable)) {
      return;
    }

    DynamicHashFile<?>[] files = ((IStructuresWrapperObservable) observable).getHashFiles();

    if (files.length == 0) {
      return;
    }

    for (int i = 0; i < files.length; i++) {
      if (i == 0 && files[i] != null) {
        view.setParcelDynamicHashFileInfo((DynamicHashFile<Parcel>) files[i]);
      }

      if (i == 1 && files[i] != null) {
        view.setPropertyDynamicHashFileInfo((DynamicHashFile<Property>) files[i]);
      }
    }
  }
}

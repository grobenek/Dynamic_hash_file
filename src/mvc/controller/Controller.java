package mvc.controller;

import entity.Parcel;
import entity.Property;
import entity.shape.Rectangle;
import mvc.model.IModel;
import mvc.view.IMainWindow;

public class Controller implements IController {
  private final IModel model;
  private IMainWindow view;

  public Controller(IModel model) {
    this.model = model;
  }

  @Override
  public Property findProperty(int propertyIdentificationNumber) {
    return model.findProperty(propertyIdentificationNumber);
  }

  @Override
  public Parcel findParcel(int parcelIdentificationNumber) {
    return model.findParcel(parcelIdentificationNumber);
  }

  @Override
  public void insertProperty(int registrationNumber, String description, Rectangle shape) {
    model.insertProperty(registrationNumber, description, shape);
  }

  @Override
  public void insertParcel(String description, Rectangle shape) {
    model.insertParcel(description, shape);
  }

  @Override
  public void removeProperty(int propertyIdentificationNumber) {
    model.removeProperty(propertyIdentificationNumber);
  }

  @Override
  public void removeParcel(int parcelIdentificationNumber) {
    model.removeParcel(parcelIdentificationNumber);
  }

  @Override
  public void initializePropertyQuadTree(int height, Rectangle shape) {
    model.initializePropertyQuadTree(height, shape);
  }

  @Override
  public void initializeParcelQuadTree(int height, Rectangle shape) {
    model.initializeParcelQuadTree(height, shape);
  }

  @Override
  public void initializePropertyDynamicHashFile(String pathToMainFile, String pathToOverflowFile, int mainFileBlockingFactor, int overflowFileBlockingFactor, int maxHeight) {
    model.initializePropertyDynamicHashFile(pathToMainFile, pathToOverflowFile, mainFileBlockingFactor, overflowFileBlockingFactor, maxHeight);
  }

  @Override
  public void initializeParcelDynamicHashFile(String pathToMainFile, String pathToOverflowFile, int mainFileBlockingFactor, int overflowFileBlockingFactor, int maxHeight) {
    model.initializeParcelDynamicHashFile(pathToMainFile, pathToOverflowFile, mainFileBlockingFactor, overflowFileBlockingFactor, maxHeight);
  }

  public void setView(IMainWindow view) {
    this.view = view;
//    view.initializeBothQuadTrees();
  }
}

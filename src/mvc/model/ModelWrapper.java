package mvc.model;

import entity.Parcel;
import entity.ParcelForQuadTree;
import entity.Property;
import entity.PropertyForQuadTree;
import entity.shape.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import structure.dynamichashfile.DynamicHashFile;
import structure.quadtree.QuadTree;

public class ModelWrapper implements IModel, AutoCloseable {
  private final Random random = new Random();
  private QuadTree<PropertyForQuadTree> propertyQuadTree;
  private QuadTree<ParcelForQuadTree> parcelQuadTree;
  private DynamicHashFile<Property> propertyDynamicHashFile;
  private DynamicHashFile<Parcel> parcelDynamicHashFile;
  private int propertyIdentificationNumberSequence;
  private int parcelIdentificationNumberSequence;

  public ModelWrapper(
      QuadTree<PropertyForQuadTree> propertyQuadTree,
      QuadTree<ParcelForQuadTree> parcelQuadTree,
      DynamicHashFile<Property> propertyDynamicHashFile,
      DynamicHashFile<Parcel> parcelDynamicHashFile,
      int propertyIdentificationNumberSequence,
      int parcelIdentificationNumberSequence) {
    this.propertyQuadTree = propertyQuadTree;
    this.parcelQuadTree = parcelQuadTree;
    this.propertyDynamicHashFile = propertyDynamicHashFile;
    this.parcelDynamicHashFile = parcelDynamicHashFile;
    this.propertyIdentificationNumberSequence = propertyIdentificationNumberSequence;
    this.parcelIdentificationNumberSequence = parcelIdentificationNumberSequence;
  }

  public ModelWrapper() {}

  @Override
  public Property findProperty(int propertyIdentificationNumber) {
    Property foundedProperty =
        propertyDynamicHashFile.find(new Property(propertyIdentificationNumber));

    List<Parcel> propertyRelatedData = foundedProperty.getRelatedDataList();

    List<Parcel> parcelsOfProperty = new ArrayList<>(propertyRelatedData.size());

    for (Parcel parcelWithOnlyIdentificationNumber : propertyRelatedData) {
      parcelsOfProperty.add(parcelDynamicHashFile.find(parcelWithOnlyIdentificationNumber));
    }

    foundedProperty.setRelatedDataList(parcelsOfProperty);

    return foundedProperty;
  }

  @Override
  public Parcel findParcel(int parcelIdentificationNumber) {
    Parcel foundedParcel = parcelDynamicHashFile.find(new Parcel(parcelIdentificationNumber));

    List<Property> parcelRelatedDataList = foundedParcel.getRelatedDataList();

    List<Property> propertiesOfParcel = new ArrayList<>(parcelRelatedDataList.size());

    for (Property propertyWithOnlyIdentificationNumber : parcelRelatedDataList) {
      propertiesOfParcel.add(propertyDynamicHashFile.find(propertyWithOnlyIdentificationNumber));
    }

    foundedParcel.setRelatedDataList(propertiesOfParcel);

    return foundedParcel;
  }

  @Override
  public void insertProperty(int registrationNumber, String description, Rectangle shape) {
    int newPropertyIdentificationNumber = getNewPropertyIdentificationNumber();
    PropertyForQuadTree propertyForQuadTree =
        new PropertyForQuadTree(newPropertyIdentificationNumber, shape);

    List<ParcelForQuadTree> parcelsOfProperty = parcelQuadTree.search(shape);

    if (parcelsOfProperty.size() > Property.getMaxParcelListSize()) {
      throw new IllegalStateException(
          String.format(
              "Cannot insert Property with identification number %d, number of related parcels %d exceeds limit for Property!",
              newPropertyIdentificationNumber, parcelsOfProperty.size()));
    }

    List<Parcel> propertyRelatedDataList = new ArrayList<>(parcelsOfProperty.size());
    for (ParcelForQuadTree parcelForQuadTree : parcelsOfProperty) {
      if (propertyQuadTree.search(parcelForQuadTree.getShapeOfData()).size() + 1
          > Parcel.getMaxPropertyListSize()) {
        throw new IllegalStateException(
            String.format(
                "Cannot insert Property with identification number %d, property list limit is exceeded for parcel %s",
                newPropertyIdentificationNumber, parcelForQuadTree));
      }

      Parcel parcelOfProperty = new Parcel(parcelForQuadTree.getIdentificationNumber());

      propertyRelatedDataList.add(parcelOfProperty);

      Parcel parcelInHashFile = parcelDynamicHashFile.find(parcelOfProperty);
      parcelInHashFile.addRelatedData(new Property(newPropertyIdentificationNumber));

      parcelDynamicHashFile.delete(parcelInHashFile);
      parcelDynamicHashFile.insert(parcelInHashFile);
    }

    propertyQuadTree.insert(propertyForQuadTree);

    Property propertyForDynamicHashFile =
        new Property(
            newPropertyIdentificationNumber,
            registrationNumber,
            description,
            shape,
            propertyRelatedDataList);

    propertyDynamicHashFile.insert(propertyForDynamicHashFile);
  }

  @Override
  public void insertParcel(String description, Rectangle shape) {
    int newParcelIdentificationNumber = getNewParcelIdentificationNumber();
    ParcelForQuadTree parcelForQuadTree =
        new ParcelForQuadTree(newParcelIdentificationNumber, shape);

    List<PropertyForQuadTree> propertiesOfParcel = propertyQuadTree.search(shape);

    if (propertiesOfParcel.size() > Parcel.getMaxPropertyListSize()) {
      throw new IllegalStateException(
          String.format(
              "Cannot insert Parcel with identification number %d, number of related parcels %d exceeds limit for Property!",
              newParcelIdentificationNumber, propertiesOfParcel.size()));
    }

    List<Property> parcelRelatedDataList = new ArrayList<>(propertiesOfParcel.size());
    for (PropertyForQuadTree pPropertyOfParcel : propertiesOfParcel) {
      if (propertyQuadTree.search(pPropertyOfParcel.getShapeOfData()).size() + 1
          > Property.getMaxParcelListSize()) {
        throw new IllegalStateException(
            String.format(
                "Cannot insert Parcel with identification number %d, parcel list limit is exceeded for property %s",
                newParcelIdentificationNumber, pPropertyOfParcel));
      }

      Property propertyOfParcel = new Property(pPropertyOfParcel.getIdentificationNumber());

      parcelRelatedDataList.add(propertyOfParcel);

      Property propertyInHashFile = propertyDynamicHashFile.find(propertyOfParcel);
      propertyInHashFile.addRelatedData(new Parcel(newParcelIdentificationNumber));

      propertyDynamicHashFile.delete(propertyInHashFile);
      propertyDynamicHashFile.insert(propertyInHashFile);
    }

    parcelQuadTree.insert(parcelForQuadTree);

    Parcel parcelForDynamicHashFile =
        new Parcel(newParcelIdentificationNumber, description, shape, parcelRelatedDataList);

    parcelDynamicHashFile.insert(parcelForDynamicHashFile);
  }

  @Override
  public void removeProperty(int propertyIdentificationNumber) {
    Property propertyToDelete =
        propertyDynamicHashFile.find(new Property(propertyIdentificationNumber));

    propertyDynamicHashFile.delete(propertyToDelete);
    propertyQuadTree.deleteData(
        new PropertyForQuadTree(propertyIdentificationNumber, propertyToDelete.getShapeOfData()));
  }

  @Override
  public void removeParcel(int parcelIdentificationNumber) {
    Parcel parcelToDelete = parcelDynamicHashFile.find(new Parcel(parcelIdentificationNumber));

    parcelDynamicHashFile.delete(parcelToDelete);
    parcelQuadTree.deleteData(
        new ParcelForQuadTree(parcelIdentificationNumber, parcelToDelete.getShapeOfData()));
  }

  @Override
  public void initializePropertyQuadTree(int height, Rectangle shape) {
    propertyQuadTree = new QuadTree<>(height, shape);
  }

  @Override
  public void initializeParcelQuadTree(int height, Rectangle shape) {
    parcelQuadTree = new QuadTree<>(height, shape);
  }

  @Override
  public void initializePropertyDynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int mainFileBlockingFactor,
      int overflowFileBlockingFactor,
      int maxHeight) {
    if (propertyDynamicHashFile != null) {
      try {
        propertyDynamicHashFile.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    try {
      propertyDynamicHashFile =
          new DynamicHashFile<>(
              pathToMainFile,
              pathToOverflowFile,
              mainFileBlockingFactor,
              overflowFileBlockingFactor,
              maxHeight,
              Property.class);
    } catch (IOException e) {
      throw new RuntimeException(
          "Cannot initialize propertyDynamicHashFile! Error: " + e.getLocalizedMessage());
    }
  }

  @Override
  public void initializeParcelDynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int mainFileBlockingFactor,
      int overflowFileBlockingFactor,
      int maxHeight) {
    if (parcelDynamicHashFile != null) {
      try {
        parcelDynamicHashFile.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    try {
      parcelDynamicHashFile =
          new DynamicHashFile<>(
              pathToMainFile,
              pathToOverflowFile,
              mainFileBlockingFactor,
              overflowFileBlockingFactor,
              maxHeight,
              Parcel.class);
    } catch (IOException e) {
      throw new RuntimeException(
          "Cannot initialize parcelDynamicHashFile! Error: " + e.getLocalizedMessage());
    }
  }

  private int getNewParcelIdentificationNumber() {
    parcelIdentificationNumberSequence++;
    return parcelIdentificationNumberSequence;
  }

  private int getNewPropertyIdentificationNumber() {
    propertyIdentificationNumberSequence++;
    return propertyIdentificationNumberSequence;
  }

  @Override
  public void close() throws Exception {
    propertyDynamicHashFile.close();
    parcelDynamicHashFile.close();
  }
}

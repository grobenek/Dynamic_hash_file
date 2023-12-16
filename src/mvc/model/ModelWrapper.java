package mvc.model;

import entity.*;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mvc.view.observable.IObserver;
import structure.dynamichashfile.DynamicHashFile;
import structure.dynamichashfile.trie.InnerTrieNode;
import structure.dynamichashfile.trie.TrieNode;
import structure.quadtree.QuadTree;
import util.file.IFileBuilder;
import util.file.IOManager;
import util.file.dynamichashfile.DynamicHashFileInfo;
import util.file.dynamichashfile.DynamicHashFileInfoBuilder;
import util.file.dynamichashfile.TextBuilderTrie;
import util.file.quadtree.CsvBuilderQuadTreeData;

public class ModelWrapper implements IModel {
  private final Random random = new Random();
  private List<IObserver> observers;
  private QuadTree<SpatialDataForQuadTree> propertyQuadTree;
  private QuadTree<SpatialDataForQuadTree> parcelQuadTree;
  private DynamicHashFile<Property> propertyDynamicHashFile;
  private DynamicHashFile<Parcel> parcelDynamicHashFile;
  private int propertyIdentificationNumberSequence;
  private int parcelIdentificationNumberSequence;

  public ModelWrapper(
      QuadTree<SpatialDataForQuadTree> propertyQuadTree,
      QuadTree<SpatialDataForQuadTree> parcelQuadTree,
      DynamicHashFile<Property> propertyDynamicHashFile,
      DynamicHashFile<Parcel> parcelDynamicHashFile,
      int propertyIdentificationNumberSequence,
      int parcelIdentificationNumberSequence) {
    this.observers = new ArrayList<>();

    this.propertyQuadTree = propertyQuadTree;
    this.parcelQuadTree = parcelQuadTree;
    this.propertyDynamicHashFile = propertyDynamicHashFile;
    this.parcelDynamicHashFile = parcelDynamicHashFile;
    this.propertyIdentificationNumberSequence = propertyIdentificationNumberSequence;
    this.parcelIdentificationNumberSequence = parcelIdentificationNumberSequence;
  }

  public ModelWrapper() {
    this.observers = new ArrayList<>();
  }

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
  public void insertProperty(int registrationNumber, String description, Rectangle shape)
      throws IOException {
    int newPropertyIdentificationNumber = getNewPropertyIdentificationNumber();
    PropertyForQuadTree propertyForQuadTree =
        new PropertyForQuadTree(newPropertyIdentificationNumber, shape);

    List<SpatialDataForQuadTree> parcelsOfProperty = parcelQuadTree.search(shape);

    if (parcelsOfProperty.size() > Property.getMaxParcelListSize()) {
      throw new IllegalStateException(
          String.format(
              "Cannot insert Property with identification number %d, number of related parcels %d exceeds limit for Property!",
              newPropertyIdentificationNumber, parcelsOfProperty.size()));
    }

    List<Parcel> propertyRelatedDataList = new ArrayList<>(parcelsOfProperty.size());
    for (SpatialDataForQuadTree parcelForQuadTree : parcelsOfProperty) {
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

    sendNotifications();
  }

  @Override
  public void insertParcel(String description, Rectangle shape) throws IOException {
    int newParcelIdentificationNumber = getNewParcelIdentificationNumber();
    ParcelForQuadTree parcelForQuadTree =
        new ParcelForQuadTree(newParcelIdentificationNumber, shape);

    List<SpatialDataForQuadTree> propertiesOfParcel = propertyQuadTree.search(shape);

    if (propertiesOfParcel.size() > Parcel.getMaxPropertyListSize()) {
      throw new IllegalStateException(
          String.format(
              "Cannot insert Parcel with identification number %d, number of related parcels %d exceeds limit for Property!",
              newParcelIdentificationNumber, propertiesOfParcel.size()));
    }

    List<Property> parcelRelatedDataList = new ArrayList<>(propertiesOfParcel.size());
    for (SpatialDataForQuadTree pPropertyOfParcel : propertiesOfParcel) {
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

    sendNotifications();
  }

  @Override
  public void removeProperty(int propertyIdentificationNumber) throws IOException {
    Property propertyToDelete =
        propertyDynamicHashFile.find(new Property(propertyIdentificationNumber));

    for (Parcel parcel : propertyToDelete.getRelatedDataList()) {
      Parcel foundedParcel = parcelDynamicHashFile.find(parcel);
      foundedParcel.removeRelatedData(propertyToDelete);
      parcelDynamicHashFile.edit(foundedParcel, foundedParcel);
    }

    propertyDynamicHashFile.delete(propertyToDelete);
    propertyQuadTree.deleteData(
        new PropertyForQuadTree(propertyIdentificationNumber, propertyToDelete.getShapeOfData()));

    sendNotifications();
  }

  @Override
  public void removeParcel(int parcelIdentificationNumber) throws IOException {
    Parcel parcelToDelete = parcelDynamicHashFile.find(new Parcel(parcelIdentificationNumber));

    for (Property property : parcelToDelete.getRelatedDataList()) {
      Property foundedProperty = propertyDynamicHashFile.find(property);
      foundedProperty.removeRelatedData(parcelToDelete);
      propertyDynamicHashFile.edit(foundedProperty, foundedProperty);
    }

    parcelDynamicHashFile.delete(parcelToDelete);
    parcelQuadTree.deleteData(
        new ParcelForQuadTree(parcelIdentificationNumber, parcelToDelete.getShapeOfData()));

    sendNotifications();
  }

  @Override
  public void editProperty(Property propertyToEdit, Property editedProperty) {
    if (!propertyToEdit.getShapeOfData().equals(editedProperty.getShapeOfData())) {
      // need to check if new coordinates can be added

      List<SpatialDataForQuadTree> parcelsOfEditedProperty =
          parcelQuadTree.search(editedProperty.getShapeOfData());

      if (parcelsOfEditedProperty.size() > Property.getMaxParcelListSize()) {
        throw new IllegalStateException(
            String.format(
                "Cannot edit Property with identification number %d, number of related parcels %d exceeds limit for Property!",
                editedProperty.getIdentificationNumber(), parcelsOfEditedProperty.size()));
      }

      for (SpatialDataForQuadTree parcelForQuadTree : parcelsOfEditedProperty) {
        if (propertyQuadTree.search(parcelForQuadTree.getShapeOfData()).size() + 1
            > Parcel.getMaxPropertyListSize()) {
          throw new IllegalStateException(
              String.format(
                  "Cannot edit Property with identification number %d, property list limit is exceeded for parcel %s",
                  editedProperty.getIdentificationNumber(), parcelForQuadTree));
        }
      }

      // setting new relatedDataList
      editedProperty.setRelatedDataList(
          parcelsOfEditedProperty.stream()
              .map(parcel -> new Parcel(parcel.getIdentificationNumber(), ""))
              .toList());

      // edit file and update quad tree
      propertyDynamicHashFile.edit(propertyToEdit, editedProperty);
      propertyQuadTree.deleteData(
          new PropertyForQuadTree(
              propertyToEdit.getIdentificationNumber(), propertyToEdit.getShapeOfData()));
      propertyQuadTree.insert(
          new PropertyForQuadTree(
              editedProperty.getIdentificationNumber(), editedProperty.getShapeOfData()));

      List<SpatialDataForQuadTree> parcelsOfOriginalProperty =
          parcelQuadTree.search(propertyToEdit.getShapeOfData());

      // merging lists
      parcelsOfOriginalProperty.removeAll(parcelsOfEditedProperty);
      parcelsOfEditedProperty.addAll(parcelsOfOriginalProperty);

      List<SpatialDataForQuadTree> mergedParcels = parcelsOfEditedProperty;
      for (SpatialDataForQuadTree parcelForQuadTree : mergedParcels) {
        Parcel parcelOfProperty =
            parcelDynamicHashFile.find(new Parcel(parcelForQuadTree.getIdentificationNumber()));

        parcelOfProperty.removeRelatedData(propertyToEdit);

        if (parcelQuadTree
            .search(editedProperty.getShapeOfData())
            .contains(
                new ParcelForQuadTree(
                    parcelOfProperty.getIdentificationNumber(),
                    parcelOfProperty.getShapeOfData()))) {
          parcelOfProperty.addRelatedData(editedProperty);
        }
        parcelDynamicHashFile.edit(parcelOfProperty, parcelOfProperty);
      }

    } else {
      propertyDynamicHashFile.edit(propertyToEdit, editedProperty);
      propertyQuadTree.deleteData(
          new PropertyForQuadTree(
              propertyToEdit.getIdentificationNumber(), propertyToEdit.getShapeOfData()));
      propertyQuadTree.insert(
          new PropertyForQuadTree(
              editedProperty.getIdentificationNumber(), editedProperty.getShapeOfData()));
    }

    sendNotifications();
  }

  @Override
  public void editParcel(Parcel parcelToEdit, Parcel editedParcel) {
    if (!parcelToEdit.getShapeOfData().equals(editedParcel.getShapeOfData())) {
      // need to check if new coordinates can be added

      List<SpatialDataForQuadTree> propertiesOfEditedParcel =
          propertyQuadTree.search(editedParcel.getShapeOfData());

      if (propertiesOfEditedParcel.size() > Parcel.getMaxPropertyListSize()) {
        throw new IllegalStateException(
            String.format(
                "Cannot edit Parcel with identification number %d, number of related parcels %d exceeds limit for Property!",
                editedParcel.getIdentificationNumber(), propertiesOfEditedParcel.size()));
      }

      for (SpatialDataForQuadTree propertyForQuadTree : propertiesOfEditedParcel) {
        if (propertyQuadTree.search(propertyForQuadTree.getShapeOfData()).size() + 1
            > Property.getMaxParcelListSize()) {
          throw new IllegalStateException(
              String.format(
                  "Cannot edit Parcel with identification number %d, property list limit is exceeded for parcel %s",
                  editedParcel.getIdentificationNumber(), propertyForQuadTree));
        }
      }

      editedParcel.setRelatedDataList(
          propertiesOfEditedParcel.stream()
              .map(property -> new Property(property.getIdentificationNumber(), -1, ""))
              .toList());

      parcelDynamicHashFile.edit(parcelToEdit, editedParcel);
      parcelQuadTree.deleteData(
          new ParcelForQuadTree(
              parcelToEdit.getIdentificationNumber(), parcelToEdit.getShapeOfData()));
      parcelQuadTree.insert(
          new ParcelForQuadTree(
              editedParcel.getIdentificationNumber(), editedParcel.getShapeOfData()));

      List<SpatialDataForQuadTree> propertiesOfOrignalParcel =
          propertyQuadTree.search(parcelToEdit.getShapeOfData());

      // merging lists
      propertiesOfOrignalParcel.removeAll(propertiesOfEditedParcel);
      propertiesOfEditedParcel.addAll(propertiesOfOrignalParcel);

      List<SpatialDataForQuadTree> mergedProperties = propertiesOfEditedParcel;
      for (SpatialDataForQuadTree propertyForQuadTree : mergedProperties) {
        Property propertyOfParcel =
            propertyDynamicHashFile.find(
                new Property(propertyForQuadTree.getIdentificationNumber()));

        propertyOfParcel.removeRelatedData(parcelToEdit);

        if (propertyQuadTree
            .search(editedParcel.getShapeOfData())
            .contains(
                new PropertyForQuadTree(
                    propertyOfParcel.getIdentificationNumber(),
                    propertyOfParcel.getShapeOfData()))) {
          propertyOfParcel.addRelatedData(editedParcel);
        }
        propertyDynamicHashFile.edit(propertyOfParcel, propertyOfParcel);
      }
    } else {
      parcelDynamicHashFile.edit(parcelToEdit, editedParcel);
      parcelQuadTree.deleteData(
          new ParcelForQuadTree(
              parcelToEdit.getIdentificationNumber(), parcelToEdit.getShapeOfData()));
      parcelQuadTree.insert(
          new ParcelForQuadTree(
              editedParcel.getIdentificationNumber(), editedParcel.getShapeOfData()));
    }

    sendNotifications();
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
      int overflowFileBlockingFactor) {
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
              Property.class);
      sendNotifications();
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
      int overflowFileBlockingFactor) {
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
              Parcel.class);

      sendNotifications();
    } catch (IOException e) {
      throw new RuntimeException(
          "Cannot initialize parcelDynamicHashFile! Error: " + e.getLocalizedMessage());
    }
  }

  @Override
  public void generateData(int numberOfProperties, int numberOfParcels) {
    // generating properties
    for (int i = 0; i < numberOfProperties; i++) {
      Rectangle quadTreeShape = propertyQuadTree.getShape();

      double x1 =
          random.nextDouble(
              quadTreeShape.getFirstPoint().widthCoordinate(),
              quadTreeShape.getSecondPoint().widthCoordinate());
      x1 = BigDecimal.valueOf(x1).setScale(2, RoundingMode.HALF_UP).doubleValue();

      double y1 =
          random.nextDouble(
              quadTreeShape.getFirstPoint().lengthCoordinate(),
              quadTreeShape.getSecondPoint().lengthCoordinate());
      y1 = BigDecimal.valueOf(y1).setScale(2, RoundingMode.HALF_UP).doubleValue();

      double x2 =
          random.nextDouble(
              quadTreeShape.getFirstPoint().widthCoordinate(),
              quadTreeShape.getSecondPoint().widthCoordinate());
      x2 = BigDecimal.valueOf(x2).setScale(2, RoundingMode.HALF_UP).doubleValue();

      double y2 =
          random.nextDouble(
              quadTreeShape.getFirstPoint().lengthCoordinate(),
              quadTreeShape.getSecondPoint().lengthCoordinate());
      y2 = BigDecimal.valueOf(y2).setScale(2, RoundingMode.HALF_UP).doubleValue();

      GpsCoordinates firstPointOfItem = new GpsCoordinates(Direction.S, x1, Direction.W, y1);
      GpsCoordinates secondPointOfItem = new GpsCoordinates(Direction.S, x2, Direction.W, y2);

      try {
        insertProperty(
            random.nextInt(10000),
            String.valueOf(random.nextInt(10000)),
            new Rectangle(firstPointOfItem, secondPointOfItem));
      } catch (IllegalStateException | IOException e) {
        // do not insert and continue if insert criteria are not met
      }
    }

    // generate parcels
    for (int i = 0; i < numberOfParcels; i++) {
      Rectangle quadTreeShape = parcelQuadTree.getShape();

      double x1 =
          (random.nextDouble(
              quadTreeShape.getFirstPoint().widthCoordinate(),
              quadTreeShape.getSecondPoint().widthCoordinate()));
      double y1 =
          (random.nextDouble(
              quadTreeShape.getFirstPoint().lengthCoordinate(),
              quadTreeShape.getSecondPoint().lengthCoordinate()));
      double x2 =
          (random.nextDouble(
              quadTreeShape.getFirstPoint().widthCoordinate(),
              quadTreeShape.getSecondPoint().widthCoordinate()));
      double y2 =
          (random.nextDouble(
              quadTreeShape.getFirstPoint().lengthCoordinate(),
              quadTreeShape.getSecondPoint().lengthCoordinate()));

      GpsCoordinates firstPointOfItem = new GpsCoordinates(Direction.S, x1, Direction.W, y1);
      GpsCoordinates secondPointOfItem = new GpsCoordinates(Direction.S, x2, Direction.W, y2);

      try {
        insertParcel(
            String.valueOf(random.nextInt(10000)),
            new Rectangle(firstPointOfItem, secondPointOfItem));
      } catch (IllegalStateException | IOException e) {
        // do not insert and continue if insert criteria are not met
      }
    }
  }

  @Override
  public String getPropertyOverflowSequenceString() {
    return propertyDynamicHashFile.sequenceToStringOverflowFile();
  }

  @Override
  public String getParcelOverflowSequenceString() {
    return parcelDynamicHashFile.sequenceToStringOverflowFile();
  }

  @Override
  public void saveToFile() throws IOException {
    IOManager<SpatialDataForQuadTree> quadTreeManager =
        new IOManager<>(new CsvBuilderQuadTreeData<>());

    quadTreeManager.saveToFile(
        "quadProperties.sz", propertyQuadTree.search(propertyQuadTree.getShape()));
    quadTreeManager.saveToFile("quadParecls.sz", parcelQuadTree.search(parcelQuadTree.getShape()));

    // saving Trie
    IOManager<TrieNode> trieIOManager = new IOManager<>(new TextBuilderTrie<>());
    trieIOManager.saveToFile("trieProperties.sz", propertyDynamicHashFile.getTrieNodes());
    trieIOManager.saveToFile("trieParcels.sz", parcelDynamicHashFile.getTrieNodes());

    // saving dynamic files info
    IOManager<DynamicHashFileInfo> dynamicHashFileIOManager =
        new IOManager<>(new DynamicHashFileInfoBuilder());
    dynamicHashFileIOManager.saveToFile(
        "dynamicHashFileInfoProperties.sz", List.of(propertyDynamicHashFile.getInfo()));
    dynamicHashFileIOManager.saveToFile(
        "dynamicHashFileInfoParcels.sz", List.of(parcelDynamicHashFile.getInfo()));
  }

  @Override
  public void loadFromFile() throws IOException {
    IFileBuilder<SpatialDataForQuadTree> quadTreeBuilder = new CsvBuilderQuadTreeData<>();
    IOManager<SpatialDataForQuadTree> quadTreeManager = new IOManager<>(quadTreeBuilder);

    quadTreeManager.loadFromFile("quadProperties.sz");
    List<SpatialDataForQuadTree> quadTreePropertyList = quadTreeBuilder.getLoadedData();
    quadTreeBuilder.clearLoadedData();

    quadTreeManager.loadFromFile("quadParecls.sz");
    List<SpatialDataForQuadTree> quadTreeParcelList = quadTreeBuilder.getLoadedData();
    quadTreeBuilder.clearLoadedData();

    for (SpatialDataForQuadTree property : quadTreePropertyList) {
      propertyQuadTree.insert(property);
    }

    for (SpatialDataForQuadTree parcel : quadTreeParcelList) {
      parcelQuadTree.insert(parcel);
    }

    // loading Trie
    IFileBuilder<TrieNode> trieNodeBuilder = new TextBuilderTrie<>();
    IOManager<TrieNode> trieIOManager = new IOManager<>(trieNodeBuilder);
    trieIOManager.loadFromFile("trieProperties.sz");

    List<TrieNode> propertyTrieNodes = trieNodeBuilder.getLoadedData();
    trieNodeBuilder.clearLoadedData();

    trieIOManager.loadFromFile("trieParcels.sz");

    List<TrieNode> parcelTrieNodes = trieNodeBuilder.getLoadedData();
    trieNodeBuilder.clearLoadedData();

    // loading dynamic files info
    IFileBuilder<DynamicHashFileInfo> fileInfoBuilder = new DynamicHashFileInfoBuilder();
    IOManager<DynamicHashFileInfo> dynamicHashFileIOManager = new IOManager<>(fileInfoBuilder);

    dynamicHashFileIOManager.loadFromFile("dynamicHashFileInfoProperties.sz");
    DynamicHashFileInfo propertyFileInfo = fileInfoBuilder.getLoadedData().get(0);
    fileInfoBuilder.clearLoadedData();

    dynamicHashFileIOManager.loadFromFile("dynamicHashFileInfoParcels.sz");
    DynamicHashFileInfo parcelFileInfo = fileInfoBuilder.getLoadedData().get(0);
    fileInfoBuilder.clearLoadedData();

    propertyDynamicHashFile.close();
    parcelDynamicHashFile.close();

    propertyDynamicHashFile =
        (DynamicHashFile<Property>)
            new DynamicHashFile<>(
                propertyFileInfo.pathToMainFile(),
                propertyFileInfo.pathToOverflowFile(),
                propertyFileInfo.blockingFactorOfMainFile(),
                propertyFileInfo.blockingFactorOfOverflowFile(),
                propertyFileInfo.tClass(),
                (InnerTrieNode) propertyTrieNodes.get(0));

    parcelDynamicHashFile =
        (DynamicHashFile<Parcel>)
            new DynamicHashFile<>(
                parcelFileInfo.pathToMainFile(),
                parcelFileInfo.pathToOverflowFile(),
                parcelFileInfo.blockingFactorOfMainFile(),
                parcelFileInfo.blockingFactorOfOverflowFile(),
                parcelFileInfo.tClass(),
                (InnerTrieNode) parcelTrieNodes.get(0));

    sendNotifications();
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

  @Override
  public void attach(IObserver observer) {
    observers.add(observer);
  }

  @Override
  public void detach(IObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void sendNotifications() {
    for (IObserver observer : observers) {
      observer.update(this);
    }
  }

  @Override
  public DynamicHashFile<?>[] getHashFiles() {
    if (parcelDynamicHashFile == null && propertyDynamicHashFile == null) {
      return new DynamicHashFile[0];
    } else if (parcelDynamicHashFile == null) {
      return new DynamicHashFile[] {propertyDynamicHashFile};
    } else if (propertyDynamicHashFile == null) {
      return new DynamicHashFile[] {parcelDynamicHashFile};
    } else {
      return new DynamicHashFile[] {parcelDynamicHashFile, propertyDynamicHashFile};
    }
  }
}

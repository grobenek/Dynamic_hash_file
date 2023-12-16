package mvc.view;

import entity.Parcel;
import entity.Property;
import entity.SpatialDataType;
import java.awt.*;
import javax.swing.*;
import mvc.controller.IController;
import mvc.view.entity.OperationType;
import mvc.view.entity.StructuresParameters;
import mvc.view.observable.IObservable;
import mvc.view.observable.IStructuresParametersObservable;
import structure.dynamichashfile.DynamicHashFile;

public class MainWindow extends JFrame implements IMainWindow {
  private final IController controller;
  private JButton insertNewPropertyButton;
  private JButton seachPropertiesButton;
  private JButton searchParcelsButton;
  private JTextPane resultText;
  private JTextPane parcelHashFileInfo;
  private JTextPane propertyHashFileInfo;
  private JPanel mainPanel;
  private JButton generateDataButton;
  private JButton insertNewParcelButton;
  private JButton resetFilesButton;
  private JButton editPropertyButton;
  private JButton editParcelButton;
  private JButton deletePropertyButton;
  private JButton deleteParcelButton;
  private JButton saveButton;
  private JButton loadButton;
  private JButton displayPropertyOverflowFileBlock;
  private JButton displayParcelOverflowFileBlock;

  public MainWindow(IController controller) {
    this.controller = controller;
    setContentPane(mainPanel);
    setTitle("Szathmáry_AUS2 - Semestrálna práca č. 1");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1400, 900);
    setLocationRelativeTo(null);
    setVisible(true);

    insertNewPropertyButton.addActionListener(
        e -> {
          NewDataDialog newPropertyDialog =
              new NewDataDialog(this, this.controller, SpatialDataType.PROPERTY);
        });

    insertNewParcelButton.addActionListener(
        e -> {
          NewDataDialog newParcelDialog =
              new NewDataDialog(this, this.controller, SpatialDataType.PARCEL);
        });

    seachPropertiesButton.addActionListener(
        e -> {
          GetIdentificationNumberDialog getIdentificationNumberDialog =
              new GetIdentificationNumberDialog(
                  this, SpatialDataType.PROPERTY, OperationType.SEARCH);
        });

    searchParcelsButton.addActionListener(
        e -> {
          GetIdentificationNumberDialog getIdentificationNumberDialog =
              new GetIdentificationNumberDialog(this, SpatialDataType.PARCEL, OperationType.SEARCH);
        });

    resetFilesButton.addActionListener(
        e -> {
          initializeBothDynamicHashFiles();
        });

    editPropertyButton.addActionListener(
        e -> {
          GetIdentificationNumberDialog getIdentificationNumberDialog =
              new GetIdentificationNumberDialog(this, SpatialDataType.PROPERTY, OperationType.EDIT);
        });

    editParcelButton.addActionListener(
        e -> {
          GetIdentificationNumberDialog getIdentificationNumberDialog =
              new GetIdentificationNumberDialog(this, SpatialDataType.PARCEL, OperationType.EDIT);
        });

    deletePropertyButton.addActionListener(
        e -> {
          GetIdentificationNumberDialog getIdentificationNumberDialog =
              new GetIdentificationNumberDialog(
                  this, SpatialDataType.PROPERTY, OperationType.DELETE);
        });

    deleteParcelButton.addActionListener(
        e -> {
          GetIdentificationNumberDialog getIdentificationNumberDialog =
              new GetIdentificationNumberDialog(this, SpatialDataType.PARCEL, OperationType.DELETE);
        });

    generateDataButton.addActionListener(
        e -> {
          GenerateDataDialog generateDataDialog = new GenerateDataDialog(this);
        });

    saveButton.addActionListener(
        e -> {
          controller.saveToFile();
        });

    loadButton.addActionListener(
        e -> {
          controller.loadFromFile();
        });
    displayPropertyOverflowFileBlock.addActionListener(
        e -> {
          resultText.setText(controller.getPropertyOverflowSequenceString());
        });

    displayParcelOverflowFileBlock.addActionListener(
        e -> {
          resultText.setText(controller.getParcelOverflowSequenceString());
        });
  }

  @Override
  public void findProperty(int propertyIdentificationNumber) {
    System.out.println("Searching");
    Property foundedProperty = controller.findProperty(propertyIdentificationNumber);
    if (foundedProperty != null) {
      resultText.setText(foundedProperty.toString());
    }
    System.out.println("Done");
  }

  @Override
  public void findParcel(int parcelIdentificationNumber) {
    System.out.println("Searching");
    Parcel foundedParcel = controller.findParcel(parcelIdentificationNumber);
    if (foundedParcel != null) {
      resultText.setText(foundedParcel.toString());
    }
    System.out.println("Done");
  }

  @Override
  public void removeProperty(int propertyIdentificationNumber) {
    controller.removeProperty(propertyIdentificationNumber);
  }

  @Override
  public void removeParcel(int parcelIdentificationNumber) {
    controller.removeParcel(parcelIdentificationNumber);
  }

  @Override
  public void editProperty(int identificationNumber) {
    Property foundedProperty = controller.findProperty(identificationNumber);

    NewDataDialog newDataDialog =
        new NewDataDialog(this, controller, SpatialDataType.PROPERTY, foundedProperty);
  }

  @Override
  public void editParcel(int identificationNumber) {
    Parcel foundedParcel = controller.findParcel(identificationNumber);

    NewDataDialog newDataDialog =
        new NewDataDialog(this, controller, SpatialDataType.PARCEL, foundedParcel);
  }

  @Override
  public void generateData(int numberOfProperties, int numberOfParcels) {
    controller.generateData(numberOfProperties, numberOfParcels);
  }

  @Override
  public void initializeBothDynamicHashFiles() {
    resultText.setText("");

    InitializeStructureDialog initializeStructureDialogParcels =
        new InitializeStructureDialog(this, SpatialDataType.PARCEL);

    initializeStructureDialogParcels.attach(this);
    initializeStructureDialogParcels.setVisible(true);

    InitializeStructureDialog initializeStructureDialogProperties =
        new InitializeStructureDialog(this, SpatialDataType.PROPERTY);

    initializeStructureDialogProperties.attach(this);
    initializeStructureDialogProperties.setVisible(true);
  }

  @Override
  public void setParcelDynamicHashFileInfo(DynamicHashFile<Parcel> dynamicHashFile) {
    parcelHashFileInfo.setText(dynamicHashFile.sequenceToStringMainFile());
  }

  @Override
  public void setPropertyDynamicHashFileInfo(DynamicHashFile<Property> dynamicHashFile) {
    propertyHashFileInfo.setText(dynamicHashFile.sequenceToStringMainFile());
  }

  @Override
  public void showPopupMessage(String message) {
    // setting maximum size - errors are often long
    JTextArea errorTextArea = new JTextArea(message);
    errorTextArea.setLineWrap(true);
    errorTextArea.setWrapStyleWord(true);
    errorTextArea.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(errorTextArea);
    scrollPane.setPreferredSize(new Dimension(500, 300));

    JOptionPane.showMessageDialog(this, scrollPane, "Nastala chyba :(", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public JFrame getJFrameObject() {
    return null;
  }

  @Override
  public void update(IObservable observable) {
    if (!(observable instanceof IStructuresParametersObservable)) {
      return;
    }

    StructuresParameters parameters =
        ((IStructuresParametersObservable) observable).getStructuresParameters();

    if (parameters.getDataType() == SpatialDataType.PARCEL) {
      controller.initializeParcelDynamicHashFile(
          parameters.getPathToMainFile(),
          parameters.getPathToOverflowFile(),
          parameters.getMainFileBlockingFactor(),
          parameters.getOverflowFileBlockingFactor());
      controller.initializeParcelQuadTree(
          parameters.getQuadTreeHeight(), parameters.getQuadTreeShape());
    }

    if (parameters.getDataType() == SpatialDataType.PROPERTY) {
      controller.initializePropertyDynamicHashFile(
          parameters.getPathToMainFile(),
          parameters.getPathToOverflowFile(),
          parameters.getMainFileBlockingFactor(),
          parameters.getOverflowFileBlockingFactor());
      controller.initializePropertyQuadTree(
          parameters.getQuadTreeHeight(), parameters.getQuadTreeShape());
    }
  }
}

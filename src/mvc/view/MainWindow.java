package mvc.view;

import entity.Parcel;
import entity.Property;
import entity.shape.Rectangle;
import mvc.controller.IController;

import javax.swing.*;

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

    public MainWindow(IController controller) {
        this.controller = controller;
        setContentPane(mainPanel);
    setTitle("Szathmáry_AUS2 - Semestrálna práca č. 1");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1200, 900);
    setLocationRelativeTo(null);
    setVisible(true);

    insertNewPropertyButton.addActionListener(
        e -> {
//          NewDataDialog newPropertyDialog =
//              new NewDataDialog(this, this.controller, DataType.PROPERTY);
        });

    insertNewParcelButton.addActionListener(
        e -> {
//          NewDataDialog newParcelDialog = new NewDataDialog(this, this.controller, DataType.PARCEL);
        });

    seachPropertiesButton.addActionListener(
        e -> {
//          GetShapeDialog getShapeDialog =
//              new GetShapeDialog(this, SearchCriteria.PROPERTIES, OperationType.SEARCH);
        });

    searchParcelsButton.addActionListener(
        e -> {
//          GetShapeDialog getShapeDialog =
//              new GetShapeDialog(this, SearchCriteria.PARCELS, OperationType.SEARCH);
        });

    resetFilesButton.addActionListener(
        e -> {
            //TODO inicializacia quat trees a dynamic hash files
          resultText.setText("");
        });

        editPropertyButton.addActionListener(
        e -> {
//            GetShapeDialog getShapeDialog =
//                    new GetShapeDialog(this, SearchCriteria.PROPERTIES, OperationType.EDIT);
        });

    editParcelButton.addActionListener(
        e -> {
//          GetShapeDialog getShapeDialog =
//              new GetShapeDialog(this, SearchCriteria.PARCELS, OperationType.EDIT);
        });

    deletePropertyButton.addActionListener(
        e -> {
//          GetShapeDialog getShapeDialog =
//              new GetShapeDialog(this, SearchCriteria.PROPERTIES, OperationType.DELETE);
        });

    deleteParcelButton.addActionListener(
        e -> {
//          GetShapeDialog getShapeDialog =
//              new GetShapeDialog(this, SearchCriteria.PARCELS, OperationType.DELETE);
        });

    generateDataButton.addActionListener(
        e -> {
//          GenerateDataDialog generateDataDialog = new GenerateDataDialog(this);
        });

    saveButton.addActionListener(
        e -> {
//          saveDataFromFile("parcels.csv", DataType.PARCEL, new CsvBuilder());
//          saveDataFromFile("properties.csv", DataType.PROPERTY, new CsvBuilder());
        });

    loadButton.addActionListener(
        e -> {
//          loadDataFromFile("parcels.csv", new CsvBuilder());
//          loadDataFromFile("properties.csv", new CsvBuilder());
        });
    }

    @Override
    public Property findProperty(int propertyIdentificationNumber) {
        return null;
    }

    @Override
    public Parcel findParcel(int parcelIdentificationNumber) {
        return null;
    }

    @Override
    public void insertProperty(int registrationNumber, String description, Rectangle shape) {

    }

    @Override
    public void insertParcel(String description, Rectangle shape) {

    }

    @Override
    public void removeProperty(Property property) {

    }

    @Override
    public void removeParcel(Parcel parcel) {

    }

    @Override
    public void editProperty(Property property) {

    }

    @Override
    public void editParcel(Parcel parcel) {

    }

    @Override
    public void generateData(int numberOfProperties, int numberOfParcels) {

    }

    @Override
    public void showPopupMessage(String message) {

    }

    @Override
    public JFrame getJFrameObject() {
        return null;
    }
}

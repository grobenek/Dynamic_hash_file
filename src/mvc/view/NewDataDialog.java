package mvc.view;

import entity.Parcel;
import entity.Property;
import entity.SpatialData;
import entity.SpatialDataType;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.awt.event.*;
import javax.swing.*;
import mvc.controller.IController;

public class NewDataDialog extends JDialog {
  private final IController controller;
  private final SpatialDataType dataType;
  private final SpatialData<?> orignalDataBeforeEdit;
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextField descriptionTextField;
  private JLabel descriptionLabel;
  private JPanel shapeJPanel;
  private JLabel shapeLabel;
  private JTextField x1TextField;
  private JLabel x1Label;
  private JTextField y1TextField;
  private JLabel y1Label;
  private JTextField x2TextField;
  private JTextField y2TextField;
  private JLabel x2Label;
  private JLabel y2Label;
  private JTextField registrationNumberTextField;
  private JLabel registrationNumberLabel;

  public NewDataDialog(
      JFrame mainWindow,
      IController controller,
      SpatialDataType dataType,
      SpatialData<?> dataToFillIntoTextFields) {
    this.orignalDataBeforeEdit = dataToFillIntoTextFields;
    this.dataType = dataType;
    this.controller = controller;

    if (dataToFillIntoTextFields instanceof Parcel parcelToAddData) {
      registrationNumberLabel.setVisible(false);
      registrationNumberTextField.setVisible(false);

      descriptionTextField.setText(parcelToAddData.getDescription().getTruncatedString());

      Rectangle shapeOfParcel = parcelToAddData.getShapeOfData();
      x1TextField.setText(String.valueOf(shapeOfParcel.getFirstPoint().widthCoordinate()));
      y1TextField.setText(String.valueOf(shapeOfParcel.getFirstPoint().lengthCoordinate()));
      x2TextField.setText(String.valueOf(shapeOfParcel.getSecondPoint().widthCoordinate()));
      y2TextField.setText(String.valueOf(shapeOfParcel.getSecondPoint().lengthCoordinate()));
    }

    if (dataToFillIntoTextFields instanceof Property propertyToAdd) {
      registrationNumberTextField.setText(String.valueOf(propertyToAdd.getRegistrationNumber()));
      descriptionTextField.setText(propertyToAdd.getDescription().getTruncatedString());

      Rectangle shapeOfParcel = propertyToAdd.getShapeOfData();
      x1TextField.setText(String.valueOf(shapeOfParcel.getFirstPoint().widthCoordinate()));
      y1TextField.setText(String.valueOf(shapeOfParcel.getFirstPoint().lengthCoordinate()));
      x2TextField.setText(String.valueOf(shapeOfParcel.getSecondPoint().widthCoordinate()));
      y2TextField.setText(String.valueOf(shapeOfParcel.getSecondPoint().lengthCoordinate()));
    }

    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(
        e -> {
          Rectangle rectangle =
              new Rectangle(
                  new GpsCoordinates(
                      Direction.S,
                      Double.parseDouble(x1TextField.getText()),
                      Direction.W,
                      Double.parseDouble(y1TextField.getText())),
                  new GpsCoordinates(
                      Direction.N,
                      Double.parseDouble(x2TextField.getText()),
                      Direction.E,
                      Double.parseDouble(y2TextField.getText())));

          switch (dataType) {
            case PROPERTY -> onOK(
                new Property(
                    dataToFillIntoTextFields.getIdentificationNumber(),
                    Integer.parseInt(registrationNumberTextField.getText()),
                    descriptionTextField.getText(),
                    rectangle));

            case PARCEL -> onOK(
                new Parcel(
                    dataToFillIntoTextFields.getIdentificationNumber(),
                    descriptionTextField.getText(),
                    rectangle));
          }
        });

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    setSize(200, 400);
    setAutoRequestFocus(true);
    setTitle("Edit Data");
    setLocationRelativeTo(mainWindow);
    setVisible(true);
  }

  public NewDataDialog(JFrame mainWindow, IController controller, SpatialDataType dataType) {
    this.orignalDataBeforeEdit = null;
    this.dataType = dataType;

    registrationNumberTextField.setVisible(this.dataType == SpatialDataType.PROPERTY);
    registrationNumberLabel.setVisible(this.dataType == SpatialDataType.PROPERTY);

    this.controller = controller;
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    setSize(200, 400);
    setAutoRequestFocus(true);
    setTitle("Create new Property");
    setLocationRelativeTo(mainWindow);
    setVisible(true);
  }

  private void onOK(Object filledData) {

    GpsCoordinates bottomLeftPoint =
        new GpsCoordinates(
            Direction.S,
            Float.parseFloat(x1TextField.getText()),
            Direction.W,
            Float.parseFloat(y1TextField.getText()));
    GpsCoordinates topRightPoint =
        new GpsCoordinates(
            Direction.N,
            Float.parseFloat(x2TextField.getText()),
            Direction.E,
            Float.parseFloat(y2TextField.getText()));

    Rectangle rectangleOfEditedData = new Rectangle(bottomLeftPoint, topRightPoint);

    if (filledData instanceof Parcel filledParcel) {
      ((Parcel) filledData).setShape(rectangleOfEditedData);

      controller.editParcel((Parcel) orignalDataBeforeEdit, filledParcel);
    }

    if (filledData instanceof Property filledProperty) {
      ((Property) filledData).setShape(rectangleOfEditedData);

      controller.editProperty((Property) orignalDataBeforeEdit, filledProperty);
    }

    dispose();
  }

  private void onOK() {
    GpsCoordinates bottomLeftPoint =
        new GpsCoordinates(
            Direction.S,
            Float.parseFloat(x1TextField.getText()),
            Direction.W,
            Float.parseFloat(y1TextField.getText()));
    GpsCoordinates topRightPoint =
        new GpsCoordinates(
            Direction.N,
            Float.parseFloat(x2TextField.getText()),
            Direction.E,
            Float.parseFloat(y2TextField.getText()));

    switch (dataType) {
      case PROPERTY -> {
        controller.insertProperty(
            Integer.parseInt(registrationNumberTextField.getText()),
            descriptionTextField.getText(),
            new Rectangle(bottomLeftPoint, topRightPoint));
      }
      case PARCEL -> {
        controller.insertParcel(
            descriptionTextField.getText(), new Rectangle(bottomLeftPoint, topRightPoint));
      }
    }
    dispose();
  }

  private void onCancel() {
    dispose();
  }
}

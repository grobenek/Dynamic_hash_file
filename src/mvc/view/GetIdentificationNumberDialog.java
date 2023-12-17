package mvc.view;

import entity.SpatialDataType;
import java.awt.event.*;
import javax.swing.*;
import mvc.view.entity.OperationType;

public class GetIdentificationNumberDialog extends JDialog {
  private final OperationType operationType;
  private final SpatialDataType dataType;
  private final IMainWindow mainWindow;
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextField identificationNumberTextField;
  private JLabel identificationNumberLabel;

  public GetIdentificationNumberDialog(
      IMainWindow mainWindow, SpatialDataType dataType, OperationType operationType) {
    this.operationType = operationType;
    this.dataType = dataType;
    this.mainWindow = mainWindow;

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

    setSize(400, 200);
    setAutoRequestFocus(true);
    setTitle("Write identification number for operation");
    setLocationRelativeTo(mainWindow.getJFrameObject());
    setVisible(true);
  }

  private void onOK() {
    int identificationNumber = Integer.parseInt(this.identificationNumberTextField.getText());

    setVisible(false);
    switch (dataType) {
      case PROPERTY -> {
        switch (operationType) {
          case EDIT -> mainWindow.editProperty(identificationNumber);
          case SEARCH -> mainWindow.findProperty(identificationNumber);
          case DELETE -> mainWindow.removeProperty(identificationNumber);
        }
      }

      case PARCEL -> {
        switch (operationType) {
          case EDIT -> mainWindow.editParcel(identificationNumber);
          case SEARCH -> mainWindow.findParcel(identificationNumber);
          case DELETE -> mainWindow.removeParcel(identificationNumber);
        }
      }
    }

    dispose();
  }

  private void onCancel() {
    dispose();
  }
}

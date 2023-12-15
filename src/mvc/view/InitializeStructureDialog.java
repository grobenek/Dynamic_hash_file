package mvc.view;

import entity.SpatialDataType;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import mvc.view.entity.StructuresParameters;
import mvc.view.observable.IObserver;
import mvc.view.observable.IStructuresParametersObservable;

public class InitializeStructureDialog extends JDialog implements IStructuresParametersObservable {
  private final List<IObserver> observers;
  private final SpatialDataType dataType;
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextField mainFilePathTextField;
  private JLabel mainFilePathLabel;
  private JTextField mainFileBlockingFactorTextField;
  private JLabel mainPathBlockingFactorLabel;
  private JTextField overflowFilePathTextField;
  private JLabel oveflowFilePathLabel;
  private JTextField overflowFileBlockingFactorTextField;
  private JLabel overflowFileBlockingFactorLabel;
  private JTextField quadTreeHeightTextField;
  private JLabel quadTreeHeightLabel;
  private JTextField quadTreeX1TextField;
  private JLabel quadTreeX1Label;
  private JTextField quadTreeY1TextField;
  private JLabel quadTreeY1Label;
  private JTextField quadTreeX2TextField;
  private JLabel quadTreeX2Label;
  private JTextField quadTreeY2TextField;
  private JLabel quadTreeY2Label;

  public InitializeStructureDialog(IMainWindow mainWindow, SpatialDataType dataType) {
    this.observers = new ArrayList<>();
    this.dataType = dataType;

    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    setSize(500, 400);
    setAutoRequestFocus(true);
    setTitle(String.format("Initialize structures for %s", this.dataType.name()));
    setLocationRelativeTo(mainWindow.getJFrameObject());
  }

  private void onOK() {
    sendNotifications();
    dispose();
  }

  private void onCancel() {
    dispose();
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
  public StructuresParameters getStructuresParameters() {
    return new StructuresParameters(
        mainFilePathTextField.getText(),
        Integer.parseInt(mainFileBlockingFactorTextField.getText()),
        overflowFilePathTextField.getText(),
        Integer.parseInt(overflowFileBlockingFactorTextField.getText()),
        Integer.parseInt(quadTreeHeightTextField.getText()),
        Double.parseDouble(quadTreeX1TextField.getText()),
        Double.parseDouble(quadTreeY1TextField.getText()),
        Double.parseDouble(quadTreeX2TextField.getText()),
        Double.parseDouble(quadTreeY2TextField.getText()),
        dataType);
  }
}

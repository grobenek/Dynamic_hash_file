package mvc.view;

import java.awt.event.*;
import javax.swing.*;

public class GenerateDataDialog extends JDialog {
  private final IMainWindow mainWindow;
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextField numberOfParcelsTextField;
  private JTextField numberOfPropertiesTextField;
  private JLabel numberOfPropertiesLabel;
  private JLabel numberOfParcelsLabel;

  public GenerateDataDialog(IMainWindow mainWindow) {
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

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    setSize(500, 200);
    setAutoRequestFocus(true);
    setTitle("Set size of generated data");
    setLocationRelativeTo(mainWindow.getJFrameObject());
    setVisible(true);
  }

  private void onOK() {
    setVisible(false);
    mainWindow.generateData(
        Integer.parseInt(numberOfPropertiesTextField.getText()),
        Integer.parseInt(numberOfParcelsTextField.getText()));
    dispose();
  }

  private void onCancel() {
    dispose();
  }
}

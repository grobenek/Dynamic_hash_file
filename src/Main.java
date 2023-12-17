import mvc.controller.Controller;
import mvc.controller.IController;
import mvc.model.IModel;
import mvc.model.ModelWrapper;
import mvc.view.IMainWindow;
import mvc.view.MainWindow;

public class Main {
  public static void main(String[] args) {
    IModel model = new ModelWrapper();
    IController controller = new Controller(model);
    IMainWindow mainWindow = new MainWindow(controller);
    controller.setView(mainWindow);
  }
}

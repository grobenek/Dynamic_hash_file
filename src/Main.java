import mvc.controller.Controller;
import mvc.controller.IController;
import mvc.model.IModel;
import mvc.model.ModelWrapper;
import mvc.view.IMainWindow;
import mvc.view.MainWindow;

public class Main {
  public static void main(String[] args) {
    try (IModel model = new ModelWrapper()) {
      IController controller = new Controller(model);
      IMainWindow mainWindow = new MainWindow(controller);
      controller.setView(mainWindow); //TODO spravit gui, prerobit a postupne vyrabat modal
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  //    Rectangle rectangle =
  //        new Rectangle(
  //            new GpsCoordinates(Direction.S, 0, Direction.W, 0),
  //            new GpsCoordinates(Direction.N, 10.0, Direction.E, 10.0));
  //
  //    try (IModel model =
  //        new ModelWrapper(
  //            new QuadTree<>(10, rectangle),
  //            new QuadTree<>(10, rectangle),
  //            new DynamicHashFile<>(
  //                "testProperty.sz", "overflowProperty.sz", 5, 10, 10, Property.class),
  //            new DynamicHashFile<>("testParcel.sz", "overflowParcel.sz", 5, 10, 10,
  // Parcel.class),
  //            0,
  //            0); ) {
  //
  //      model.insertParcel(
  //          "Prva",
  //          new Rectangle(
  //              new GpsCoordinates(Direction.S, 0, Direction.W, 0),
  //              new GpsCoordinates(Direction.N, 5, Direction.E, 5)));
  //
  //      System.out.println(model.findParcel(1));
  //
  //      model.insertProperty(1, "Prva", new Rectangle(
  //              new GpsCoordinates(Direction.S, 1, Direction.W, 2),
  //              new GpsCoordinates(Direction.N, 0, Direction.E, 4)));
  //
  //      model.insertProperty(1, "Druha", new Rectangle(
  //              new GpsCoordinates(Direction.S, 2, Direction.W, 3),
  //              new GpsCoordinates(Direction.N, 1, Direction.E, 2)));
  //
  //      System.out.println(model.findProperty(1));
  //
  //      model.insertParcel("druha", new Rectangle(
  //              new GpsCoordinates(Direction.S, 1.5, Direction.W, 1.8),
  //              new GpsCoordinates(Direction.N, 0, Direction.E, 3.9)));
  //
  //      System.out.println(model.findProperty(1));
  //
  //      System.out.println(model.findParcel(1));
  //
  //    } catch (Exception e) {
  //      throw new RuntimeException(e);
  //    }
  //  }
  //    Rectangle rectangle =
  //        new Rectangle(
  //            new GpsCoordinates(Direction.S, 0, Direction.W, 0),
  //            new GpsCoordinates(Direction.N, 10.0, Direction.E, 10.0));
  //
  //    byte[] byteArray = rectangle.toByteArray();
  //
  //    LimitedString limitedString = new LimitedString(10, "Testovaci");
  //
  //    //    System.out.println(limitedString.getString());
  //
  //    byte[] limitedStringByte = limitedString.toByteArray();
  //
  //    LimitedString fromBytes = new LimitedString();
  //    fromBytes.fromByteArray(limitedStringByte);
  //
  //    //    System.out.println(fromBytes.equals(limitedString));
  //
  //    Rectangle newRectangle = new Rectangle();
  //    newRectangle.fromByteArray(byteArray);
  //
  //    //    System.out.println(rectangle.equals(newRectangle));
  //    Parcel parcel = new Parcel(1, "testik", rectangle);
  //    Property property = new Property(14156, 123, "abcdasddassdfdasdas", rectangle,
  // List.of(parcel));
  //    parcel.addRelatedData(property);
  //
  //    byte[] propertyByteArray = property.toByteArray();
  //
  //    Property restoredProperty = (Property) RecordDataFactory.fromByteArray(propertyByteArray);
  //
  //    Property property2 = new Property(51345256, 461, "testicek", rectangle, List.of(parcel));
  //    Property property3 = new Property(98765, 462, "testicek2", rectangle, List.of(parcel));
  //    Property property4 = new Property(3, 463, "testicek3", rectangle, List.of(parcel));
  //    Property property5 = new Property(6136, 464, "testicek4", rectangle, List.of(parcel));
  //    Property property6 = new Property(8653456, 465, "testicek5", rectangle, List.of(parcel));
  //    Property property7 = new Property(34, 466, "testicek6", rectangle, List.of(parcel));
  //    Property property8 = new Property(897125343, 467, "testicek7", rectangle, List.of(parcel));
  //    Property property9 = new Property(676534136, 468, "testicek8", rectangle, List.of(parcel));
  //    Property property10 = new Property(865123356, 469, "testicek9", rectangle, List.of(parcel));
  //    Property property11 = new Property(734, 470, "testicek10", rectangle, List.of(parcel));
  //    Property property12 = new Property(945671343, 471, "testicek11", rectangle,
  // List.of(parcel));
  //
  //    try (DynamicHashFile<Property> dynamicHashFile =
  //        new DynamicHashFile<>("test.sz", "overflow.sz", 5, 10, 10, Property.class)) {
  //      dynamicHashFile.insert(property);
  //      dynamicHashFile.insert(property2);
  //      dynamicHashFile.insert(property3);
  //      dynamicHashFile.insert(property4);
  //      dynamicHashFile.insert(property5);
  //      dynamicHashFile.insert(property6);
  //      dynamicHashFile.insert(property7);
  //      dynamicHashFile.insert(property8);
  //      dynamicHashFile.insert(property9);
  //      dynamicHashFile.insert(property10);
  //      dynamicHashFile.insert(property11);
  //      dynamicHashFile.insert(property12);
  //
  //            System.out.println("FOUNDED: " + dynamicHashFile.find(property2));
  //
  ////      System.out.println(
  ////          "BEFORE DELETE MAIN BLOCK\n: " + dynamicHashFile.sequenceToStringMainFile());
  ////      System.out.println(
  ////          "BEFORE DELETE OVERFLOW BLOCK\n: " +
  // dynamicHashFile.sequenceToStringOverflowFile());
  ////
  ////      dynamicHashFile.delete(property2);
  //      //
  //      //      System.out.println("\nAFTER DELETE MAIN BLOCK\n: " +
  //      // dynamicHashFile.sequenceToStringMainFile());
  //      //      System.out.println("AFTER DELETE OVERFLOW BLOCK\n: " +
  //      // dynamicHashFile.sequenceToStringOverflowFile());
  //
  //      System.out.println("FOUNDED: " + dynamicHashFile.find(property2));
  //    } catch (Exception e) {
  //      throw new RuntimeException(e);
  //    }
  //  }
}

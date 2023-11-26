import entity.Parcel;
import entity.Property;
import entity.SpatialDataFactory;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.util.List;
import structure.dynamichashfile.DynamicHashFile;
import structure.dynamichashfile.LimitedString;

public class Main {
  public static void main(String[] args) {
    Rectangle rectangle =
        new Rectangle(
            new GpsCoordinates(Direction.S, 0, Direction.W, 0),
            new GpsCoordinates(Direction.N, 10.0, Direction.E, 10.0));

    byte[] byteArray = rectangle.toByteArray();

    LimitedString limitedString = new LimitedString(10, "Testovaci");

    //    System.out.println(limitedString.getString());

    byte[] limitedStringByte = limitedString.toByteArray();

    LimitedString fromBytes = new LimitedString();
    fromBytes.fromByteArray(limitedStringByte);

    //    System.out.println(fromBytes.equals(limitedString));

    Rectangle newRectangle = new Rectangle();
    newRectangle.fromByteArray(byteArray);

    //    System.out.println(rectangle.equals(newRectangle));
    Parcel parcel = new Parcel(1, "testik", rectangle);
    Property property = new Property(14156, 123, "abcdasddassdfdasdas", rectangle, List.of(parcel));
    parcel.addRelatedData(property);

    byte[] propertyByteArray = property.toByteArray();

    Property restoredProperty = (Property) SpatialDataFactory.fromByteArray(propertyByteArray);

    Property property2 = new Property(51345256, 461, "testicek", rectangle, List.of(parcel));
    Property property3 = new Property(98765, 462, "testicek2", rectangle, List.of(parcel));
    Property property4 = new Property(3, 463, "testicek3", rectangle, List.of(parcel));
    Property property5 = new Property(6136, 464, "testicek4", rectangle, List.of(parcel));
    Property property6 = new Property(8653456, 465, "testicek5", rectangle, List.of(parcel));
    Property property7 = new Property(34, 466, "testicek6", rectangle, List.of(parcel));
    Property property8 = new Property(894171343, 467, "testicek7", rectangle, List.of(parcel));

    try (DynamicHashFile<Property> dynamicHashFile =
        new DynamicHashFile<>("test.sz", 5, 10, Property.class)) {
      dynamicHashFile.insert(property);
      dynamicHashFile.insert(property2);
      dynamicHashFile.insert(property3);
      dynamicHashFile.insert(property4);
      dynamicHashFile.insert(property5);
      dynamicHashFile.insert(property6);
      dynamicHashFile.insert(property7);
      dynamicHashFile.insert(property8);

      System.out.println(dynamicHashFile.sequenceToString());

      System.out.println("FOUNDED: " + dynamicHashFile.find(property2));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

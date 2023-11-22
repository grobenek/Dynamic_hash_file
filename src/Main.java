import entity.Parcel;
import entity.Property;
import entity.SpatialData;
import entity.SpatialDataFactory;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import entity.shape.RectangleFactory;
import java.util.List;
import structure.dynamichashfile.LimitedString;

public class Main {
  public static void main(String[] args) {
    Rectangle rectangle =
        new Rectangle(
            new GpsCoordinates(Direction.S, 0, Direction.W, 0),
            new GpsCoordinates(Direction.N, 10.0, Direction.E, 10.0));

    byte[] byteArray = rectangle.toByteArray();

    LimitedString limitedString = new LimitedString(10, "Testovaci");

    System.out.println(limitedString.getString());

    byte[] limitedStringByte = limitedString.toByteArray();

    LimitedString fromBytes = limitedString.fromByteArray(limitedStringByte);

    System.out.println(fromBytes.equals(limitedString));

    Rectangle newRectangle = RectangleFactory.fromByteArray(byteArray);

    System.out.println(rectangle.equals(newRectangle));
    Parcel parcel = new Parcel(1, "testik", rectangle);
    Property property = new Property(1, 1, "abcd", rectangle, List.of(parcel));
    parcel.addRelatedData(property);

    byte[] propertyByteArray = property.toByteArray();

    SpatialData restoredProperty = SpatialDataFactory.fromByteArray(propertyByteArray, true);

    System.out.println(property);
    System.out.println(restoredProperty);
  }
}

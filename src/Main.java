import entity.Parcel;
import entity.Property;
import entity.SpatialDataFactory;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.util.List;

import structure.dynamichashfile.Block;
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

    Block block = new Block(3);
    block.addRecord(property);
    block.addRecord(restoredProperty);

    byte[] blockInBytes = block.toByteArray();

    Block newBlock = new Block();
    newBlock.fromByteArray(blockInBytes);

    System.out.println(block);
    System.out.println(newBlock);

    //    System.out.println(property);
    //    System.out.println(restoredProperty);
  }
}

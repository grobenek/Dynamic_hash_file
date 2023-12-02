package structure.dynamichashfile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import entity.Property;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DynamicHashFileTest {
  private static final int NUMBER_OF_REPETETIONS = 100;

  @Test
  void testInsertAndFind() {

    Rectangle shape =
        new Rectangle(
            new GpsCoordinates(Direction.S, 0.0, Direction.W, 0.0),
            new GpsCoordinates(Direction.N, 10.0, Direction.E, 10.0));

    for (int repetetion = 0; repetetion < NUMBER_OF_REPETETIONS; repetetion++) {
      Random random = new Random(repetetion);

      List<Property> propertyList = new ArrayList<>();

      int NUMBER_OF_PROPERTIES = 20;
      for (int i = 0; i < NUMBER_OF_PROPERTIES; i++) {
        propertyList.add(new Property(i + random.nextInt(100), i, String.valueOf(i), shape));
      }

      try (DynamicHashFile<Property> dynamicHashFile =
          new DynamicHashFile<>("test.sz", "overflow.sz", 5, 10, 10, Property.class)) {
        for (int i = 0; i < NUMBER_OF_PROPERTIES; i++) {
          dynamicHashFile.insert(propertyList.get(i));
        }

        for (int i = NUMBER_OF_PROPERTIES - 1; i > 0; i--) {
          assertEquals(propertyList.get(i), dynamicHashFile.find(propertyList.get(i)));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}

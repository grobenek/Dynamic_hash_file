package structure.dynamichashfile;

import static org.junit.jupiter.api.Assertions.*;

import entity.Property;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DynamicHashFileTest {
  private static final int NUMBER_OF_REPETETIONS = 100;
  private static final int NUMBER_OF_ACTIONS_IN_REPETETION = 100;

  @Test
  void testInsertAndFind() {

    Rectangle shape =
        new Rectangle(
            new GpsCoordinates(Direction.S, 0.0, Direction.W, 0.0),
            new GpsCoordinates(Direction.N, 10.0, Direction.E, 10.0));

    for (int repetetion = 0; repetetion < NUMBER_OF_REPETETIONS; repetetion++) {
      Random random = new Random(repetetion);

      List<Property> propertyList = new ArrayList<>();

      int NUMBER_OF_PROPERTIES = 10000;
      for (int i = 0; i < NUMBER_OF_PROPERTIES; i++) {
        propertyList.add(new Property(i + random.nextInt(100), i, String.valueOf(i), shape));
      }

      try (DynamicHashFile<Property> dynamicHashFile =
          new DynamicHashFile<>("test.sz", "overflow.sz", 5, 10, 3, Property.class)) {
        for (int j = 0; j < NUMBER_OF_PROPERTIES; j++) {
          dynamicHashFile.insert(propertyList.get(j));
        }

        for (int i = 0; i < NUMBER_OF_PROPERTIES; i++) {
          assertEquals(propertyList.get(i), dynamicHashFile.find(propertyList.get(i)));
        }

        System.out.println(dynamicHashFile.sequenceToStringMainFile());
        System.out.println(dynamicHashFile.sequenceToStringOverflowFile());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  void testAllOperations() {
    int NUMBER_OF_INSERTS_IN_100 = 40;
    int NUMBER_OF_DELETES_IN_100 = 30;
    int NUMBER_OF_SEARCHES_IN_100 = 30;

    if (NUMBER_OF_DELETES_IN_100 + NUMBER_OF_INSERTS_IN_100 + NUMBER_OF_SEARCHES_IN_100 != 100) {
      throw new IllegalArgumentException("Number of operations does not add up to 100!");
    }

    for (int repetetion = 0; repetetion < NUMBER_OF_REPETETIONS; repetetion++) {
      Random random = new Random(repetetion);

      List<Property> insertedItems = new ArrayList<>();

      try (DynamicHashFile<Property> dynamicHashFile =
          new DynamicHashFile<>("test.sz", "overflow.sz", 5, 10, 3, Property.class)) {
        for (int i = 0; i < NUMBER_OF_ACTIONS_IN_REPETETION; i++) {
          GpsCoordinates firstPoint =
              new GpsCoordinates(
                  Direction.S, random.nextDouble(1000), Direction.W, random.nextDouble(1000));
          GpsCoordinates secondPoint =
              new GpsCoordinates(
                  Direction.S, random.nextDouble(1000), Direction.W, random.nextDouble(1000));

          Rectangle rectangle = new Rectangle(firstPoint, secondPoint);

          Property property = new Property(i, i, String.valueOf(i), rectangle);

          int chance = random.nextInt(100);

          if (chance <= NUMBER_OF_SEARCHES_IN_100) {
            seachAndTestResult(random, insertedItems, dynamicHashFile);
          } else if (chance <= NUMBER_OF_SEARCHES_IN_100 + NUMBER_OF_DELETES_IN_100) {
            deleteAndTestResult(random, insertedItems, dynamicHashFile);
          } else {
            insertAndTestResult(insertedItems, property, dynamicHashFile);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void insertAndTestResult(
      List<Property> insertedItems, Property property, DynamicHashFile<Property> dynamicHashFile) {
    assertDoesNotThrow(() -> dynamicHashFile.insert(property));

    assertEquals(property, dynamicHashFile.find(property));

    insertedItems.add(property);
  }

  private void deleteAndTestResult(
      Random random, List<Property> insertedItems, DynamicHashFile<Property> dynamicHashFile)
      throws IOException {
    if (insertedItems.isEmpty()) {
      assertThrows(IllegalArgumentException.class, () -> dynamicHashFile.delete(null));
      return;
    }

    int index = random.nextInt(insertedItems.size());

    Property propertyToDelete = insertedItems.get(index);

    dynamicHashFile.delete(propertyToDelete);

    assertThrows(IllegalStateException.class, () -> dynamicHashFile.find(propertyToDelete));

    insertedItems.remove(propertyToDelete);
  }

  private void seachAndTestResult(
      Random random, List<Property> insertedItems, DynamicHashFile<Property> dynamicHashFile) {
    if (insertedItems.isEmpty()) {
      assertThrows(IllegalArgumentException.class, () -> dynamicHashFile.find(null));
      return;
    }

    int index = random.nextInt(insertedItems.size());

    Property propertyToSearch = insertedItems.get(index);

    Property foundedProperty = dynamicHashFile.find(propertyToSearch);

    assertEquals(propertyToSearch, foundedProperty);
  }
}

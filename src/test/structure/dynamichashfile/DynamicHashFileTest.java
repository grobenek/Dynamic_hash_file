package structure.dynamichashfile;

import static org.junit.jupiter.api.Assertions.*;

import entity.Parcel;
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
  private static final int NUMBER_OF_REPETETIONS = 1000;
  private static final int NUMBER_OF_ACTIONS_IN_REPETETION = 500;

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

      List<Parcel> insertedItems = new ArrayList<>();

      try (DynamicHashFile<Parcel> dynamicHashFile =
          new DynamicHashFile<>("test.sz", "overflow.sz", 5, 10, 3, Parcel.class)) {
        for (int i = 0; i < NUMBER_OF_ACTIONS_IN_REPETETION; i++) {
          GpsCoordinates firstPoint =
              new GpsCoordinates(
                  Direction.S, random.nextDouble(1000), Direction.W, random.nextDouble(1000));
          GpsCoordinates secondPoint =
              new GpsCoordinates(
                  Direction.S, random.nextDouble(1000), Direction.W, random.nextDouble(1000));

          Rectangle rectangle = new Rectangle(firstPoint, secondPoint);

          Parcel parcel = new Parcel(i, String.valueOf(i), rectangle);

          int chance = random.nextInt(100);

          if (chance <= NUMBER_OF_SEARCHES_IN_100) {
            seachAndTestResult(random, insertedItems, dynamicHashFile);
          } else if (chance <= NUMBER_OF_SEARCHES_IN_100 + NUMBER_OF_DELETES_IN_100) {
            deleteAndTestResult(random, insertedItems, dynamicHashFile);
          } else {
            insertAndTestResult(insertedItems, parcel, dynamicHashFile);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void insertAndTestResult(
      List<Parcel> insertedItems, Parcel parcel, DynamicHashFile<Parcel> dynamicHashFile) {
    assertDoesNotThrow(() -> dynamicHashFile.insert(parcel));

    assertThrows(IllegalStateException.class, () -> dynamicHashFile.insert(parcel));

    assertEquals(parcel, dynamicHashFile.find(parcel));

    insertedItems.add(parcel);
  }

  private void deleteAndTestResult(
      Random random, List<Parcel> insertedItems, DynamicHashFile<Parcel> dynamicHashFile)
      throws IOException {
    if (insertedItems.isEmpty()) {
      assertThrows(IllegalArgumentException.class, () -> dynamicHashFile.delete(null));
      return;
    }

    int index = random.nextInt(insertedItems.size());

    Parcel parcelToDelete = insertedItems.get(index);

    dynamicHashFile.delete(parcelToDelete);

    assertThrows(IllegalStateException.class, () -> dynamicHashFile.find(parcelToDelete));
    assertThrows(IllegalStateException.class, () -> dynamicHashFile.delete(parcelToDelete));

    insertedItems.remove(parcelToDelete);
  }

  private void seachAndTestResult(
      Random random, List<Parcel> insertedItems, DynamicHashFile<Parcel> dynamicHashFile) {
    if (insertedItems.isEmpty()) {
      assertThrows(IllegalArgumentException.class, () -> dynamicHashFile.find(null));
      return;
    }

    int index = random.nextInt(insertedItems.size());

    Parcel parcelToSearch = insertedItems.get(index);

    Parcel foundedParcel = dynamicHashFile.find(parcelToSearch);

    assertEquals(parcelToSearch, foundedParcel);
  }
}

package util.file.quadtree;

import entity.SpatialDataForQuadTree;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import util.file.IFileBuilder;

public class CsvBuilderQuadTreeData<T extends SpatialDataForQuadTree> implements IFileBuilder<T> {
  private static final char DELIMITER = ';';
  private List<T> loadedItems;

  public CsvBuilderQuadTreeData() {
    this.loadedItems = new LinkedList<>();
  }

  @Override
  public void saveToFile(String pathToFile, List<T> itemsToSave) throws IOException {

    if (itemsToSave.isEmpty()) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    sb.append("Object name;IdentificationNumber;X1;Y1;X2;Y2\n");
    sb.append(itemsToSave.get(0).getClass().getName());
    sb.append("\n");

    for (T spatialData : itemsToSave) {
      sb.append(spatialData.getIdentificationNumber());
      sb.append(DELIMITER);
      sb.append(spatialData.getShapeOfData().getFirstPoint().widthCoordinate());
      sb.append(DELIMITER);
      sb.append(spatialData.getShapeOfData().getFirstPoint().lengthCoordinate());
      sb.append(DELIMITER);
      sb.append(spatialData.getShapeOfData().getSecondPoint().widthCoordinate());
      sb.append(DELIMITER);
      sb.append(spatialData.getShapeOfData().getSecondPoint().lengthCoordinate());
      sb.append("\n");
    }

    try (DataOutputStream dataOutputStream =
        new DataOutputStream(new FileOutputStream(pathToFile))) {
      dataOutputStream.writeBytes(sb.toString());
    }
  }

  @Override
  public void loadFromFile(String pathToFile) throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToFile))) {
      // skip header
      bufferedReader.readLine();

      String className = bufferedReader.readLine();

      // getting class to create reading class name from file
      Class<?> classToCreate = Class.forName(className);

      // reading lines
      while (true) {
        String line = bufferedReader.readLine();

        if (line == null) {
          break;
        }

        // creating object using reflections
        String[] data = line.split(String.valueOf(DELIMITER));

        Rectangle shapeOfObject =
            new Rectangle(
                new GpsCoordinates(
                    Direction.S, Float.parseFloat(data[1]), Direction.W, Float.parseFloat(data[2])),
                new GpsCoordinates(
                    Direction.S,
                    Float.parseFloat(data[3]),
                    Direction.W,
                    Float.parseFloat(data[4])));
        Object createdObject =
            classToCreate.getConstructors()[0].newInstance(
                Integer.parseInt(data[0]), shapeOfObject);

        if (createdObject instanceof SpatialDataForQuadTree) {
          loadedItems.add((T) createdObject);
        }
      }
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot load file. Cannot load class from file.");
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Cannot load file: %s. Cannot create instance of class from file.");
    }
  }

  @Override
  public List<T> getLoadedData() {
    return loadedItems;
  }

  @Override
  public void clearLoadedData() {
    loadedItems = new ArrayList<>();
  }
}

package structure.quadtree;

import entity.SpatialData;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.util.*;

public class QuadTree<T extends IShapeData> {
  private int height;
  private Rectangle shape;
  private QuadNode<T> root;
  private int size;
  private int itemsInNorthWest;
  private int itemsInNorthEast;
  private int itemsInSouthWest;
  private int itemsInSouthEast;
  private int itemsInRoot;
  private List<Quadrant> last2WorstQuadrants;

  public QuadTree(int maxHeight, Rectangle shape) {
    this.height = maxHeight;
    this.shape = shape;
    this.root = new QuadNode<>(shape);
    this.size = 0;
    this.itemsInNorthWest = 0;
    this.itemsInNorthEast = 0;
    this.itemsInSouthEast = 0;
    this.itemsInSouthWest = 0;
    this.itemsInRoot = 0;
    this.last2WorstQuadrants = new ArrayList<>(2);
  }

  public Rectangle getShape() {
    return shape;
  }

  public int getHeight() {
    return height;
  }

  public void insert(T data) {
    Rectangle shape = data.getShapeOfData();
    if (shape == null) {
      throw new IllegalArgumentException("Shape of inserted data is null!");
    }

    if (!checkIfDataFitIntoRoot(data)) {
      throw new IllegalArgumentException(
          String.format(
              "Data: %s does not fit into QuadTree with shape: %s", data, root.getShape()));
    }

    // finding right place for data
    findFreeParentForDataAndAddDataToNewChild(root, data, true);
  }

  /**
   * Finds suitable place for data and returns node where data was inserted and insert them if
   * needed
   *
   * @param startingPoint Node from which we look for suitable place
   * @param data data to be added
   * @param insertNewData flag if data should be inserted, or we just need to find place for it
   */
  private QuadNode<T> findFreeParentForDataAndAddDataToNewChild(
      QuadNode<T> startingPoint, T data, boolean insertNewData) {
    if (startingPoint == null) {
      throw new IllegalArgumentException(
          "Cannot find free parent for data when startingPoint is null!");
    }

    QuadNode<T> parentOfPlace = startingPoint;
    Quadrant quadrantForDataToBePlaced = startingPoint.getQuadrantOfShape(data, false);

    // quadrant is null - add data to parent and end
    if (quadrantForDataToBePlaced == null) {
      if (insertNewData) {
        itemsInRoot++;
        startingPoint.addItem(data);
        size++;
        return null;
      }
      return startingPoint;
    }

    // Update item counts in each quadrant
    if (insertNewData) {
      switch (quadrantForDataToBePlaced) {
        case NORTH_EAST -> itemsInNorthEast++;
        case NORTH_WEST -> itemsInNorthWest++;
        case SOUTH_WEST -> itemsInSouthWest++;
        case SOUTH_EAST -> itemsInSouthEast++;
      }
    }

    QuadNode<T> possiblePlaceForData = parentOfPlace.getChild(quadrantForDataToBePlaced);

    // free place for data - generate child and add data and return
    if (possiblePlaceForData == null) {
      if (insertNewData) {
        tryToInsertOldDataIntoNewChildrenAndInsertNewData(parentOfPlace, data);
        size++;
        return null;
      }
      return parentOfPlace;
    }

    // finding free place for items or until max height is reached
    int heightCounter = possiblePlaceForData.getHeight();
    while (possiblePlaceForData != null) {
      if (heightCounter == height) {
        if (insertNewData) {
          possiblePlaceForData.addItem(data);
          size++;
          return null;
        }
        return possiblePlaceForData;
      }

      parentOfPlace = possiblePlaceForData;
      quadrantForDataToBePlaced = parentOfPlace.getQuadrantOfShape(data, false);

      // quadrant is null - add data to parent and end
      if (quadrantForDataToBePlaced == null) {
        if (insertNewData) {
          parentOfPlace.addItem(data);
          size++;
          return null;
        }
        return parentOfPlace;
      }

      possiblePlaceForData = parentOfPlace.getChild(quadrantForDataToBePlaced);
      heightCounter++;
    }
    // free space for data
    if (insertNewData) {
      tryToInsertOldDataIntoNewChildrenAndInsertNewData(parentOfPlace, data);
      size++;
      return null;
    }
    return parentOfPlace;
  }

  private void tryToInsertOldDataIntoNewChildrenAndInsertNewData(
      QuadNode<T> originalParent, T data) {
    Stack<T> stackOfItemsThatNeedSToBeReinserted = new Stack<>();

    // insert data - will process as last
    stackOfItemsThatNeedSToBeReinserted.push(data);
    stackOfItemsThatNeedSToBeReinserted.addAll(originalParent.getItems());
    originalParent.removeAllItems();

    while (!stackOfItemsThatNeedSToBeReinserted.isEmpty()) {
      T itemToWorkWith = stackOfItemsThatNeedSToBeReinserted.pop();

      QuadNode<T> parentOfPlace = originalParent;
      int heightCounter = parentOfPlace.getHeight();

      if (heightCounter == height) {
        parentOfPlace.addItem(itemToWorkWith);
        continue;
      }

      Quadrant quadrantOfItem = parentOfPlace.getQuadrantOfShape(itemToWorkWith, false);

      // item cannot be inserted deeper, adding to parent
      if (quadrantOfItem == null) {
        parentOfPlace.addItem(itemToWorkWith);
        continue;
      }

      QuadNode<T> possiblePlaceForItem = parentOfPlace.getChild(quadrantOfItem);

      // item can be inserted
      if (possiblePlaceForItem == null) {
        parentOfPlace.generateChild(quadrantOfItem).addItem(itemToWorkWith);
        continue;
      }
      heightCounter++;

      // finding possible place for item
      while (true) {
        if (heightCounter == height) {
          parentOfPlace.addItem(itemToWorkWith);
          break;
        }

        parentOfPlace = possiblePlaceForItem;
        quadrantOfItem = parentOfPlace.getQuadrantOfShape(itemToWorkWith, false);

        // item cannot be inserted deeper, adding to parent
        if (quadrantOfItem == null) {
          parentOfPlace.addItem(itemToWorkWith);
          break;
        }

        possiblePlaceForItem = parentOfPlace.getChild(quadrantOfItem);

        // item can be inserted
        if (possiblePlaceForItem == null) {
          if (parentOfPlace.getItemsSize() != 0) {
            stackOfItemsThatNeedSToBeReinserted.addAll(parentOfPlace.getItems());
            parentOfPlace.removeAllItems();
          }
          parentOfPlace.generateChild(quadrantOfItem).addItem(itemToWorkWith);
          break;
        }
        heightCounter++;
      }
    }
  }

  /**
   * Finds and returns all data that are within given shape
   *
   * @param shape shape where to search for data
   * @return List with result data
   */
  public List<T> search(Rectangle shape) {
    if (shape == null) {
      throw new IllegalArgumentException("Cannot find any data for null shape!");
    }

    if (!root.doesOverlapWithRectangle(shape)) {
      throw new IllegalArgumentException(
          String.format(
              "Shape %s does not overlap with quad tree with root %s", shape, root.getShape()));
    }

    Stack<QuadNode<T>> nodesToProcess = new Stack<>();
    QuadNode<T> currentNode = root;

    nodesToProcess.addAll(
        Arrays.stream(currentNode.getChildren())
            .filter(child -> child != null && child.doesOverlapWithRectangle(shape))
            .toList());

    List<T> result = // creating result list with first filtered items from root
        new LinkedList<>(
            currentNode.getItems().stream()
                .filter(item -> item.getShapeOfData().doesOverlapWithRectangle(shape))
                .toList());

    while (!nodesToProcess.empty()) {
      currentNode = nodesToProcess.pop();
      // add children for proccesing when they are overlapping with shape
      nodesToProcess.addAll(
          Arrays.stream(currentNode.getChildren())
              .filter(child -> child != null && child.doesOverlapWithRectangle(shape))
              .toList());

      if (currentNode.getItemsSize() == 0) {
        continue;
      }

      // add item within shape to result
      currentNode.getItems().stream()
          .filter(item -> item.getShapeOfData().doesOverlapWithRectangle(shape))
          .forEach(result::add);
    }

    return result;
  }

  public void deleteData(T data) {
    delete(data);
  }

  private void delete(T data) {
    if (data == null) {
      throw new IllegalArgumentException("Cannot delete data: null!");
    }

    QuadNode<T> nodeOfChild = findFreeParentForDataAndAddDataToNewChild(root, data, false);

    if (nodeOfChild == null) {
      throw new IllegalStateException(String.format("Data %s not found in structure.quadtree", data));
    }

    Quadrant quadrantOfDataInRoot = root.getQuadrantOfShape(data, false);

    T foundData = nodeOfChild.getItem(data);

    if (foundData == null) {
      throw new IllegalStateException(
          String.format("Data %s not found in area %s", data, nodeOfChild.getShape()));
    }

    nodeOfChild.removeItem(foundData);
    size--;

    if (quadrantOfDataInRoot == null) {
      itemsInRoot--;
    } else {
      switch (quadrantOfDataInRoot) {
        case SOUTH_EAST -> itemsInSouthEast--;
        case SOUTH_WEST -> itemsInSouthWest--;
        case NORTH_EAST -> itemsInNorthEast--;
        case NORTH_WEST -> itemsInNorthWest--;
      }
    }

    // is leaf, but it is not empty
    if (nodeOfChild.isLeaf() && nodeOfChild.getItemsSize() != 0) {
      return;
    }

    // leaf is empty
    // remove child from its parent
    if (nodeOfChild.isLeaf() && nodeOfChild.getItemsSize() == 0) {
      QuadNode<T> parentOfChild = nodeOfChild.getParent();

      // parent is null - current node is root
      if (parentOfChild == null) {
        return;
      }

      Quadrant quadrantOfChild = parentOfChild.getQuadrantOfShape(nodeOfChild.getShape(), true);

      if (quadrantOfChild == null) {
        throw new IllegalStateException(
            String.format(
                "Quadrant for child: %s not found in parent %s", nodeOfChild, parentOfChild));
      }

      parentOfChild.removeChild(quadrantOfChild);

      // has another child, cannot remove him
      if (!parentOfChild.isLeaf()) {
        return;
      }

      // parent has 0 children
      // deleting empty parents
      while (true) {
        nodeOfChild = parentOfChild;
        parentOfChild = nodeOfChild.getParent();

        if (!parentOfChild.isLeaf()) {
          return;
        }

        if (nodeOfChild.getItemsSize() != 0) {
          return;
        }

        quadrantOfChild = parentOfChild.getQuadrantOfShape(nodeOfChild.getShape(), true);

        if (quadrantOfChild == null) {
          throw new IllegalStateException(
              String.format(
                  "Quadrant for child: %s not found in parent %s", nodeOfChild, parentOfChild));
        }

        parentOfChild.removeChild(quadrantOfChild);
      }
    }
  }

  public int getSize() {
    return size;
  }

  private boolean checkIfDataFitIntoRoot(T data) {
    GpsCoordinates firstPointOfData = data.getShapeOfData().getFirstPoint();
    GpsCoordinates secondPointOfData = data.getShapeOfData().getSecondPoint();

    GpsCoordinates thisFirstPoint = shape.getFirstPoint();
    GpsCoordinates thisSecondPoint = shape.getSecondPoint();

    return (firstPointOfData.widthCoordinate() >= thisFirstPoint.widthCoordinate()
        && secondPointOfData.widthCoordinate() <= thisSecondPoint.widthCoordinate()
        && firstPointOfData.lengthCoordinate() >= thisFirstPoint.lengthCoordinate()
        && secondPointOfData.lengthCoordinate() <= thisSecondPoint.lengthCoordinate());
  }

  /**
   * Reinsert all items that have height greater than given height and set new height of QuadTree
   *
   * @param newHeight new height for QuadTree
   */
  public void setHeight(int newHeight) {
    if (newHeight <= 0) {
      throw new IllegalArgumentException("New height cannot be <= 0!");
    }

    if (height <= newHeight) {
      height = newHeight;
      return;
    }

    height = newHeight;

    Stack<QuadNode<T>> stackOfNodesToProcess = new Stack<>();
    Stack<T> itemsToReinsert = new Stack<>();

    stackOfNodesToProcess.push(root);

    while (!stackOfNodesToProcess.empty()) {
      QuadNode<T> currentNode = stackOfNodesToProcess.pop();

      if (currentNode.getHeight() > newHeight) {
        itemsToReinsert.addAll(currentNode.getItems());
        currentNode.removeAllItems();
      }

      for (QuadNode<T> child : currentNode.getChildren()) {
        if (child != null) {
          stackOfNodesToProcess.push(child);
        }
      }
    }

    while (!itemsToReinsert.empty()) {
      insert(itemsToReinsert.pop());
    }
  }

  /**
   * Calculates health of quad tree. The smaller the number, the better health does quad tree have.
   * <br>
   * It is calculated as distance between two vectors. First one is ideal quad tree (0, size/4,
   * size/4, size/4, size/4) and second one is current layoult quad tree in shape (itemsInRoot,
   * itemsInNorthWest, itemsInNorthEast, itemsInSouthWest, itemsInSouthEast).
   *
   * @return Number representing health of quad tree. <br>
   *     0 is best scenario.
   */
  public double getHealthOfQuadTree() {
    double[] idealLayoult = {
      0.0, (double) size / 4, (double) size / 4, (double) size / 4, (double) size / 4
    };

    double[] currentLayoult = {
      itemsInRoot, itemsInNorthWest, itemsInNorthEast, itemsInSouthWest, itemsInSouthEast
    };

    double[] result = new double[5];

    for (int i = 0; i < idealLayoult.length; i++) {
      result[i] = Math.pow(idealLayoult[i] - currentLayoult[i], 2);
    }

    return Math.sqrt(Arrays.stream(result).sum());
  }

  /**
   * Returns how
   *
   * @param quadrantOfNode quadrant for which calculate health. Null means health of root.
   * @return Number representing health of quad tree. <br>
   *     0 is best scenario.
   */
  private double getHealthForRootsNode(Quadrant quadrantOfNode) {
    if (quadrantOfNode != null && root.getChild(quadrantOfNode) == null) {
      throw new IllegalStateException("Cannot calculate health for null child of root!");
    }

    if (quadrantOfNode == null) {
      return itemsInRoot;
    }

    double idealLayoult = (double) size / 4;

    double currentLayoult;

    switch (quadrantOfNode) {
      case NORTH_EAST -> currentLayoult = itemsInNorthEast;
      case NORTH_WEST -> currentLayoult = itemsInNorthWest;
      case SOUTH_WEST -> currentLayoult = itemsInSouthWest;
      case SOUTH_EAST -> currentLayoult = itemsInSouthEast;
      default -> throw new IllegalArgumentException(
          String.format("Quadrant %s is not valid quadrant!", quadrantOfNode.name()));
    }

    return Math.abs(idealLayoult - currentLayoult);
  }

  public void optimize() {
    if (size <= 4) {
      return;
    }

    double[] healthOfEachQudrant = new double[5];

    for (Quadrant quadrant : Quadrant.values()) {
      if (root.getChild(quadrant) == null) {
        continue;
      }

      healthOfEachQudrant[quadrant.ordinal()] = getHealthForRootsNode(quadrant);
    }

    healthOfEachQudrant[4] = getHealthForRootsNode(null);

    double max = Double.MIN_VALUE;
    int maxIndex = -1;
    for (int i = 0; i < healthOfEachQudrant.length; i++) {
      if (healthOfEachQudrant[i] > max) {
        max = healthOfEachQudrant[i];
        maxIndex = i;
      }
    }

    // everything is equal
    if (maxIndex == -1 || maxIndex == 4) {
      return;
    }

    Quadrant worstQuadrant;

    worstQuadrant = Quadrant.values()[maxIndex];

    if (last2WorstQuadrants.contains(worstQuadrant)) {
      return;
    }

    addWorstQuadrantToWorstQuadrantHistory(worstQuadrant);

    generateNewQuadTreeBasedOnWorstQuadrant(worstQuadrant);
  }

  private void addWorstQuadrantToWorstQuadrantHistory(Quadrant worstQuadrant) {
    if (last2WorstQuadrants.size() == 2) {
      last2WorstQuadrants.remove(0);
      last2WorstQuadrants.add(worstQuadrant);
    } else {
      last2WorstQuadrants.add(worstQuadrant);
    }
  }

  private void generateNewQuadTreeBasedOnWorstQuadrant(Quadrant worstQuadrant) {

    Rectangle shapeOfWorstQuadrant;
    double xCenterOfWorstQuadrant;
    double yCenterOfWorstQuadrant;
    Rectangle centerOfWorstQuadrant;
    GpsCoordinates firstPointOfNewShape;
    GpsCoordinates seconPointOfNewShape;

    shapeOfWorstQuadrant = root.getChild(worstQuadrant).getShape();

    xCenterOfWorstQuadrant =
        (shapeOfWorstQuadrant.getFirstPoint().widthCoordinate()
                + shapeOfWorstQuadrant.getSecondPoint().widthCoordinate())
            / 2;
    yCenterOfWorstQuadrant =
        (shapeOfWorstQuadrant.getFirstPoint().lengthCoordinate()
                + shapeOfWorstQuadrant.getSecondPoint().lengthCoordinate())
            / 2;

    centerOfWorstQuadrant =
        new Rectangle(
            new GpsCoordinates(
                Direction.S, xCenterOfWorstQuadrant, Direction.W, yCenterOfWorstQuadrant),
            new GpsCoordinates(
                Direction.S, xCenterOfWorstQuadrant, Direction.W, yCenterOfWorstQuadrant));

    firstPointOfNewShape =
        new GpsCoordinates(
            Direction.S,
            (centerOfWorstQuadrant.getFirstPoint().widthCoordinate() - (shape.getWidth())),
            Direction.W,
            (centerOfWorstQuadrant.getFirstPoint().lengthCoordinate() - (shape.getLength())));

    seconPointOfNewShape =
        new GpsCoordinates(
            Direction.N,
            (centerOfWorstQuadrant.getSecondPoint().widthCoordinate()) + (shape.getWidth()),
            Direction.E,
            (centerOfWorstQuadrant.getSecondPoint().lengthCoordinate() + (shape.getLength())));

    Rectangle newShapeOfTree = new Rectangle(firstPointOfNewShape, seconPointOfNewShape);

    QuadTree<T> newQuadTree = new QuadTree<>(height, newShapeOfTree);
    repopulateNewTreeWithItemsFromOldTree(newQuadTree);

    this.shape = newQuadTree.shape;
    this.root = newQuadTree.root;
    this.size = newQuadTree.size;
    this.itemsInNorthWest = newQuadTree.itemsInNorthWest;
    this.itemsInNorthEast = newQuadTree.itemsInNorthEast;
    this.itemsInSouthWest = newQuadTree.itemsInSouthWest;
    this.itemsInSouthEast = newQuadTree.itemsInSouthEast;
    this.itemsInRoot = newQuadTree.itemsInRoot;
  }

  private void repopulateNewTreeWithItemsFromOldTree(QuadTree<T> newQuadTree) {
    Queue<QuadNode<T>> queueOfNodesToProcess = new LinkedList<>();

    queueOfNodesToProcess.offer(root);

    while (!queueOfNodesToProcess.isEmpty()) {
      QuadNode<T> currentNode = queueOfNodesToProcess.poll();

      if (currentNode.getItemsSize() != 0) {
        currentNode.getItems().forEach(newQuadTree::insert);
        currentNode.removeAllItems();
      }

      for (QuadNode<T> child : currentNode.getChildren()) {
        if (child != null) {
          queueOfNodesToProcess.offer(child);
        }
      }
    }
  }

  public int getItemsInNorthWest() {
    return itemsInNorthWest;
  }

  public int getItemsInNorthEast() {
    return itemsInNorthEast;
  }

  public int getItemsInSouthWest() {
    return itemsInSouthWest;
  }

  public int getItemsInSouthEast() {
    return itemsInSouthEast;
  }

  public int getItemsInRoot() {
    return itemsInRoot;
  }
}

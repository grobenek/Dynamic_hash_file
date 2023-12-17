package structure.quadtree;

import entity.SpatialData;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuadNode<T extends IShapeData> {
  public static final int MAX_CHILDREN = 4;
  private final QuadNode<T>[] children;
  private QuadNode<T> parent;
  private int height;
  private final Rectangle shape;
  private final List<T> items;
  private int childrenSize;

  public QuadNode(Rectangle shape) {
    this.children = (QuadNode<T>[]) new QuadNode[MAX_CHILDREN]; // TODO warning
    this.shape = shape;
    this.items = new ArrayList<>();
    this.parent = null;
    childrenSize = 0;
  }

  public QuadNode(T data, Rectangle shape) {
    this.children = (QuadNode<T>[]) new QuadNode[MAX_CHILDREN]; // TODO warning
    this.shape = shape;
    this.items = new ArrayList<>();
    this.parent = null;
    childrenSize = 0;
    items.add(data);
  }

  public int getHeight() {
    return height;
  }

  public QuadNode<T> setHeight(int height) {
    this.height = height;
    return this;
  }

  public QuadNode<T> getParent() {
    return parent;
  }

  public QuadNode<T> setParent(QuadNode<T> parent) {
    this.parent = parent;
    return this;
  }

  public T getItem(int index) {
    return items.get(index);
  }

  public T getItem(T item) {
    int indexOfFirstItem = items.indexOf(item);

    return indexOfFirstItem != -1 ? items.get(indexOfFirstItem) : null;
  }

  public QuadNode<T> addItem(T item) {
    items.add(item);
    return this;
  }

  public int getItemsSize() {
    return items.size();
  }

  public List<T> getItems() {
    return items;
  }

  public Rectangle getShape() {
    return shape;
  }

  public QuadNode<T>[] getChildren() {
    return children;
  }

  public QuadNode<T> getChild(Quadrant quadrant) {
    if (quadrant == null) {
      throw new IllegalArgumentException("Cannot get child, because null is not valid direction!");
    }

    return children[quadrant.ordinal()];
  }

  public void removeChild(Quadrant quadrant) {
    if (quadrant == null) {
      throw new IllegalArgumentException("Cannot remove child from quadrant: null!");
    }

    this.children[quadrant.ordinal()] = null;
  }

  private void addChild(QuadNode<T> child, Quadrant quadrant) {
    checkForExistingChildrenAtDirection(quadrant);

    children[quadrant.ordinal()] = child;
    childrenSize++;
  }

  /**
   * Generate new child for given quadrant and return refference to it
   *
   * @param quadrant quadrant of child
   * @return generated child
   */
  public QuadNode<T> generateChild(Quadrant quadrant) {
    checkForExistingChildrenAtDirection(quadrant);

    addChild(new QuadNode<>(generateChildShape(quadrant)), quadrant);
    getChild(quadrant).setParent(this).setHeight(getHeight() + 1);
    return getChild(quadrant);
  }

  public void generateChildren() {
    int childrenSize = getChildrenSize();
    if (childrenSize != 0) {
      throw new IllegalStateException(
          String.format(
              "Cannot genereate new children, because %d children already exist!", childrenSize));
    }

    for (Quadrant quadrant : Quadrant.values()) {
      this.generateChild(quadrant);
    }
  }

  private void checkForExistingChildrenAtDirection(Quadrant quadrant) {
    if (this.children[quadrant.ordinal()] != null) {
      throw new IllegalStateException(
          String.format(
              "Child at direction %d already exists at [%f, %f], [%f, %f] for node in height %d!",
              quadrant.ordinal(),
              shape.getFirstPoint().widthCoordinate(),
              shape.getFirstPoint().lengthCoordinate(),
              shape.getSecondPoint().widthCoordinate(),
              shape.getSecondPoint().lengthCoordinate(),
              height));
    }
  }

  private Rectangle generateChildShape(Quadrant quadrant) {
    GpsCoordinates bottomLeft = shape.getFirstPoint();
    GpsCoordinates topRight = shape.getSecondPoint();

    double midWidth = shape.getHalfWidth();
    double midLength = shape.getHalfLength();

    switch (quadrant) {
      case NORTH_WEST -> {
        return new Rectangle(
            new GpsCoordinates(Direction.S, bottomLeft.widthCoordinate(), Direction.W, midLength),
            new GpsCoordinates(Direction.N, midWidth, Direction.E, topRight.lengthCoordinate()));
      }
      case NORTH_EAST -> {
        return new Rectangle(
            new GpsCoordinates(Direction.S, midWidth, Direction.W, midLength),
            new GpsCoordinates(
                Direction.N, topRight.widthCoordinate(), Direction.E, topRight.lengthCoordinate()));
      }
      case SOUTH_WEST -> {
        return new Rectangle(
            new GpsCoordinates(
                Direction.S,
                bottomLeft.widthCoordinate(),
                Direction.W,
                bottomLeft.lengthCoordinate()),
            new GpsCoordinates(Direction.N, midWidth, Direction.E, midLength));
      }
      case SOUTH_EAST -> {
        return new Rectangle(
            new GpsCoordinates(Direction.S, midWidth, Direction.W, bottomLeft.lengthCoordinate()),
            new GpsCoordinates(Direction.N, topRight.widthCoordinate(), Direction.E, midLength));
      }
      default -> throw new IllegalArgumentException(
          String.format("Invalid direction %d in generateChildShape", quadrant.ordinal()));
    }
  }

  public int getChildrenSize() {
    return childrenSize;
  }

  /**
   * Checks for shape presence in quadrant. Returns null if it is in multiple quadrants
   *
   * @param data shape to check
   * @return quadrant where shape is in or null if it is in multiple quadrants or equals quadrant
   */
  public Quadrant getQuadrantOfShape(IShapeData data, boolean isDirectChildOfCurrentNode) {
    GpsCoordinates bottomLeftPoint = data.getShapeOfData().getFirstPoint();
    GpsCoordinates topRightPoint = data.getShapeOfData().getSecondPoint();

    if (isDirectChildOfCurrentNode) {
      return getChildsQuadrant(data);
    }

    if (data.getShapeOfData().equals(shape)) {
      return null;
    }

    if (isLeaf() && data.getShapeOfData().isPoint()) {
      if (compareItemsShapeWithNew(data)) {
        return null;
      }
    }

    if (chekForPresenceInNorthWestQuadrant(bottomLeftPoint, topRightPoint)) {
      return Quadrant.NORTH_WEST;
    }
    if (checkForPresenceInNorthEastQuadrant(bottomLeftPoint, topRightPoint)) {
      return Quadrant.NORTH_EAST;
    }
    if (checkForPresenceInSouthWestQuadrant(bottomLeftPoint, topRightPoint)) {
      return Quadrant.SOUTH_WEST;
    }
    if (checkForPresenceInSouthEastQuadrant(bottomLeftPoint, topRightPoint)) {
      return Quadrant.SOUTH_EAST;
    }
    return null;
  }

  private Quadrant getChildsQuadrant(IShapeData childToFind) {
    for (int i = 0; i < children.length; i++) {
      if (children[i] == null) {
        continue;
      }

      if (children[i].getShape().equals(childToFind.getShapeOfData())) {
        return Quadrant.values()[i];
      }
    }
    return null;
  }

  private boolean compareItemsShapeWithNew(IShapeData data) {
    for (IShapeData item : items) {
      if (item.getShapeOfData().equals(data.getShapeOfData())) {
        return true;
      }
    }
    return false;
  }

  private boolean checkForPresenceInSouthEastQuadrant(
      GpsCoordinates bottomLeftPoint, GpsCoordinates topRightPoint) {
    double thisFirstPointLength = shape.getFirstPoint().lengthCoordinate();
    double thisSecondPointWidth = shape.getSecondPoint().widthCoordinate();

    double bottomLeftPointWidth = bottomLeftPoint.widthCoordinate();
    double bottomLeftPointLength = bottomLeftPoint.lengthCoordinate();
    double topRightPointWidth = topRightPoint.widthCoordinate();
    double topRightPointLength = topRightPoint.lengthCoordinate();

    double halfWidth = shape.getHalfWidth();
    double halfLength = shape.getHalfLength();

    return ((bottomLeftPointWidth > halfWidth
        && bottomLeftPointWidth < thisSecondPointWidth
        && (bottomLeftPointLength > thisFirstPointLength && bottomLeftPointLength < halfLength)
        && (topRightPointWidth > halfWidth
            && topRightPointWidth < thisSecondPointWidth
            && (topRightPointLength > thisFirstPointLength && topRightPointLength < halfLength))));
  }

  private boolean checkForPresenceInSouthWestQuadrant(
      GpsCoordinates bottomLeftPoint, GpsCoordinates topRightPoint) {
    double thisFirstPointWidth = shape.getFirstPoint().widthCoordinate();
    double thisFirstPointLength = shape.getFirstPoint().lengthCoordinate();

    double bottomLeftPointWidth = bottomLeftPoint.widthCoordinate();
    double bottomLeftPointLength = bottomLeftPoint.lengthCoordinate();
    double topRightPointWidth = topRightPoint.widthCoordinate();
    double topRightPointLength = topRightPoint.lengthCoordinate();

    double halfWidth = shape.getHalfWidth();
    double halfLength = shape.getHalfLength();

    return ((bottomLeftPointWidth > thisFirstPointWidth
            && bottomLeftPointWidth < halfWidth
            && (bottomLeftPointLength > thisFirstPointLength && bottomLeftPointLength < halfLength))
        && (topRightPointWidth > thisFirstPointWidth
            && topRightPointWidth < shape.getHalfWidth()
            && (topRightPointLength > thisFirstPointLength && topRightPointLength < halfLength)));
  }

  private boolean checkForPresenceInNorthEastQuadrant(
      GpsCoordinates bottomLeftPoint, GpsCoordinates topRightPoint) {
    double thisSecondPointWidth = shape.getSecondPoint().widthCoordinate();
    double thisSecondPointLength = shape.getSecondPoint().lengthCoordinate();

    double bottomLeftPointWidth = bottomLeftPoint.widthCoordinate();
    double bottomLeftPointLength = bottomLeftPoint.lengthCoordinate();
    double topRightPointWidth = topRightPoint.widthCoordinate();
    double topRightPointLength = topRightPoint.lengthCoordinate();

    double halfWidth = shape.getHalfWidth();
    double halfLength = shape.getHalfLength();

    return ((bottomLeftPointWidth > halfWidth && bottomLeftPointWidth < thisSecondPointWidth)
            && (bottomLeftPointLength > halfLength
                && bottomLeftPointLength < thisSecondPointLength))
        && ((topRightPointWidth > halfWidth && topRightPointWidth < thisSecondPointWidth)
            && (topRightPointLength > halfLength && topRightPointLength < thisSecondPointLength));
  }

  private boolean chekForPresenceInNorthWestQuadrant(
      GpsCoordinates bottomLeftPoint, GpsCoordinates topRightPoint) {

    double thisFirstPointWidth = shape.getFirstPoint().widthCoordinate();
    double thisSecondPointLength = shape.getSecondPoint().lengthCoordinate();

    double bottomLeftPointWidth = bottomLeftPoint.widthCoordinate();
    double bottomLeftPointLength = bottomLeftPoint.lengthCoordinate();
    double topRightPointWidth = topRightPoint.widthCoordinate();
    double topRightPointLength = topRightPoint.lengthCoordinate();

    double halfWidth = shape.getHalfWidth();
    double halfLength = shape.getHalfLength();

    return ((bottomLeftPointWidth > thisFirstPointWidth && bottomLeftPointWidth < halfWidth)
            && (bottomLeftPointLength > halfLength
                && bottomLeftPointLength < thisSecondPointLength))
        && ((topRightPointWidth > thisFirstPointWidth && topRightPointWidth < halfWidth)
            && (topRightPointLength > halfLength && topRightPointLength < thisSecondPointLength));
  }

  @Override
  public String toString() {
    return "QuadNode{"
        + "children="
        + Arrays.toString(Arrays.stream(children).map(child -> child == null ? 0 : 1).toArray())
        + ", gpsCoordinates="
        + shape
        + ", items="
        + items
        + '}';
  }

  public void removeItem(T item) {
    items.remove(item);
  }

  public void removeAllItems() {
    items.clear();
  }

  public boolean isLeaf() {
    return childrenSize == 0;
  }

  public void addAllItems(List<T> itemsToAdd) {
    items.addAll(itemsToAdd);
  }

  public boolean doesOverlapWithRectangle(Rectangle otherRectangle) {
    return shape.doesOverlapWithRectangle(otherRectangle);
  }
}

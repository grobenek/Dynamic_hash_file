package mvc.view.entity;

import entity.SpatialDataType;
import entity.shape.Direction;
import entity.shape.GpsCoordinates;
import entity.shape.Rectangle;

public class StructuresParameters {
  private final String pathToMainFile;
  private final int mainFileBlockingFactor;
  private final String pathToOverflowFile;
  private final int overflowFileBlockingFactor;
  private final int quadTreeHeight;
  private final Rectangle quadTreeShape;
  private final SpatialDataType dataType;

  public StructuresParameters(
      String pathToMainFile,
      int mainFileBlockingFactor,
      String pathToOverflowFile,
      int overflowFileBlockingFactor,
      int quadTreeHeight,
      double x1,
      double y1,
      double x2,
      double y2,
      SpatialDataType dataType) {
    this.pathToMainFile = pathToMainFile;
    this.mainFileBlockingFactor = mainFileBlockingFactor;
    this.pathToOverflowFile = pathToOverflowFile;
    this.overflowFileBlockingFactor = overflowFileBlockingFactor;
    this.quadTreeHeight = quadTreeHeight;
    this.dataType = dataType;

    GpsCoordinates firstPoint = new GpsCoordinates(Direction.S, x1, Direction.W, y1);
    GpsCoordinates secondPoint = new GpsCoordinates(Direction.N, x2, Direction.E, y2);

    this.quadTreeShape = new Rectangle(firstPoint, secondPoint);
  }

  public String getPathToMainFile() {
    return pathToMainFile;
  }

  public int getMainFileBlockingFactor() {
    return mainFileBlockingFactor;
  }

  public String getPathToOverflowFile() {
    return pathToOverflowFile;
  }

  public int getOverflowFileBlockingFactor() {
    return overflowFileBlockingFactor;
  }

  public int getQuadTreeHeight() {
    return quadTreeHeight;
  }

  public Rectangle getQuadTreeShape() {
    return quadTreeShape;
  }

  public SpatialDataType getDataType() {
    return dataType;
  }
}

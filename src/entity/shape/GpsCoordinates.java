package entity.shape;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** */
public final class GpsCoordinates {
  private final Direction width;
  private final double widthCoordinate;
  private final Direction length;
  private final double lengthCoordinate;

  /**
   * @param width N or S
   * @param length E or W
   */
  public GpsCoordinates(
      Direction width, double widthCoordinate, Direction length, double lengthCoordinate) {
    this.width = width;
    this.widthCoordinate =
        BigDecimal.valueOf(widthCoordinate).setScale(2, RoundingMode.HALF_UP).doubleValue();
    this.length = length;
    this.lengthCoordinate =
        BigDecimal.valueOf(lengthCoordinate).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GpsCoordinates)) {
      return false;
    }

    GpsCoordinates casted = (GpsCoordinates) obj;
    return ((casted.widthCoordinate == widthCoordinate)
        && (casted.lengthCoordinate == lengthCoordinate)
        && (casted.width.getDirection() == width.getDirection())
        && (casted.length.getDirection() == length.getDirection()));
  }

  @Override
  public String toString() {
    return "GpsCoordinates{"
        + "width="
        + width
        + ", widthCoordinate="
        + widthCoordinate
        + ", length="
        + length
        + ", lengthCoordinate="
        + lengthCoordinate
        + '}';
  }

  public Direction width() {
    return width;
  }

  public double widthCoordinate() {
    return widthCoordinate;
  }

  public Direction length() {
    return length;
  }

  public double lengthCoordinate() {
    return lengthCoordinate;
  }
}

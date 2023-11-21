package entity.shape;

/** */
public record GpsCoordinates(
    Direction width, double widthCoordinate, Direction length, double lengthCoordinate) {
  /**
   * @param width N or S
   * @param length E or W
   */
  public GpsCoordinates {}

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
}

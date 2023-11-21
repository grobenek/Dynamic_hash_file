package entity.shape;

public enum Direction {
  N('N'),
  S('S'),
  W('W'),
  E('E');

  private final char direction;

  Direction(char direction) {
    this.direction = direction;
  }

  public char getDirection() {
    return direction;
  }
}

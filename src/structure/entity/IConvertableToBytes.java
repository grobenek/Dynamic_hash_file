package structure.entity;

public interface IConvertableToBytes {
  byte[] toByteArray();

  void fromByteArray(byte[] byteArray);
}

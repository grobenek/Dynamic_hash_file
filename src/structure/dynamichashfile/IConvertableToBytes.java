package structure.dynamichashfile;

public interface IConvertableToBytes {
  byte[] toByteArray();

  void fromByteArray(byte[] byteArray);
}

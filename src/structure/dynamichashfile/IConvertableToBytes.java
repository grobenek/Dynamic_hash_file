package structure.dynamichashfile;

public interface IConvertableToBytes<T> {
  byte[] toByteArray();

  T fromByteArray(byte[] byteArray);
}

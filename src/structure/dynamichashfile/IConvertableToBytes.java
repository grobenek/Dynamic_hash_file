package structure.dynamichashfile;

public interface IConvertableToBytes {
    byte[] toByteArray();
    Record fromByteArray();
}

package structure.dynamichashfile;

import entity.SpatialData;
import entity.SpatialDataFactory;

public class Record<T extends SpatialData<?>> implements IConvertableToBytes {
  private T data;

  public Record(T data) {
    this.data = data;
  }

  @Override
  public byte[] toByteArray() {
    return data.toByteArray();
  }

  @Override
  public void fromByteArray(byte[] byteArray) {
    data = (T) SpatialDataFactory.fromByteArray(byteArray);
  }
}

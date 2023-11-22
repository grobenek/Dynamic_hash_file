package structure.dynamichashfile;

import entity.SpatialData;
import entity.SpatialDataFactory;

public class Record<T extends SpatialData> implements IConvertableToBytes<Record<T>> {
    private T data;

    public Record(T data) {
        this.data = data;
    }

    @Override
    public byte[] toByteArray() {
        return data.toByteArray();
    }

    @Override
    public Record<T> fromByteArray(byte[] byteArray) {
        T data = (T) SpatialDataFactory.fromByteArray(byteArray, true); // TODO warning
        return new Record<>(data);
    }
}

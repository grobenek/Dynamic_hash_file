package structure.dynamichashfile;

import entity.SpatialDataFactory;
import java.io.*;
import java.util.Arrays;
import structure.dynamichashfile.constant.ElementByteSize;

public class Block<T extends Record> implements IConvertableToBytes {
  private final T tDummyInstance;
  private Record[] records;
  private int blockingFactor;
  private int validRecordsCount;

  public Block(int blockingFactor, Class<T> tClass) {
    this.blockingFactor = blockingFactor;
    this.validRecordsCount = 0;
    this.records = new Record[blockingFactor];
    this.tDummyInstance = RecordFactory.createDummyInstance(tClass);
    Arrays.fill(records, tDummyInstance);
  }

  //  public Block() {
  //    this.blockingFactor = 0;
  //    this.records = new Record[0];
  //    this.validRecordsCount = 0;
  //  }

  public int getBlockingFactor() {
    return blockingFactor;
  }

  public int getByteSize() {
    return tDummyInstance.getByteSize() * blockingFactor;
  }

  public Record[] getValidRecords() {
    return Arrays.copyOfRange(records, 0, validRecordsCount);
  }

  public int getValidRecordsCount() {
    return validRecordsCount;
  }

  public boolean hasFreeSpace() {
    return blockingFactor - validRecordsCount > 0;
  }

  public Record getRecord(Record pRecord) {
    if (pRecord == null) {
      throw new IllegalArgumentException("Cannot search null Record!");
    }

    for (Record record : records) {
      if (record.equals(pRecord)) {
        return record;
      }
    }
    throw new IllegalArgumentException(String.format("Record %s was not found!", pRecord));
  }

  public void addRecord(Record record) {
    if (record == null) {
      throw new IllegalArgumentException("Cannot add null Record!");
    }

    if (validRecordsCount >= blockingFactor) {
      throw new IllegalStateException(
          String.format("Cannot add new Record! Blocking factor of %d exceeded!", blockingFactor));
    }

    validRecordsCount++;
    records[validRecordsCount - 1] = record;
  }

  public void removeRecord(Record pRecord) {
    if (validRecordsCount == 0) {
      throw new IllegalStateException("Cannot delete Record from empty block!");
    }

    if (pRecord == null) {
      throw new IllegalArgumentException("Cannot remove null Record!");
    }

    int lastValidRecordIndex = validRecordsCount - 1;
    for (int i = 0; i < records.length; i++) {
      if (records[i].equals(pRecord)) {
        removeRecord(i, lastValidRecordIndex);
        return;
      }
    }
    throw new IllegalStateException(String.format("Record %s was not found!", pRecord));
  }

  private void removeRecord(int i, int lastValidRecordIndex) {
    if (i != lastValidRecordIndex) {
      // swap with last valid record and decrease count
      swapRecords(i, lastValidRecordIndex);
    }
    validRecordsCount--;
  }

  private void swapRecords(int indexOfFirstItem, int indexOfSecondItem) {
    Record recordToSwap = records[indexOfFirstItem];
    records[indexOfFirstItem] = records[indexOfSecondItem];
    records[indexOfSecondItem] = recordToSwap;
  }

  public void clear() {
    Arrays.fill(records, tDummyInstance);
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(blockingFactor);
      outputStream.writeInt(validRecordsCount);

      int byteSizeOfOneRecord = tDummyInstance.getByteSize();
      byte[] result = new byte[byteSizeOfOneRecord * blockingFactor];
      for (int i = 0; i < records.length; i++) {
        byte[] recordBytes = records[i].toByteArray();
        System.arraycopy(recordBytes, 0, result, (i) * byteSizeOfOneRecord, byteSizeOfOneRecord);
      }

      outputStream.write(result);

      return byteArrayOutputStream.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  @Override
  public void fromByteArray(byte[] byteArray) {

    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {
      blockingFactor = inputStream.readInt();
      validRecordsCount = inputStream.readInt();

      int byteSizeOfOneRecord = tDummyInstance.getByteSize();
      int numberOfRecordsInByteArray = byteArray.length / byteSizeOfOneRecord;
      records = new Record[numberOfRecordsInByteArray];

      if (numberOfRecordsInByteArray > blockingFactor) {
        throw new IllegalStateException(
            String.format(
                "Byte array contains more records that Block can handle. %d records for %d blocking factor!",
                numberOfRecordsInByteArray, blockingFactor));
      }

      int counter = 0;
      for (int i = 0;
          i < numberOfRecordsInByteArray * byteSizeOfOneRecord;
          i += byteSizeOfOneRecord) {
        Record record =
            SpatialDataFactory.fromByteArray(
                Arrays.copyOfRange(
                    byteArray,
                    (ElementByteSize.intByteSize() * 2) + i,
                    ((ElementByteSize.intByteSize() * 2) + i) + byteSizeOfOneRecord));
        records[counter] = record;
        counter++;
      }

    } catch (IOException e) {
      throw new RuntimeException("Error during conversion from byte array.", e);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Block - blocking factor: ")
        .append(blockingFactor)
        .append(" valid records: ")
        .append(validRecordsCount)
        .append("\n")
        .append("Records: ");
    for (int i = 0; i < validRecordsCount; i++) {
      sb.append(records[i].toString()).append("\n");
    }

    return sb.toString();
  }
}

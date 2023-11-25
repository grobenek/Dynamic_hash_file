package structure.dynamichashfile;

import entity.SpatialDataFactory;
import java.io.*;
import java.util.Arrays;
import structure.dynamichashfile.constant.ElementByteSize;

public class Block implements IConvertableToBytes {
  private Record[] records;
  private int blockingFactor;
  private int size;
  private int validRecords;

  public Block(int blockingFactor) {
    this.blockingFactor = blockingFactor;
    this.size = 0;
    this.validRecords = 0;
    this.records = new Record[blockingFactor];
    Arrays.fill(records, Record.getDummyInstance());
  }

  public Block() {
    this.blockingFactor = 0;
    this.size = 0;
    this.records = new Record[0];
    this.validRecords = 0;
  }

  public int getBlockingFactor() {
    return blockingFactor;
  }

  public int getSize() {
    return size;
  }

  public int getValidRecords() {
    return validRecords;
  }

  public void addRecord(Record record) {
    if (size >= blockingFactor) {
      throw new IllegalStateException(
          String.format("Cannot add new Record! Blocking factor of %d exceeded!", blockingFactor));
    }

    validRecords++;
    records[validRecords - 1] = record;
  }

  private void swapRecords(int indexOfFirstItem, int indexOfSecondItem) {
    Record recordToSwap = records[indexOfFirstItem];
    records[indexOfFirstItem] = records[indexOfSecondItem];
    records[indexOfSecondItem] = recordToSwap;
  }

  private void initializeInvalidRecords() {
    Arrays.fill(records, Record.getDummyInstance());
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(blockingFactor);
      outputStream.writeInt(validRecords);

      int byteSizeOfOneRecord = Record.getByteSize();
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
      validRecords = inputStream.readInt();

      int byteSizeOfOneRecord = Record.getByteSize();
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
        .append(validRecords)
        .append("\n")
        .append("Records: ");
    for (Record record : records) {
      sb.append(record.toString()).append("\n");
    }

    return sb.toString();
  }
}

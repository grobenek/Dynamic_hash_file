package structure.dynamichashfile.entity;

import entity.RecordDataFactory;
import java.io.*;
import java.util.Arrays;
import structure.dynamichashfile.constant.ElementByteSize;
import structure.dynamichashfile.entity.record.Record;
import structure.dynamichashfile.entity.record.RecordFactory;

public class Block<T extends Record> implements IConvertableToBytes {
  private static final int INVALID_ADDRESS = -1;
  private final T tDummyInstance;
  private final int blockingFactor;
  private Record[] records;
  private int validRecordsCount;
  private long addressOfOverflowBlock;
  private long nextOverflowBlockAddress;
  private long previousOverflowBlockAddress;
  private long previousFreeBlockAddress;
  private long nextFreeBlockAddress;

  public Block(int blockingFactor, Class<T> tClass) {
    this.blockingFactor = blockingFactor;
    this.validRecordsCount = 0;
    this.records = new Record[blockingFactor];
    this.tDummyInstance = RecordFactory.getDummyInstance(tClass);
    Arrays.fill(records, tDummyInstance);

    this.addressOfOverflowBlock = INVALID_ADDRESS;
    this.nextOverflowBlockAddress = INVALID_ADDRESS;
    this.previousOverflowBlockAddress = INVALID_ADDRESS;

    this.previousFreeBlockAddress = INVALID_ADDRESS;
    this.nextFreeBlockAddress = INVALID_ADDRESS;
  }

  public static int getInvalidAddress() {
    return INVALID_ADDRESS;
  }

  public long getNextOverflowBlockAddress() {
    return nextOverflowBlockAddress;
  }

  public Block<T> setNextOverflowBlockAddress(long nextOverflowBlockAddress) {
    this.nextOverflowBlockAddress = nextOverflowBlockAddress;
    return this;
  }

  public long getPreviousOverflowBlockAddress() {
    return previousOverflowBlockAddress;
  }

  public Block<T> setPreviousOverflowBlockAddress(long previousOverflowBlockAddress) {
    this.previousOverflowBlockAddress = previousOverflowBlockAddress;
    return this;
  }

  public int getBlockingFactor() {
    return blockingFactor;
  }

  public int getByteSize() {
    return tDummyInstance.getByteSize() * blockingFactor
        + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5));
  }

  public Record[] getValidRecords() {
    return Arrays.copyOfRange(records, 0, validRecordsCount);
  }

  public int getValidRecordsCount() {
    return validRecordsCount;
  }

  public long getAddressOfOverflowBlock() {
    return addressOfOverflowBlock;
  }

  public Block<T> setAddressOfOverflowBlock(long addressOfOverflowBlock) {
    this.addressOfOverflowBlock = addressOfOverflowBlock;
    return this;
  }

  public void setPreviousFreeBlockAddress(long address) {
    previousFreeBlockAddress = address;
  }

  public long getNextFreeBlockAddress() {
    return nextFreeBlockAddress;
  }

  public void setNextFreeBlockAddress(long address) {
    nextFreeBlockAddress = address;
  }

  public boolean hasFreeSpace() {
    return blockingFactor - validRecordsCount > 0;
  }

  public Record getRecord(Record pRecord) {
    if (pRecord == null) {
      throw new IllegalArgumentException("Cannot search null Record!");
    }

    for (int i = 0; i < validRecordsCount; i++) {
      Record record = records[i];
      if (record.equals(pRecord)) {
        return record;
      }
    }
    return null;
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
    validRecordsCount = 0;
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(validRecordsCount);
      outputStream.writeLong(addressOfOverflowBlock);
      outputStream.writeLong(previousFreeBlockAddress);
      outputStream.writeLong(nextFreeBlockAddress);
      outputStream.writeLong(nextOverflowBlockAddress);
      outputStream.writeLong(previousOverflowBlockAddress);

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

      validRecordsCount = inputStream.readInt();
      addressOfOverflowBlock = inputStream.readLong();
      previousFreeBlockAddress = inputStream.readLong();
      nextFreeBlockAddress = inputStream.readLong();
      nextOverflowBlockAddress = inputStream.readLong();
      previousOverflowBlockAddress = inputStream.readLong();

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
            RecordDataFactory.fromByteArray(
                Arrays.copyOfRange(
                    byteArray,
                    (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5)) + i,
                    ((ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5)) + i)
                        + byteSizeOfOneRecord));
        records[counter] = record;
        counter++;
      }

    } catch (IOException e) {
      throw new RuntimeException("Error during conversion from byte array.", e);
    }
  }

  public boolean isEmpty() {
    return validRecordsCount == 0;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Block - blocking factor: ")
        .append(blockingFactor)
        .append("\n Address of overflowBlock: ")
        .append(addressOfOverflowBlock)
        .append("\n Address of next overflowBlock: ")
        .append(nextOverflowBlockAddress)
        .append("\n Address of previous overflowBlock: ")
        .append(previousOverflowBlockAddress)
        .append("\n Address of next free block: ")
        .append(nextFreeBlockAddress)
        .append("\n Address of previous free block: ")
        .append(previousFreeBlockAddress)
        .append("\n valid records: ")
        .append(validRecordsCount)
        .append("\n")
        .append("Records: [\n");
    for (int i = 0; i < validRecordsCount; i++) {
      sb.append("\t").append(records[i].toString()).append("\n");
    }
    sb.append("];\n");

    return sb.toString();
  }
}

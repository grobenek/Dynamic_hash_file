package structure.dynamichashfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import structure.dynamichashfile.constant.ElementByteSize;
import structure.dynamichashfile.trie.LeafTrieNode;
import structure.entity.Block;
import structure.entity.record.Record;
import structure.entity.record.RecordFactory;

public class FileBlockManager<T extends Record> implements AutoCloseable {
  private static final int INVALID_ADDRESS = Block.getInvalidAddress();
  private final RandomAccessFile mainFileStream;
  private final RandomAccessFile overflowFileStream;
  private final int mainFileBlockingFactor;
  private final int overflowFileBlockingFactor;
  private final Class<T> tClass;
  private final T tDummyInstance;
  private final File mainFile;
  private final File overflowFile; // TODO inicializovat
  private long firstFreeBlockAddressFromMainFile;
  private long firstFreeBlockAddressFromOverflowFile;

  public FileBlockManager(
      String mainFilePath,
      int mainBlockingFactor,
      String overflowFilePath,
      int overflowBlockingFactor,
      Class<T> tClass)
      throws IOException {
    this.mainFile = new File(mainFilePath);
    this.overflowFile = new File(overflowFilePath);

    this.mainFileStream = new RandomAccessFile(this.mainFile, "rw");
    this.overflowFileStream = new RandomAccessFile(this.overflowFile, "rw");
    this.mainFileBlockingFactor = mainBlockingFactor;
    this.overflowFileBlockingFactor = overflowBlockingFactor;
    this.firstFreeBlockAddressFromOverflowFile = INVALID_ADDRESS;
    this.firstFreeBlockAddressFromMainFile = INVALID_ADDRESS;

    this.tClass = tClass;
    this.tDummyInstance = RecordFactory.getDummyInstance(tClass);

    resetFile(mainFile);
    resetFile(overflowFile);
  }

  private void resetFile(File file) {
    try {
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(
          "Error occured when resetting file! Message: " + e.getLocalizedMessage());
    }
  }

  public Class<T> getTClass() {
    return tClass;
  }

  public int getMainFileBlockingFactor() {
    return mainFileBlockingFactor;
  }

  public int getOverflowFileBlockingFactor() {
    return overflowFileBlockingFactor;
  }

  public Block<T> getMainBlock(long address) {
    try {
      mainFileStream.seek(address);
      Block<T> block = new Block<>(mainFileBlockingFactor, tClass);
      byte[] blockBytes = new byte[block.getByteSize()];
      mainFileStream.read(blockBytes);
      block.fromByteArray(blockBytes);

      return block;
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to read block from address %d. Error message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  public void writeMainBlock(Block<T> block, long address) {
    try {
      mainFileStream.seek(address);
      mainFileStream.write(block.toByteArray());

    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to write block to address %d. Error message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  public void deleteMainBlock(LeafTrieNode nodeOfData) throws IOException {
    long addressOfData = nodeOfData.getAddressOfData();
    Block<T> blockToDelete = getMainBlock(addressOfData);

    if (isMainBlockOnTheEndOfFile(addressOfData, blockToDelete)) {
      // block is on the end of a file - set new length of file
      mainFileStream.setLength(addressOfData);
    } else {
      // block is in the middle - clear it and put it in free blocks
      nodeOfData.removeDataInMainBlock(nodeOfData.getDataSizeInMainBlock());
      blockToDelete.clear();
      writeMainBlock(blockToDelete, addressOfData);

      setBlockAsFirstFreeBlock(addressOfData, blockToDelete);
    }
  }

  private boolean isMainBlockOnTheEndOfFile(long address, Block<T> blockToCheck)
      throws IOException {
    return address + blockToCheck.getByteSize() == mainFileStream.length();
  }

  public String sequenceToString() throws IOException {
    StringBuilder sb = new StringBuilder();
    //        Stack<TrieNode> stack = new Stack<>();
    //
    //        stack.push(trie.root);
    //
    //        while (!stack.isEmpty()) {
    //          TrieNode currentNode = stack.pop();
    //
    //          if (currentNode instanceof LeafTrieNode) {
    //            sb.append(getBlock(((LeafTrieNode) currentNode).getAddressOfData()));
    //          }
    //
    //          if (currentNode instanceof InnerTrieNode) {
    //            TrieNode leftSon = ((InnerTrieNode) currentNode).getLeftSon();
    //            TrieNode rightSon = ((InnerTrieNode) currentNode).getRightSon();
    //
    //            if (leftSon != null) {
    //              stack.push(leftSon);
    //            }
    //
    //            if (rightSon != null) {
    //              stack.push(rightSon);
    //            }
    //          }
    //        }

    for (long i = 0, fileLength = mainFileStream.length();
        i < fileLength;
        i +=
            ((long) tDummyInstance.getByteSize() * mainFileBlockingFactor
                + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 3L)))) {
      Block<T> block = getMainBlock(i);

      sb.append(i).append(" ").append(block);
    }

    return sb.toString();
  }

  private void setBlockAsFirstFreeBlock(long addressOfData, Block<T> blockToDelete) {
    if (firstFreeBlockAddressFromMainFile == INVALID_ADDRESS) {
      firstFreeBlockAddressFromMainFile = addressOfData;
      return;
    }

    Block<T> firstFreeBlock = getMainBlock(firstFreeBlockAddressFromMainFile);
    firstFreeBlock.setPreviousFreeBlockAddress(addressOfData);
    blockToDelete.setNextFreeBlockAddress(firstFreeBlockAddressFromMainFile);
    firstFreeBlockAddressFromMainFile = addressOfData;

    writeMainBlock(blockToDelete, addressOfData);
  }

  public void createMainBlock(long address) {
    try {
      mainFileStream.seek(address);
      mainFileStream.write(new Block<>(mainFileBlockingFactor, tClass).toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to create new Block in address %d. Message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  public long getNewMainBlockAddress() {
    try {
      if (firstFreeBlockAddressFromMainFile == INVALID_ADDRESS) {
        long fileLength = mainFileStream.length();
        mainFileStream.setLength(
            fileLength
                + ((long) tDummyInstance.getByteSize() * mainFileBlockingFactor
                    + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 3L))));

        return fileLength;
      } else {
        return getAddressFromFreeBlocks();
      }
    } catch (IOException e) {
      throw new RuntimeException("New block cannot been created! Message: " + e);
    }
  }

  private long getAddressFromFreeBlocks() throws IOException {
    long freeBlockAddress = firstFreeBlockAddressFromMainFile;
    Block<T> freeBlock = getMainBlock(freeBlockAddress);
    long nextFreeBlockAddress = freeBlock.getNextFreeBlockAddress();

    if (nextFreeBlockAddress == INVALID_ADDRESS) {
      freeBlock.setNextFreeBlockAddress(INVALID_ADDRESS);
      writeMainBlock(freeBlock, freeBlockAddress);
      firstFreeBlockAddressFromMainFile = INVALID_ADDRESS;

      return freeBlockAddress;
    }

    Block<T> secondFreeBlock = getMainBlock(nextFreeBlockAddress);

    freeBlock.setNextFreeBlockAddress(INVALID_ADDRESS);
    secondFreeBlock.setPreviousFreeBlockAddress(INVALID_ADDRESS);

    writeMainBlock(freeBlock, freeBlockAddress);
    writeMainBlock(secondFreeBlock, nextFreeBlockAddress);

    firstFreeBlockAddressFromMainFile = nextFreeBlockAddress;
    return freeBlockAddress;
  }

  // New methods to handle the overflow file (similar to main file methods)
  public Block<T> getOverflowBlock(long address) throws IOException {
    // Similar logic to getBlock, but for the overflow file
    throw new UnsupportedOperationException();
  }

  public void writeOverflowBlock(Block<T> block, long address) throws IOException {
    // Similar logic to writeBlock, but for the overflow file
  }

  public long allocateNewOverflowBlock() throws IOException {
    // Logic to allocate a new block in the overflow file
    // This might include handling firstFreeBlockAddressInOverflowFile
    // and expanding the file size as needed
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    mainFileStream.close();
    overflowFileStream.close();
  }
}

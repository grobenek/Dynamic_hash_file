package structure.dynamichashfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import structure.dynamichashfile.constant.ElementByteSize;
import structure.dynamichashfile.trie.LeafTrieNode;
import structure.dynamichashfile.entity.Block;
import structure.dynamichashfile.entity.record.Record;
import structure.dynamichashfile.entity.record.RecordFactory;

class FileBlockManager<T extends Record> implements AutoCloseable {
  private static final int INVALID_ADDRESS = Block.getInvalidAddress();
  private final RandomAccessFile mainFileStream;
  private final RandomAccessFile overflowFileStream;
  private final int mainFileBlockingFactor;
  private final int overflowFileBlockingFactor;
  private final Class<T> tClass;
  private final T tDummyInstance;
  private long firstFreeBlockAddressFromMainFile;
  private long firstFreeBlockAddressFromOverflowFile;

  public FileBlockManager(
      String mainFilePath,
      int mainBlockingFactor,
      String overflowFilePath,
      int overflowBlockingFactor,
      Class<T> tClass)
      throws IOException {
    File mainFile = new File(mainFilePath);
    File overflowFile = new File(overflowFilePath);

    resetFile(mainFile);
    resetFile(overflowFile);

    this.mainFileStream = new RandomAccessFile(mainFile, "rw");
    this.overflowFileStream = new RandomAccessFile(overflowFile, "rw");
    this.mainFileBlockingFactor = mainBlockingFactor;
    this.overflowFileBlockingFactor = overflowBlockingFactor;
    this.firstFreeBlockAddressFromOverflowFile = INVALID_ADDRESS;
    this.firstFreeBlockAddressFromMainFile = INVALID_ADDRESS;

    this.tClass = tClass;
    this.tDummyInstance = RecordFactory.getDummyInstance(tClass);
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

  public void deleteMainBlock(LeafTrieNode nodeOfBlockToDelete) throws IOException {
    long addressOfData = nodeOfBlockToDelete.getAddressOfData();
    Block<T> blockToDelete = getMainBlock(addressOfData);

    if (isMainBlockOnTheEndOfFile(addressOfData, blockToDelete)) {
      // block is on the end of a file - set new length of file
      mainFileStream.setLength(addressOfData);
      //      mainFileStream.setLength( //TODO toto spravit, ale asi niekde v trie, lebo ked to
      // vymazem a trie o tom nevie, tak pristupujem k datam mimo file - ale mozno to staci takto
      //              getAddressLastEmptyBlockFromEndOfFile(addressOfData, blockToDelete, true));
    } else {
      // block is in the middle - clear it and put it in free blocks
      nodeOfBlockToDelete.removeDataInMainBlock(nodeOfBlockToDelete.getDataSizeInOverflowBlock());
      blockToDelete.clear();
      writeMainBlock(blockToDelete, addressOfData);

      setMainBlockAsFirstFreeBlock(addressOfData, blockToDelete);
    }
  }

  private long getAddressLastEmptyBlockFromEndOfFile( // TODO toto hore
      long addressOfPCurrentBlock, Block<T> pCurrentBlock, boolean isInMainFile) {
    long addressOfCurrentBlock = addressOfPCurrentBlock;
    Block<T> currentBlock = pCurrentBlock;

    while (currentBlock.getAddressOfOverflowBlock() == INVALID_ADDRESS && currentBlock.isEmpty()) {
      addressOfCurrentBlock =
          addressOfCurrentBlock - (currentBlock.getByteSize()) < 0
              ? 0
              : addressOfCurrentBlock - ((currentBlock.getByteSize()));
      currentBlock =
          isInMainFile
              ? getMainBlock(addressOfCurrentBlock)
              : getOverflowBlock(addressOfCurrentBlock);
    }

    return addressOfCurrentBlock;
  }

  private boolean isMainBlockOnTheEndOfFile(long address, Block<T> blockToCheck)
      throws IOException {
    return address + blockToCheck.getByteSize() == mainFileStream.length();
  }

  public String sequenceToStringMainFile() throws IOException {
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

    sb.append("Main file:\n");
    for (long i = 0, fileLength = mainFileStream.length();
        i < fileLength;
        i +=
            ((long) tDummyInstance.getByteSize() * mainFileBlockingFactor
                + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5L)))) {
      Block<T> block = getMainBlock(i);

      sb.append("---------------------")
          .append("\n")
          .append(i)
          .append(" ")
          .append(block)
          .append("\n");
    }

    return sb.toString();
  }

  private void setMainBlockAsFirstFreeBlock(long addressOfData, Block<T> blockToDelete) {
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
                    + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5L))));

        return fileLength;
      } else {
        return getAddressFromFreeMainBlocks();
      }
    } catch (IOException e) {
      throw new RuntimeException("New block cannot been created! Message: " + e);
    }
  }

  private long getAddressFromFreeMainBlocks() throws IOException {
    long freeBlockAddress = firstFreeBlockAddressFromMainFile;
    Block<T> freeBlock = getMainBlock(freeBlockAddress);
    long nextFreeBlockAddress = freeBlock.getNextFreeBlockAddress();

    if (nextFreeBlockAddress == INVALID_ADDRESS) {
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
  public Block<T> getOverflowBlock(long address) {
    // Similar logic to getBlock, but for the overflow file
    try {
      overflowFileStream.seek(address);
      Block<T> block = new Block<>(overflowFileBlockingFactor, tClass);
      byte[] blockBytes = new byte[block.getByteSize()];
      overflowFileStream.read(blockBytes);
      block.fromByteArray(blockBytes);

      return block;
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to read overflow block from address %d. Error message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  public void writeOverflowBlock(Block<T> block, long address) {
    try {
      overflowFileStream.seek(address);
      overflowFileStream.write(block.toByteArray());

    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to write overflow block to address %d. Error message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  public void deleteOverflowBlock(LeafTrieNode nodeOfData) throws IOException {
    long addressOfData = nodeOfData.getAddressOfData();
    Block<T> blockToDelete = getOverflowBlock(addressOfData);

    if (isOverflowBlockOnTheEndOfFile(addressOfData, blockToDelete)) {
      // block is on the end of a file - set new length of file
      overflowFileStream.setLength(
          addressOfData); // TODO treba pozriet aj predchodcu a zmazat ak tak
    } else {
      // block is in the middle - clear it and put it in free blocks
      nodeOfData.removeDataInReserveBlock(nodeOfData.getDataSizeInOverflowBlock());
      blockToDelete.clear();
      writeOverflowBlock(blockToDelete, addressOfData);

      setOverflowBlockAsFirstFreeBlock(addressOfData, blockToDelete);
    }
  }

  public Block<T> createOverflowBlock(long address) {
    try {
      overflowFileStream.seek(address);
      Block<T> newBlock = new Block<>(overflowFileBlockingFactor, tClass);
      overflowFileStream.write(newBlock.toByteArray());

      return newBlock;
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to create new Block in address %d. Message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  private void setOverflowBlockAsFirstFreeBlock(long addressOfData, Block<T> blockToDelete) {
    if (firstFreeBlockAddressFromOverflowFile == INVALID_ADDRESS) {
      firstFreeBlockAddressFromOverflowFile = addressOfData;
      return;
    }

    Block<T> firstFreeBlock = getOverflowBlock(firstFreeBlockAddressFromOverflowFile);
    firstFreeBlock.setPreviousFreeBlockAddress(addressOfData);
    blockToDelete.setNextFreeBlockAddress(firstFreeBlockAddressFromOverflowFile);
    firstFreeBlockAddressFromOverflowFile = addressOfData;

    writeOverflowBlock(blockToDelete, addressOfData);
  }

  private boolean isOverflowBlockOnTheEndOfFile(long address, Block<T> blockToCheck)
      throws IOException {
    return address + blockToCheck.getByteSize() == overflowFileStream.length();
  }

  public long getNewOverflowBlockAddress() {
    try {
      if (firstFreeBlockAddressFromOverflowFile == INVALID_ADDRESS) {
        long fileLength = overflowFileStream.length();
        overflowFileStream.setLength(
            fileLength
                + ((long) tDummyInstance.getByteSize() * overflowFileBlockingFactor
                    + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5L))));

        return fileLength;
      } else {
        return getAddressFromFreeOverflowBlocks();
      }
    } catch (IOException e) {
      throw new RuntimeException("New overflow block cannot been created! Message: " + e);
    }
  }

  private long getAddressFromFreeOverflowBlocks() throws IOException {
    long freeBlockAddress = firstFreeBlockAddressFromOverflowFile;
    Block<T> freeBlock = getOverflowBlock(freeBlockAddress);
    long nextFreeBlockAddress = freeBlock.getNextFreeBlockAddress();

    if (nextFreeBlockAddress == INVALID_ADDRESS) {
      freeBlock.setNextFreeBlockAddress(INVALID_ADDRESS);
      writeOverflowBlock(freeBlock, freeBlockAddress);
      firstFreeBlockAddressFromOverflowFile = INVALID_ADDRESS;

      return freeBlockAddress;
    }

    Block<T> secondFreeBlock = getOverflowBlock(nextFreeBlockAddress);

    freeBlock.setNextFreeBlockAddress(INVALID_ADDRESS);
    secondFreeBlock.setPreviousFreeBlockAddress(INVALID_ADDRESS);

    writeOverflowBlock(freeBlock, freeBlockAddress);
    writeOverflowBlock(secondFreeBlock, nextFreeBlockAddress);

    firstFreeBlockAddressFromOverflowFile = nextFreeBlockAddress;
    return freeBlockAddress;
  }

  public String sequenceToStringOverflowFile() throws IOException {
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
    sb.append("Overflow file:\n");

    for (long i = 0, fileLength = overflowFileStream.length();
        i < fileLength;
        i +=
            ((long) tDummyInstance.getByteSize() * overflowFileBlockingFactor
                + (ElementByteSize.intByteSize() + (ElementByteSize.longByteSize() * 5L)))) {
      Block<T> block = getOverflowBlock(i);

      sb.append("---------------------------------------")
          .append("\n")
          .append(i)
          .append(" ")
          .append(block)
          .append("\n");
    }

    return sb.toString();
  }

  @Override
  public void close() throws IOException {
    System.out.println("MANAGER: CLOSING FILES");
    mainFileStream.close();
    overflowFileStream.close();
  }
}

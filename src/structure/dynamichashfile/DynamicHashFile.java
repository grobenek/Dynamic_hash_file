package structure.dynamichashfile;

import java.io.*;
import java.util.BitSet;
import structure.dynamichashfile.constant.ElementByteSize;
import structure.dynamichashfile.trie.*;

public class DynamicHashFile<T extends Record> implements AutoCloseable {
  private static final int INVALID_ADDRESS = Block.getInvalidAddress();
  private final String pathToMainFile;
  private final String pathToOverflowFile;
  private final File mainFile;
  private final File overflowFile;
  private final RandomAccessFile mainFileStream;
  private final RandomAccessFile overflowFileStream;
  private final int mainFileBlockingFactor;
  private final int overflowFileBlockingFactor;
  private final Trie trie;
  private final Class<T> tClass;
  private final T tDummyInstance;
  private long firstFreeBlockAddressFromMainFile;
  private long firstFreeBlockAddressFromOverflowFile;

  public DynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int blockingFactorOfMainFile,
      int blockingFactorOfOverflowFile,
      int maxDepth,
      Class<T> tClass)
      throws FileNotFoundException {
    this.pathToMainFile = pathToMainFile;
    this.mainFile = new File(pathToMainFile);
    this.mainFileBlockingFactor = blockingFactorOfMainFile;

    this.pathToOverflowFile = pathToOverflowFile;
    this.overflowFile = new File(pathToOverflowFile);
    this.overflowFileBlockingFactor = blockingFactorOfOverflowFile;

    this.firstFreeBlockAddressFromMainFile = INVALID_ADDRESS;
    this.firstFreeBlockAddressFromOverflowFile = INVALID_ADDRESS;

    this.tClass = tClass;
    this.tDummyInstance = RecordFactory.getDummyInstance(tClass);

    resetFile(mainFile);
    resetFile(overflowFile);

    this.mainFileStream = new RandomAccessFile(mainFile, "rw");
    this.overflowFileStream = new RandomAccessFile(overflowFile, "rw");

    this.trie = new Trie(maxDepth);
  }

  private static <T extends Record> Record[] getDataToFill(T recordToInsert, Block<T> block) {
    Record[] dataToFill = new Record[block.getValidRecordsCount() + 1];
    Record[] validRecordsOfBlock = block.getValidRecords();
    System.arraycopy(validRecordsOfBlock, 0, dataToFill, 0, validRecordsOfBlock.length);
    dataToFill[dataToFill.length - 1] = recordToInsert;
    return dataToFill;
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

  private long getNewBlockAddress() {
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

  private long getAddressFromFreeBlocks() {
    long freeBlockAddress = firstFreeBlockAddressFromMainFile;
    Block<T> freeBlock = getBlock(freeBlockAddress);
    long nextFreeBlockAddress = freeBlock.getNextFreeBlockAddress();

    if (nextFreeBlockAddress == INVALID_ADDRESS) {
      freeBlock.setNextFreeBlockAddress(INVALID_ADDRESS);
      writeBlock(freeBlock, freeBlockAddress);
      firstFreeBlockAddressFromMainFile = INVALID_ADDRESS;

      return freeBlockAddress;
    }

    Block<T> secondFreeBlock = getBlock(nextFreeBlockAddress);

    freeBlock.setNextFreeBlockAddress(INVALID_ADDRESS);
    secondFreeBlock.setPreviousFreeBlockAddress(INVALID_ADDRESS);

    writeBlock(freeBlock, freeBlockAddress);
    writeBlock(secondFreeBlock, nextFreeBlockAddress);

    firstFreeBlockAddressFromMainFile = nextFreeBlockAddress;
    return freeBlockAddress;
  }

  public T find(T recordToFind) {
    BitSet hash = recordToFind.hash();

    long address = trie.getLeafOfData(hash).getAddressOfData();

    if (address == INVALID_ADDRESS) {
      throw new IllegalStateException(
          String.format("Address for record %s was not found!", recordToFind));
    }

    Block<T> block = getBlock(address);

    return (T) block.getRecord(recordToFind);
  }

  public void insert(T recordToInsert) throws IOException {
    // checking if hash file already contains the item
    boolean isDuplicate;
    try {
      find(recordToInsert);
      isDuplicate = true;
    } catch (Exception e) {
      isDuplicate = false;
    }

    if (isDuplicate) {
      throw new IllegalStateException(
          String.format(
              "Cannot insert new item. DynamicHashFile already contains item %s", recordToInsert));
    }

    BitSet hash = recordToInsert.hash();
    LeafTrieNode leafOfData = trie.getLeafOfData(hash);
    long address = leafOfData.getAddressOfData();

    if (address == INVALID_ADDRESS) {
      throw new IllegalStateException(
          String.format("Address for record %s was not found!", recordToInsert));
    }

    Block<T> block = getBlock(address);

    // free space in block for data - insert
    if (block.hasFreeSpace()) {
      block.addRecord(recordToInsert);
      writeBlock(block, address);
      leafOfData.addDataInMainBlock(); // TODO zmenit a dat pozor ci aj nie do preplnujuceho
      return;
    }

    // no free space - expand Trie
    Record[] dataToFill = getDataToFill(recordToInsert, block);

    trie.expandLeafByHash(dataToFill, leafOfData);
  }

  public void delete(T recordToDelete) throws IOException {
    BitSet hash = recordToDelete.hash();

    LeafTrieNode leafOfData = trie.getLeafOfData(hash);
    long address = leafOfData.getAddressOfData();

    if (address == INVALID_ADDRESS) {
      throw new IllegalStateException(
          String.format("Address for record %s was not found!", recordToDelete));
    }

    Block<T> block = getBlock(address);

    block.removeRecord(recordToDelete);

    leafOfData.removeDataInMainBlock();

    writeBlock(block, address);

    trie.shrinkIfNeeded((InnerTrieNode) leafOfData.getParent());
  }

  private Block<T> getBlock(long address) {
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

  private void createBlock(long address) {
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

  private void writeBlock(Block<T> block, long address) {
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

  private void deleteBlock(LeafTrieNode nodeOfData) throws IOException {
    long addressOfData = nodeOfData.getAddressOfData();
    Block<T> blockToDelete = getBlock(addressOfData);

    if (isBlockOnTheEndOfFile(addressOfData, blockToDelete)) {
      // block is on the end of a file - set new length of file
      mainFileStream.setLength(addressOfData);
    } else {
      // block is in the middle - clear it and put it in free blocks
      nodeOfData.removeDataInMainBlock(nodeOfData.getDataSizeInMainBlock());
      blockToDelete.clear();
      writeBlock(blockToDelete, addressOfData);

      setBlockAsFirstFreeBlock(addressOfData, blockToDelete);
    }
  }

  private void setBlockAsFirstFreeBlock(long addressOfData, Block<T> blockToDelete) {
    if (firstFreeBlockAddressFromMainFile == INVALID_ADDRESS) {
      firstFreeBlockAddressFromMainFile = addressOfData;
      return;
    }

    Block<T> firstFreeBlock = getBlock(firstFreeBlockAddressFromMainFile);
    firstFreeBlock.setPreviousFreeBlockAddress(addressOfData);
    blockToDelete.setNextFreeBlockAddress(firstFreeBlockAddressFromMainFile);
    firstFreeBlockAddressFromMainFile = addressOfData;

    writeBlock(blockToDelete, addressOfData);
  }

  private boolean isBlockOnTheEndOfFile(long address, Block<T> blockToCheck) throws IOException {
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
      Block<T> block = getBlock(i);

      sb.append(i).append(" ").append(block);
    }

    return sb.toString();
  }

  @Override
  public void close() throws Exception {
    mainFileStream.close();
    overflowFileStream.close();
  }

  private class Trie {
    private final InnerTrieNode root;
    private final int maxDepth;

    private Trie(int maxDepth) {
      root = new InnerTrieNode(null, maxDepth);
      root.setLeftSon(new LeafTrieNode(root, maxDepth));
      root.setRightSon(new LeafTrieNode(root, maxDepth));

      long leftSonAddress = getNewBlockAddress();
      long rightSonAddress = getNewBlockAddress();
      ((LeafTrieNode) root.getLeftSon()).setAddressOfData(leftSonAddress);
      ((LeafTrieNode) root.getRightSon()).setAddressOfData(rightSonAddress);

      createBlock(leftSonAddress);
      createBlock(rightSonAddress);

      this.maxDepth = maxDepth;
    }

    private static <T extends Record> void fillBlockFromOtherBlock(
        Block<T> blockToFill, Block<T> blockToFillFrom) {
      for (Record validRecord : blockToFillFrom.getValidRecords()) {
        blockToFill.addRecord(validRecord);
      }
    }

    private LeafTrieNode getLeafOfData(BitSet hash) {
      int currentBitSetIndex = 0;
      TrieNode currentNode = root;
      TrieNode parent;
      do {
        parent = currentNode;

        currentNode =
            hash.get(currentBitSetIndex)
                ? ((InnerTrieNode) currentNode).getLeftSon()
                : ((InnerTrieNode) currentNode).getRightSon();

        if (currentNode == null) {
          currentNode = createLeafNode((InnerTrieNode) parent, hash.get(currentBitSetIndex));
        }

        if (currentNode instanceof InnerTrieNode) {
          currentBitSetIndex++;
          continue;
        }

        return ((LeafTrieNode) currentNode);
      } while (currentBitSetIndex
          != hash.size()); // TODO toto pozriet potom ci nedat do preplnujuceho

      return LeafTrieNode.getInvalidAddressNode();
    }

    private LeafTrieNode createLeafNode(InnerTrieNode parent, boolean isLeftSon) {
      LeafTrieNode createdNode = new LeafTrieNode(parent, maxDepth);

      if (isLeftSon) {
        parent.setLeftSon(createdNode);
      } else {
        parent.setRightSon(createdNode);
      }

      long address = getNewBlockAddress();
      createdNode.setAddressOfData(address);
      createBlock(address);
      return createdNode;
    }

    public void expandLeafByHash(Record[] dataToFill, LeafTrieNode leafOfData) throws IOException {
      LeafTrieNode leaf = leafOfData;
      InnerTrieNode parentOfOriginalLeaf = (InnerTrieNode) leaf.getParent();

      while (true) {
        long addressOfData = leaf.getAddressOfData();
        Block<T> blockOfNodeToExpand = getBlock(addressOfData);

        InnerTrieNode newTransformedInnerNode = new InnerTrieNode(parentOfOriginalLeaf, maxDepth);

        LeafTrieNode leftSon = new LeafTrieNode(leaf, maxDepth);
        LeafTrieNode rightSon = new LeafTrieNode(leaf, maxDepth);

        newTransformedInnerNode.setLeftSon(leftSon).setRightSon(rightSon);

        if (parentOfOriginalLeaf.getLeftSon() == leaf) {
          parentOfOriginalLeaf.setLeftSon(newTransformedInnerNode);
        } else if (parentOfOriginalLeaf.getRightSon() == leaf) {
          parentOfOriginalLeaf.setRightSon(newTransformedInnerNode);
        }

        Block<T> leftSonBlock = new Block<>(mainFileBlockingFactor, tClass);
        Block<T> rightSonBlock = new Block<>(mainFileBlockingFactor, tClass);

        boolean blockIsFull =
            fillBlocksByBits(
                dataToFill,
                leftSonBlock,
                rightSonBlock,
                newTransformedInnerNode.getDepth(),
                leftSon,
                rightSon);

        deleteBlock(leafOfData);

        if (blockIsFull) {
          if (leftSonBlock.getValidRecordsCount() == 0) {
            newTransformedInnerNode.setLeftSon(null);
          } else {
            leftSon.setAddressOfData(getNewBlockAddress());
            writeBlock(leftSonBlock, leftSon.getAddressOfData());
          }

          if (rightSonBlock.getValidRecordsCount() == 0) {
            newTransformedInnerNode.setRightSon(null);
          } else {
            rightSon.setAddressOfData(getNewBlockAddress());
            writeBlock(rightSonBlock, rightSon.getAddressOfData());
          }

          // continue expansion on full block
          leaf = (leftSonBlock.getValidRecordsCount() > 0) ? leftSon : rightSon;
          parentOfOriginalLeaf = newTransformedInnerNode;

          if (newTransformedInnerNode.getDepth() == maxDepth) {
            throw new IllegalStateException(String.format("Maximum depth %d exceeded!", maxDepth));
          }

          continue;
        }

        if (leftSonBlock.getValidRecordsCount() > 0) {
          leftSon.setAddressOfData(getNewBlockAddress());
          writeBlock(leftSonBlock, leftSon.getAddressOfData());
        }
        // TODO spravit preplnujuci subor - daj volny blok a zrus blok. Kazdy blok ma referenciu na
        // preplnujuci blok.
        if (rightSonBlock.getValidRecordsCount() > 0) {
          rightSon.setAddressOfData(getNewBlockAddress());
          writeBlock(rightSonBlock, rightSon.getAddressOfData());
        }

        break;
      }
    }

    private boolean fillBlocksByBits(
        Record[] dataToFill,
        Block<T> leftSonBlock,
        Block<T> rightSonBlock,
        int depth,
        LeafTrieNode leftSon,
        LeafTrieNode rightSon) {
      boolean blockIsFullFlag = false;
      for (Record record : dataToFill) {
        try {
          if (record.hash().get(depth)) {
            leftSonBlock.addRecord(record);
            leftSon.addDataInMainBlock();
          } else {
            rightSonBlock.addRecord(record);
            rightSon.addDataInMainBlock();
          }
        } catch (IllegalStateException e) {
          blockIsFullFlag = true;
          break;
        }
      }
      return blockIsFullFlag;
    }

    private void shrinkLeaves(InnerTrieNode parentNodeToShrink) throws IOException {
      LeafTrieNode leftChild = (LeafTrieNode) parentNodeToShrink.getLeftSon();
      LeafTrieNode rightChild = (LeafTrieNode) parentNodeToShrink.getRightSon();

      if (leftChild.hasItemsInOverflowBlock()) {
        throw new IllegalStateException(
            String.format(
                "Cannot shrink children of paretNode %s! At least one child has items in overflow block!",
                parentNodeToShrink));
      }

      int itemCountOfChildren =
          leftChild.getDataSizeInMainBlock() + rightChild.getDataSizeInMainBlock();
      if (itemCountOfChildren > mainFileBlockingFactor) {
        throw new IllegalStateException(
            String.format(
                "Cannot shrink children of parentNode %s! Children item size: %d, blocking factor: %d",
                parentNodeToShrink, itemCountOfChildren, mainFileBlockingFactor));
      }

      // shrink children into parent
      InnerTrieNode parentOfParent = (InnerTrieNode) parentNodeToShrink.getParent();

      if (parentOfParent == null) {
        throw new IllegalStateException("Cannot shrink root!");
      }

      LeafTrieNode newParentToShrinkChildrenInto =
          new LeafTrieNode(parentOfParent, parentOfParent.getDepth() + 1);

      // updating reference to new parent
      if (parentOfParent.getLeftSon() == parentNodeToShrink) {
        parentOfParent.setLeftSon(newParentToShrinkChildrenInto);
      } else if (parentOfParent.getRightSon() == parentNodeToShrink) {
        parentOfParent.setRightSon(newParentToShrinkChildrenInto);
      } else {
        throw new IllegalStateException(
            String.format(
                "Cannot shrink children of paretnNode %s! Parent node is not child of his parent %s!",
                parentNodeToShrink, parentOfParent));
      }

      long addressOfNewParent = getNewBlockAddress();
      createBlock(addressOfNewParent);
      newParentToShrinkChildrenInto.setAddressOfData(addressOfNewParent);

      // refill items from children to new parent
      long leftChildAddress = leftChild.getAddressOfData();
      long rightChildAddress = rightChild.getAddressOfData();

      Block<T> leftChildBlock = getBlock(leftChildAddress);
      Block<T> rightChildBlock = getBlock(rightChildAddress);

      Block<T> newBlock = getBlock(addressOfNewParent);
      fillBlockFromOtherBlock(newBlock, leftChildBlock);
      fillBlockFromOtherBlock(newBlock, rightChildBlock);

      newParentToShrinkChildrenInto.addDataInMainBlock(itemCountOfChildren);

      writeBlock(newBlock, addressOfNewParent);

      deleteBlock(leftChild);
      deleteBlock(rightChild);
    }

    public void shrinkIfNeeded(InnerTrieNode nodeToShrink) throws IOException {
      if (nodeToShrink == null) {
        return;
      }

      InnerTrieNode currentNode = nodeToShrink;
      while (currentNode != null) {
        boolean canShrinkIntoParent = isAbleToShrinkIntoParent(currentNode);

        if (!canShrinkIntoParent) {
          break;
        }

        try {
          shrinkLeaves(currentNode);
        } catch (IllegalStateException e) {
          break;
        }

        currentNode = (InnerTrieNode) currentNode.getParent();
      }
    }

    private boolean isAbleToShrinkIntoParent(InnerTrieNode currentNode) {
      LeafTrieNode leftChild = (LeafTrieNode) currentNode.getLeftSon();
      LeafTrieNode rightChild = (LeafTrieNode) currentNode.getRightSon();

      int leftChildSize = leftChild != null ? leftChild.getDataSizeInMainBlock() : 0;
      int rightChildSize = rightChild != null ? rightChild.getDataSizeInMainBlock() : 0;

      int itemCountOfChildren =
          leftChildSize + rightChildSize;

      boolean leftChildOverflowBlock = leftChild != null && !leftChild.hasItemsInOverflowBlock();
      boolean rightChildOverflowBlock = rightChild != null && !rightChild.hasItemsInOverflowBlock();

      return itemCountOfChildren <= mainFileBlockingFactor
          && leftChildOverflowBlock
          && rightChildOverflowBlock;
    }
  }
}

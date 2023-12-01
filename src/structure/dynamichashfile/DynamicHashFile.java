package structure.dynamichashfile;

import java.io.*;
import java.util.BitSet;
import java.util.Stack;
import structure.dynamichashfile.constant.ElementByteSize;
import structure.dynamichashfile.trie.*;

public class DynamicHashFile<T extends Record> implements AutoCloseable {
  private static final int INVALID_ADDRESS = -1;
  private final String path;
  private final File file;
  private final RandomAccessFile fileStream;
  private final int blockingFactor;
  private final Trie trie;
  private final Class<T> tClass;
  private final T tDummyInstance;

  public DynamicHashFile(String path, int blockingFactor, int maxDepth, Class<T> tClass)
      throws FileNotFoundException {
    this.path = path;
    this.file = new File(path);
    this.blockingFactor = blockingFactor;
    this.tClass = tClass;
    this.tDummyInstance = RecordFactory.getDummyInstance(tClass);

    resetFile();
    this.fileStream = new RandomAccessFile(file, "rw");

    this.trie = new Trie(maxDepth);
  }

  private static <T extends Record> Record[] getDataToFill(T recordToInsert, Block<T> block) {
    Record[] dataToFill = new Record[block.getValidRecordsCount() + 1];
    Record[] validRecordsOfBlock = block.getValidRecords();
    System.arraycopy(validRecordsOfBlock, 0, dataToFill, 0, validRecordsOfBlock.length);
    dataToFill[dataToFill.length - 1] = recordToInsert;
    return dataToFill;
  }

  private void resetFile() {
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
      long fileLength = fileStream.length();
      fileStream.setLength(
          fileLength
              + ((long) tDummyInstance.getByteSize() * blockingFactor
                  + (ElementByteSize.intByteSize() * 2L)));

      return fileLength;
    } catch (IOException e) {
      throw new RuntimeException("New block cannot been created! Message: " + e);
    }
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

  public void insert(T recordToInsert) {
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
      return;
    }

    // no free space - expand Trie
    Record[] dataToFill = getDataToFill(recordToInsert, block);

    trie.expandLeafByHash(hash, dataToFill, leafOfData);
  }

  private Block<T> getBlock(long address) {
    try {
      fileStream.seek(address);
      Block<T> block = new Block<>(blockingFactor, tClass);
      byte[] blockBytes = new byte[block.getByteSize()];
      // popripade mozno netreba ani zapisovat blokovaci faktor
      fileStream.read(blockBytes);
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
      fileStream.seek(address);
      fileStream.write(new Block<>(blockingFactor, tClass).toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to create new Block in address %d. Message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  private void writeBlock(Block<T> block, long address) {
    try {
      fileStream.seek(address);
      fileStream.write(block.toByteArray());

    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when trying to write block to address %d. Error message: %s",
              address, e.getLocalizedMessage()));
    }
  }

  public String sequenceToString() {
    StringBuilder sb = new StringBuilder();
    Stack<TrieNode> stack = new Stack<>();

    stack.push(trie.root);

    while (!stack.isEmpty()) {
      TrieNode currentNode = stack.pop();

      if (currentNode instanceof LeafTrieNode) {
        sb.append(getBlock(((LeafTrieNode) currentNode).getAddressOfData()));
      }

      if (currentNode instanceof InnerTrieNode) {
        TrieNode leftSon = ((InnerTrieNode) currentNode).getLeftSon();
        TrieNode rightSon = ((InnerTrieNode) currentNode).getRightSon();

        if (leftSon != null) {
          stack.push(leftSon);
        }

        if (rightSon != null) {
          stack.push(rightSon);
        }
      }
    }
    return sb.toString();
  }

  @Override
  public void close() throws Exception {
    fileStream.close();
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
      } while (currentBitSetIndex != hash.size());

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

    public void expandLeafByHash(BitSet hash, Record[] dataToFill, LeafTrieNode leafOfData) {
      LeafTrieNode leaf = leafOfData;
      InnerTrieNode parentOfOriginalLeaf = (InnerTrieNode) leaf.getParent();
      while (true) {
        InnerTrieNode newTransformedInnerNode = new InnerTrieNode(parentOfOriginalLeaf, maxDepth);

        LeafTrieNode leftSon = new LeafTrieNode(leaf, maxDepth);
        LeafTrieNode rightSon = new LeafTrieNode(leaf, maxDepth);

        newTransformedInnerNode.setLeftSon(leftSon).setRightSon(rightSon);

        if (parentOfOriginalLeaf.getLeftSon() == leaf) {
          parentOfOriginalLeaf.setLeftSon(newTransformedInnerNode);
        } else if (parentOfOriginalLeaf.getRightSon() == leaf) {
          parentOfOriginalLeaf.setRightSon(newTransformedInnerNode);
        }

        Block<T> leftSonBlock = new Block<>(blockingFactor, tClass);
        Block<T> rightSonBlock = new Block<>(blockingFactor, tClass);

        boolean blockIsFull =
            fillBlocksByBits(
                dataToFill, leftSonBlock, rightSonBlock, newTransformedInnerNode.getDepth());

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

        if (rightSonBlock.getValidRecordsCount() > 0) {
          rightSon.setAddressOfData(getNewBlockAddress());
          writeBlock(rightSonBlock, rightSon.getAddressOfData());
        }

        break;
      }
    }

    private boolean fillBlocksByBits(
        Record[] dataToFill, Block<T> leftSonBlock, Block<T> rightSonBlock, int depth) {
      boolean blockIsFullFlag = false;
      for (Record record : dataToFill) {
        try {
          if (record.hash().get(depth)) {
            leftSonBlock.addRecord(record);
          } else {
            rightSonBlock.addRecord(record);
          }
        } catch (IllegalStateException e) {
          blockIsFullFlag = true;
          break;
        }
      }
      return blockIsFullFlag;
    }
  }
}

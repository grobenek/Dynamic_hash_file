package structure.dynamichashfile;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;
import structure.dynamichashfile.entity.Block;
import structure.dynamichashfile.entity.record.Record;
import structure.dynamichashfile.entity.record.RecordFactory;
import structure.dynamichashfile.trie.*;
import util.file.dynamichashfile.DynamicHashFileInfo;

public class DynamicHashFile<T extends Record> implements AutoCloseable {
  private static final int INVALID_ADDRESS = Block.getInvalidAddress();
  private final FileBlockManager<T> fileBlockManager;
  private Trie trie;

  public DynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int blockingFactorOfMainFile,
      int blockingFactorOfOverflowFile,
      Class<T> tClass)
      throws IOException {

    this.fileBlockManager =
        new FileBlockManager<>(
            pathToMainFile,
            blockingFactorOfMainFile,
            pathToOverflowFile,
            blockingFactorOfOverflowFile,
            tClass);

    this.trie = new Trie(RecordFactory.getDummyInstance(tClass).getMaxHashSize());
  }

  public DynamicHashFile(
      String pathToMainFile,
      String pathToOverflowFile,
      int blockingFactorOfMainFile,
      int blockingFactorOfOverflowFile,
      Class<T> tClass,
      InnerTrieNode rootOfTrie)
      throws IOException {

    this.fileBlockManager =
        new FileBlockManager<>(
            pathToMainFile,
            blockingFactorOfMainFile,
            pathToOverflowFile,
            blockingFactorOfOverflowFile,
            tClass);

    this.trie = new Trie(rootOfTrie, RecordFactory.getDummyInstance(tClass).getMaxHashSize());
  }

  private static <T extends Record> Record[] getDataToFill(T recordToInsert, Block<T> block) {
    Record[] dataToFill = new Record[block.getValidRecordsCount() + 1];
    Record[] validRecordsOfBlock = block.getValidRecords();
    System.arraycopy(validRecordsOfBlock, 0, dataToFill, 0, validRecordsOfBlock.length);
    dataToFill[dataToFill.length - 1] = recordToInsert;
    return dataToFill;
  }

  public T find(T recordToFind) {
    if (recordToFind == null) {
      throw new IllegalArgumentException("Cannot find null record!");
    }

    BitSet hash = recordToFind.hash();

    long address = trie.getLeafOfData(hash).getAddressOfData();

    if (address == INVALID_ADDRESS) {
      throw new IllegalStateException(
          String.format("Address for record %s was not found!", recordToFind));
    }

    Block<T> block = fileBlockManager.getMainBlock(address);

    T foundRecord = (T) block.getRecord(recordToFind);

    if (foundRecord == null) {
      long overflowBlockAddress = block.getAddressOfOverflowBlock();

      while (overflowBlockAddress != INVALID_ADDRESS) {
        block = fileBlockManager.getOverflowBlock(overflowBlockAddress);
        foundRecord = (T) block.getRecord(recordToFind);

        if (foundRecord != null) {
          return foundRecord;
        }

        overflowBlockAddress = block.getNextOverflowBlockAddress();

        if (overflowBlockAddress == INVALID_ADDRESS) {
          throw new IllegalStateException(String.format("Record %s was not found!", recordToFind));
        }
      }
    } else {
      return foundRecord;
    }

    throw new IllegalStateException(String.format("Record %s was not found!", recordToFind));
  }

  public void edit(T recordToEdit, T changedRecordToSave) {
    if (!recordToEdit.equals(changedRecordToSave)) {
      throw new IllegalArgumentException(
          String.format(
              "Cannot edit record %s to record %s. Objects are not equal!",
              recordToEdit, changedRecordToSave));
    }

    if (recordToEdit == null) {
      throw new IllegalArgumentException("Cannot find null record!");
    }

    BitSet hash = recordToEdit.hash();

    long address = trie.getLeafOfData(hash).getAddressOfData();

    if (address == INVALID_ADDRESS) {
      throw new IllegalStateException(
          String.format("Address for record %s was not found!", recordToEdit));
    }

    Block<T> block = fileBlockManager.getMainBlock(address);

    T foundRecord = (T) block.getRecord(recordToEdit);

    if (foundRecord == null) {
      long overflowBlockAddress = block.getAddressOfOverflowBlock();

      while (overflowBlockAddress != INVALID_ADDRESS) {
        block = fileBlockManager.getOverflowBlock(overflowBlockAddress);
        foundRecord = (T) block.getRecord(recordToEdit);

        if (foundRecord != null) {
          // swap objects
          block.removeRecord(foundRecord);
          block.addRecord(changedRecordToSave);

          // save block
          fileBlockManager.writeOverflowBlock(block, overflowBlockAddress);
          return;
        }

        overflowBlockAddress = block.getNextOverflowBlockAddress();

        if (overflowBlockAddress == INVALID_ADDRESS) {
          throw new IllegalStateException(String.format("Record %s was not found!", recordToEdit));
        }
      }
    } else {
      // swap objects
      block.removeRecord(foundRecord);
      block.addRecord(changedRecordToSave);

      // save block
      fileBlockManager.writeMainBlock(block, address);
      return;
    }

    throw new IllegalStateException(String.format("Record %s was not found!", recordToEdit));
  }

  public void insert(T recordToInsert) {
    // checking if hash file already contains the item
    boolean isDuplicate;
    try {
      find(recordToInsert);
      isDuplicate = true;
    } catch (IllegalStateException e) {
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

    Block<T> block = fileBlockManager.getMainBlock(address);

    // free space in block for data - insert
    if (block.hasFreeSpace()) {
      block.addRecord(recordToInsert);
      fileBlockManager.writeMainBlock(block, address);
      leafOfData.addDataInMainBlock();
      return;
    }

    // no free space - expand Trie
    Record[] dataToFill = getDataToFill(recordToInsert, block);

    try {
      trie.expandLeafByHash(dataToFill, leafOfData);
    } catch (IOException e) {
      throw new RuntimeException(
          String.format(
              "Error occured when expanding leaf %s. Error message: %s",
              leafOfData, e.getLocalizedMessage()));
    }
  }

  public void delete(T recordToDelete) throws IOException {
    if (recordToDelete == null) {
      throw new IllegalArgumentException("Cannot delete null record!");
    }

    BitSet hash = recordToDelete.hash();

    LeafTrieNode leafOfData = trie.getLeafOfData(hash);
    long address = leafOfData.getAddressOfData();

    if (address == INVALID_ADDRESS) {
      throw new IllegalStateException(
          String.format("Address for record %s was not found when deleting!", recordToDelete));
    }

    Block<T> block = fileBlockManager.getMainBlock(address);

    T foundRecord = (T) block.getRecord(recordToDelete);

    if (foundRecord == null) {
      long overflowBlockAddress = block.getAddressOfOverflowBlock();

      if (overflowBlockAddress == INVALID_ADDRESS) {
        throw new IllegalStateException(
            String.format("Cannot delete record %s, it was not found!", recordToDelete));
      }

      while (overflowBlockAddress != INVALID_ADDRESS) {
        Block<T> overflowBlock = fileBlockManager.getOverflowBlock(overflowBlockAddress);
        foundRecord = (T) overflowBlock.getRecord(recordToDelete);

        if (foundRecord != null) {
          overflowBlock.removeRecord(foundRecord);
          leafOfData.removeDataInReserveBlock();

//                    if (overflowBlock.isEmpty()
//                        && fileBlockManager.isOverflowBlockOnTheEndOfFile(overflowBlockAddress, overflowBlock)) {
//                      // is empty and on end of file
//                      long previousBlockAddress = overflowBlock.getPreviousOverflowBlockAddress();
//
//                      if (previousBlockAddress != INVALID_ADDRESS) {
//                        // update previous block refference to this block
//                        Block<T> previousBlock = fileBlockManager.getOverflowBlock(previousBlockAddress);
//                        previousBlock.setNextOverflowBlockAddress(INVALID_ADDRESS);
//
//                        if (overflowBlock.getNextOverflowBlockAddress() != INVALID_ADDRESS) {
//                          previousBlock.setNextOverflowBlockAddress(overflowBlock.getNextOverflowBlockAddress());
//                        }
//
//                        fileBlockManager.writeOverflowBlock(previousBlock, previousBlockAddress);
//                      } else {
//                        // no previous block - delete overflow block and update main block
//                        block.setAddressOfOverflowBlock(INVALID_ADDRESS);
//                        fileBlockManager.writeMainBlock(block, address); //TODO mozno refactornut delete metodu a spravit jednu na rovno seknutie file - odstranim duplicitne checkovanie
//                      }
//
//                      //remove block
//                      fileBlockManager.deleteOverflowBlock(leafOfData);
//                      return;
//                    }
//                    //TODO spravit to iste aj s overflow blockom
          fileBlockManager.writeOverflowBlock(overflowBlock, overflowBlockAddress);
          break;
        }

        overflowBlockAddress = overflowBlock.getNextOverflowBlockAddress();

        if (overflowBlockAddress == INVALID_ADDRESS) {
          throw new IllegalStateException(
              String.format("Cannot delete record %s, it was not found!", recordToDelete));
        }
      }
    } else {
      block.removeRecord(foundRecord);
      leafOfData.removeDataInMainBlock();

      // checking if block has become empty and on end of file
      if (block.isEmpty() && leafOfData.getDataSizeInReserveBlock() == 0) {
        // delete block from end of file
        fileBlockManager.deleteMainBlock(leafOfData);
        InnerTrieNode paretnOfData = (InnerTrieNode) leafOfData.getParent();

        if (paretnOfData.getLeftSon() != null && paretnOfData.getLeftSon().equals(leafOfData)) {
          paretnOfData.setLeftSon(null);
        }
        if (paretnOfData.getRightSon() != null && paretnOfData.getRightSon().equals(leafOfData)) {
          paretnOfData.setRightSon(null);
        }
      } else {
        fileBlockManager.writeMainBlock(block, address);
      }

      try {
        trie.shrinkIfNeeded((InnerTrieNode) leafOfData.getParent());
      } catch (IOException e) {
        throw new RuntimeException(
            String.format(
                "Cannot try to shrink node %s. Error message: %s",
                leafOfData.getParent(), e.getLocalizedMessage()));
      }
    }

    //TODO striasanie
  }

  public String sequenceToStringMainFile() {
    try {
      return fileBlockManager.sequenceToStringMainFile();
    } catch (IOException e) {
      return "There was error reading main file: " + e.getLocalizedMessage();
    }
  }

  public String sequenceToStringOverflowFile() {
    try {
      return fileBlockManager.sequenceToStringOverflowFile();
    } catch (IOException e) {
      return "There was error reading overflow file: " + e.getLocalizedMessage();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      fileBlockManager.close();
    } catch (IOException e) {
      throw new IOException(e);
    }
  }

  public List<TrieNode> getTrieNodes() {
    List<TrieNode> nodes = new ArrayList<>();

    Stack<TrieNode> nodesStack = new Stack<>();
    nodesStack.push(trie.root);

    while (!nodesStack.isEmpty()) {
      TrieNode currentNode = nodesStack.pop();
      nodes.add(currentNode);

      if (currentNode instanceof InnerTrieNode innerCurrentNode) {
        if (innerCurrentNode.getRightSon() != null) {
          nodesStack.push(innerCurrentNode.getRightSon());
        } else {
          nodes.add(null);
        }

        if (innerCurrentNode.getLeftSon() != null) {
          nodesStack.push(innerCurrentNode.getLeftSon());
        } else {
          nodes.add(null);
        }
      }
    }

    return nodes;
  }

  public int getTrieMaxDepth() {
    return trie.maxDepth;
  }

  public DynamicHashFileInfo getInfo() {
    return new DynamicHashFileInfo(
        fileBlockManager.getMainFileBlockingFactor(),
        fileBlockManager.getOverflowFileBlockingFactor(),
        fileBlockManager.getTClass(),
        fileBlockManager.getMainFilePath(),
        fileBlockManager.getOvetflowFilePath());
  }

  public void setTrie(InnerTrieNode root) {
    trie =
        new Trie(
            root, RecordFactory.getDummyInstance(fileBlockManager.getTClass()).getMaxHashSize());
  }

  public void setMainFileBlockingFactor(int mainFileBlockingFactor) {
    fileBlockManager.setMainFileBlockingFactor(mainFileBlockingFactor);
  }

  public void setOverflowFileBlockingFactor(int overflowFileBlockingFactor) {
    fileBlockManager.setOverflowFileBlockingFactor(overflowFileBlockingFactor);
  }

  private class Trie {
    private final InnerTrieNode root;
    private final int maxDepth;

    private Trie(int maxDepth) {
      root = new InnerTrieNode(null, maxDepth);
      root.setLeftSon(new LeafTrieNode(root, maxDepth));
      root.setRightSon(new LeafTrieNode(root, maxDepth));

      //      long leftSonAddress = fileBlockManager.getNewMainBlockAddress();
      //      long rightSonAddress = fileBlockManager.getNewMainBlockAddress();
      //      ((LeafTrieNode) root.getLeftSon()).setAddressOfData(leftSonAddress);
      //      ((LeafTrieNode) root.getRightSon()).setAddressOfData(rightSonAddress);
      //
      //      fileBlockManager.createMainBlock(leftSonAddress);
      //      fileBlockManager.createMainBlock(rightSonAddress);

      this.maxDepth = maxDepth;
    }

    private Trie(InnerTrieNode root, int maxDepth) {
      this.root = root;
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

        // if node has no address, create one
        LeafTrieNode currentLeafNode = (LeafTrieNode) currentNode;
        if (currentLeafNode.getAddressOfData() == INVALID_ADDRESS) {
          currentLeafNode.setAddressOfData(fileBlockManager.getNewMainBlockAddress());
          fileBlockManager.createMainBlock(currentLeafNode.getAddressOfData());
        }

        return currentLeafNode;
      } while (currentBitSetIndex != maxDepth + 1);

      return LeafTrieNode.getInvalidAddressNode();
    }

    private LeafTrieNode createLeafNode(InnerTrieNode parent, boolean isLeftSon) {
      LeafTrieNode createdNode = new LeafTrieNode(parent, maxDepth);

      if (isLeftSon) {
        parent.setLeftSon(createdNode);
      } else {
        parent.setRightSon(createdNode);
      }

      long address = fileBlockManager.getNewMainBlockAddress();
      createdNode.setAddressOfData(address);
      fileBlockManager.createMainBlock(address);
      return createdNode;
    }

    public void expandLeafByHash(Record[] dataToFill, LeafTrieNode leafToExpand)
        throws IOException {
      LeafTrieNode leafBeingExpanded = leafToExpand;
      InnerTrieNode parentOfOriginalLeaf = (InnerTrieNode) leafBeingExpanded.getParent();

      while (true) {

        if (leafBeingExpanded.getDepth() == maxDepth) {
          insertDataInOveflowFile(dataToFill[dataToFill.length - 1], leafBeingExpanded);
          break;
        }

        InnerTrieNode newTransformedInnerNode = new InnerTrieNode(parentOfOriginalLeaf, maxDepth);

        LeafTrieNode leftSon = new LeafTrieNode(leafBeingExpanded, maxDepth);
        LeafTrieNode rightSon = new LeafTrieNode(leafBeingExpanded, maxDepth);

        newTransformedInnerNode.setLeftSon(leftSon).setRightSon(rightSon);

        if (parentOfOriginalLeaf.getLeftSon() == leafBeingExpanded) {
          parentOfOriginalLeaf.setLeftSon(newTransformedInnerNode);
        } else if (parentOfOriginalLeaf.getRightSon() == leafBeingExpanded) {
          parentOfOriginalLeaf.setRightSon(newTransformedInnerNode);
        }

        Block<T> leftSonBlock =
            new Block<>(fileBlockManager.getMainFileBlockingFactor(), fileBlockManager.getTClass());
        Block<T> rightSonBlock =
            new Block<>(fileBlockManager.getMainFileBlockingFactor(), fileBlockManager.getTClass());

        boolean blockIsFull =
            fillBlocksByBits(
                dataToFill,
                leftSonBlock,
                rightSonBlock,
                newTransformedInnerNode.getDepth(),
                leftSon,
                rightSon);

        fileBlockManager.deleteMainBlock(leafBeingExpanded);

        if (blockIsFull) {
          if (leftSonBlock.getValidRecordsCount() == 0) {
            newTransformedInnerNode.setLeftSon(null);
          } else {
            leftSon.setAddressOfData(fileBlockManager.getNewMainBlockAddress());
            fileBlockManager.writeMainBlock(leftSonBlock, leftSon.getAddressOfData());
          }

          if (rightSonBlock.getValidRecordsCount() == 0) {
            newTransformedInnerNode.setRightSon(null);
          } else {
            rightSon.setAddressOfData(fileBlockManager.getNewMainBlockAddress());
            fileBlockManager.writeMainBlock(rightSonBlock, rightSon.getAddressOfData());
          }

          // continue expansion on full block
          leafBeingExpanded = (leftSonBlock.getValidRecordsCount() > 0) ? leftSon : rightSon;
          parentOfOriginalLeaf = newTransformedInnerNode;

          continue;
        }

        if (leftSonBlock.getValidRecordsCount() > 0) {
          leftSon.setAddressOfData(fileBlockManager.getNewMainBlockAddress());
          fileBlockManager.writeMainBlock(leftSonBlock, leftSon.getAddressOfData());
        }
        if (rightSonBlock.getValidRecordsCount() > 0) {
          rightSon.setAddressOfData(fileBlockManager.getNewMainBlockAddress());
          fileBlockManager.writeMainBlock(rightSonBlock, rightSon.getAddressOfData());
        }

        break;
      }
    }

    private void insertDataInOveflowFile(Record dataToInsert, LeafTrieNode nodeOfMainBlock) {
      Block<T> mainBlock = fileBlockManager.getMainBlock(nodeOfMainBlock.getAddressOfData());

      // getting free overflow block
      Block<T> overflowBlock;
      long addressOfOverflowBlock;
      if (mainBlock.getAddressOfOverflowBlock() == INVALID_ADDRESS) {
        // creating new overflow block
        addressOfOverflowBlock = fileBlockManager.getNewOverflowBlockAddress();
        overflowBlock = fileBlockManager.createOverflowBlock(addressOfOverflowBlock);
        mainBlock.setAddressOfOverflowBlock(addressOfOverflowBlock);
      } else {
        // finding free block
        addressOfOverflowBlock = mainBlock.getAddressOfOverflowBlock();
        overflowBlock = fileBlockManager.getOverflowBlock(addressOfOverflowBlock);

        while (true) {
          if (!overflowBlock.hasFreeSpace()) {
            // no space, looking for next
            if (overflowBlock.getNextOverflowBlockAddress() == INVALID_ADDRESS) {
              // creating new overflow block
              long newOverflowBlockAddress = fileBlockManager.getNewOverflowBlockAddress();
              Block<T> newOverflowBlock =
                  fileBlockManager.createOverflowBlock(newOverflowBlockAddress);

              overflowBlock.setNextOverflowBlockAddress(newOverflowBlockAddress);
              newOverflowBlock.setPreviousOverflowBlockAddress(addressOfOverflowBlock);

              fileBlockManager.writeOverflowBlock(overflowBlock, addressOfOverflowBlock);

              overflowBlock = newOverflowBlock;
              addressOfOverflowBlock = newOverflowBlockAddress;
            } else {
              addressOfOverflowBlock = overflowBlock.getNextOverflowBlockAddress();
              overflowBlock = fileBlockManager.getOverflowBlock(addressOfOverflowBlock);
            }
          } else {
            // has free space
            break;
          }
        }
      }

      // adding data
      overflowBlock.addRecord(dataToInsert);
      nodeOfMainBlock.addDataInReserveBlocks();

      // saving blocks
      fileBlockManager.writeMainBlock(mainBlock, nodeOfMainBlock.getAddressOfData());
      fileBlockManager.writeOverflowBlock(overflowBlock, addressOfOverflowBlock);
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
      if (itemCountOfChildren > fileBlockManager.getMainFileBlockingFactor()) {
        throw new IllegalStateException(
            String.format(
                "Cannot shrink children of parentNode %s! Children item size: %d, blocking factor: %d",
                parentNodeToShrink,
                itemCountOfChildren,
                fileBlockManager.getMainFileBlockingFactor()));
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

      long addressOfNewParent = fileBlockManager.getNewMainBlockAddress();
      fileBlockManager.createMainBlock(addressOfNewParent);
      newParentToShrinkChildrenInto.setAddressOfData(addressOfNewParent);

      // refill items from children to new parent
      long leftChildAddress = leftChild.getAddressOfData();
      long rightChildAddress = rightChild.getAddressOfData();

      Block<T> leftChildBlock = fileBlockManager.getMainBlock(leftChildAddress);
      Block<T> rightChildBlock = fileBlockManager.getMainBlock(rightChildAddress);

      Block<T> newBlock = fileBlockManager.getMainBlock(addressOfNewParent);
      fillBlockFromOtherBlock(newBlock, leftChildBlock);
      fillBlockFromOtherBlock(newBlock, rightChildBlock);

      newParentToShrinkChildrenInto.addDataInMainBlock(itemCountOfChildren);

      fileBlockManager.writeMainBlock(newBlock, addressOfNewParent);

      fileBlockManager.deleteMainBlock(leftChild);
      fileBlockManager.deleteMainBlock(rightChild);
    }

    public void shrinkIfNeeded(InnerTrieNode nodeToShrink) throws IOException {
      if (nodeToShrink == null) {
        return;
      }

      InnerTrieNode currentNode = nodeToShrink;
      while (currentNode != null) {
        boolean canShrinkIntoParent = isAbleToShrinkIntoParent(currentNode);

        if (!canShrinkIntoParent) {
          // check if either block is empty and is on the end of file
          if (currentNode.getLeftSon() != null
              && currentNode.getLeftSon() instanceof LeafTrieNode leftChild
              && leftChild.getDataSizeInReserveBlock() == 0
              && leftChild.getAddressOfData() != INVALID_ADDRESS) {
            Block<T> leftSonBlock = fileBlockManager.getMainBlock(leftChild.getAddressOfData());
            if (leftSonBlock.isEmpty()
                && fileBlockManager.isMainBlockOnTheEndOfFile(
                    leftChild.getAddressOfData(), leftSonBlock)) {
              fileBlockManager.deleteMainBlock(leftChild);
              currentNode.setLeftSon(null);
            }
          }

          if (currentNode.getRightSon() != null
              && currentNode.getRightSon() instanceof LeafTrieNode rightChild
              && rightChild.getDataSizeInReserveBlock() == 0
              && rightChild.getAddressOfData() != INVALID_ADDRESS) {
            Block<T> rightSonBlock = fileBlockManager.getMainBlock(rightChild.getAddressOfData());
            if (rightSonBlock.isEmpty()
                && fileBlockManager.isMainBlockOnTheEndOfFile(
                    rightChild.getAddressOfData(), rightSonBlock)) {
              fileBlockManager.deleteMainBlock(rightChild);
              currentNode.setRightSon(null);
            }
          }
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
      LeafTrieNode leftChild =
          currentNode.getLeftSon() instanceof LeafTrieNode
              ? (LeafTrieNode) currentNode.getLeftSon()
              : null;
      LeafTrieNode rightChild =
          currentNode.getRightSon() instanceof LeafTrieNode
              ? (LeafTrieNode) currentNode.getRightSon()
              : null;

      int leftChildSize = leftChild != null ? leftChild.getDataSizeInMainBlock() : 0;
      int rightChildSize = rightChild != null ? rightChild.getDataSizeInMainBlock() : 0;

      int itemCountOfChildren = leftChildSize + rightChildSize;

      boolean leftChildOverflowBlock = leftChild != null && !leftChild.hasItemsInOverflowBlock();
      boolean rightChildOverflowBlock = rightChild != null && !rightChild.hasItemsInOverflowBlock();

      return itemCountOfChildren <= fileBlockManager.getMainFileBlockingFactor()
          && leftChildOverflowBlock
          && rightChildOverflowBlock;
    }

    public int getMaxDepth() {
      return maxDepth;
    }
  }
}

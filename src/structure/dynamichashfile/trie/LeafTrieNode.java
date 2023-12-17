package structure.dynamichashfile.trie;

public class LeafTrieNode extends TrieNode {
  private static final LeafTrieNode INVALID_ADDRESS_NODE;

  static {
    INVALID_ADDRESS_NODE = new LeafTrieNode(null, INVALID_ADDRESS);
  }

  private long addressOfData;
  private int dataSizeInMainBlock;
  private int dataSizeInReserveBlocks;
  private int overflowBlocksCount;

  public LeafTrieNode() {}

  public LeafTrieNode(TrieNode parent, int maxDepth) {
    super(parent, maxDepth);
    this.addressOfData = INVALID_ADDRESS;
  }

  public static LeafTrieNode getInvalidAddressNode() {
    return INVALID_ADDRESS_NODE;
  }

  public void addDataInMainBlock() {
    dataSizeInMainBlock++;
  }

  public void addDataInMainBlock(int count) {
    dataSizeInMainBlock += count;
  }

  public void addDataInReserveBlocks() {
    dataSizeInReserveBlocks++;
  }

  public void removeDataInMainBlock() {
    dataSizeInMainBlock--;
  }

  public void removeDataInMainBlock(int count) {
    dataSizeInMainBlock -= count;
  }

  public void removeDataInReserveBlock() {
    dataSizeInReserveBlocks--;
  }

  public void removeDataInReserveBlock(int count) {
    dataSizeInReserveBlocks -= count;
  }

  public void increaseOverflowBlocksCount() {
    overflowBlocksCount++;
  }

  public void decreaseOverflowBlocksCount() {
    overflowBlocksCount--;
  }

  public int getOverflowBlocksCount() {
    return overflowBlocksCount;
  }

  public LeafTrieNode setOverflowBlocksCount(int overflowBlocksCount) {
    this.overflowBlocksCount = overflowBlocksCount;
    return this;
  }

  public int getDataSizeInReserveBlocks() {
    return dataSizeInReserveBlocks;
  }

  public LeafTrieNode setDataSizeInReserveBlocks(int dataSizeInReserveBlocks) {
    this.dataSizeInReserveBlocks = dataSizeInReserveBlocks;
    return this;
  }

  public int getDataSizeInMainBlock() {
    return dataSizeInMainBlock;
  }

  public LeafTrieNode setDataSizeInMainBlock(int dataSizeInMainBlock) {
    this.dataSizeInMainBlock = dataSizeInMainBlock;
    return this;
  }

  public int getDataSizeInReserveBlock() {
    return dataSizeInReserveBlocks;
  }

  public long getAddressOfData() {
    return addressOfData;
  }

  public LeafTrieNode setAddressOfData(long addressOfData) {
    this.addressOfData = addressOfData;
    return this;
  }

  public boolean hasItemsInOverflowBlock() {
    return dataSizeInReserveBlocks != 0;
  }

}

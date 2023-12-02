package structure.dynamichashfile.trie;

public class LeafTrieNode extends TrieNode {
  private static final LeafTrieNode INVALID_ADDRESS_NODE;

  static {
    INVALID_ADDRESS_NODE = new LeafTrieNode(null, INVALID_ADDRESS);
  }

  private long addressOfData;
  private int dataSizeInMainBlock;
  private int dataSizeInReserveBlocks;

  public LeafTrieNode(TrieNode parent, long addressOfData, int maxDepth) {
    super(parent, maxDepth);
    this.addressOfData = addressOfData;
  }

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

  public void removeDataInReserveBlcok() {
    dataSizeInReserveBlocks--;
  }

  public int getDataSizeInMainBlock() {
    return dataSizeInMainBlock;
  }

  public int getDataSizeInReserveBlocks() {
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

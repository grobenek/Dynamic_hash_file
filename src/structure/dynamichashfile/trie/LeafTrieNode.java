package structure.dynamichashfile.trie;

public class LeafTrieNode extends TrieNode {
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

  public void addDataInMainBlock() {
    dataSizeInMainBlock++;
  }

  public void addDataInReserveBlocks() {
    dataSizeInReserveBlocks++;
  }

  public void removeDataInMainBlock() {
    dataSizeInMainBlock--;
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
}

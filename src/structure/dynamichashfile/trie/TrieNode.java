package structure.dynamichashfile.trie;

public abstract class TrieNode {
  protected static final int INVALID_ADDRESS = -1;
  private TrieNode parent;
  private int depth;

  public TrieNode(TrieNode parent, int maxDepth) {
    this.parent = parent;

    depth = (parent == null) ? 0 : Math.min(parent.depth + 1, maxDepth);
  }

  public TrieNode() {}
  ;

  public TrieNode getParent() {
    return parent;
  }

  public TrieNode setParent(TrieNode parent) {
    this.parent = parent;
    return this;
  }

  public int getDepth() {
    return depth;
  }
}

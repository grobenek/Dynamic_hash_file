package structure.dynamichashfile.trie;

public class InnerTrieNode extends TrieNode {
  private TrieNode leftSon;
  private TrieNode rightSon;

  public InnerTrieNode(TrieNode parent, TrieNode leftSon, TrieNode rightSon, int maxDepth) {
    super(parent, maxDepth);
    this.leftSon = leftSon;
    this.rightSon = rightSon;
  }

  public InnerTrieNode(TrieNode parent, int maxDepth) {
    super(parent, maxDepth);
    this.leftSon = null;
    this.rightSon = null;
  }

  public TrieNode getLeftSon() {
    return leftSon;
  }

  public InnerTrieNode setLeftSon(TrieNode leftSon) {
    this.leftSon = leftSon;
    if (leftSon != null) {
      leftSon.setParent(this);
    }
    return this;
  }

  public TrieNode getRightSon() {
    return rightSon;
  }

  public InnerTrieNode setRightSon(TrieNode rightSon) {
    this.rightSon = rightSon;

    if (rightSon != null) {
      rightSon.setParent(this);
    }
    return this;
  }
}

package util.file.dynamichashfile;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import structure.dynamichashfile.trie.InnerTrieNode;
import structure.dynamichashfile.trie.LeafTrieNode;
import structure.dynamichashfile.trie.TrieNode;
import util.file.IFileBuilder;

public class TextBuilderTrie<T extends TrieNode> implements IFileBuilder<T> {
  public static final String DELIMITER = " ";
  public static final String NULL = "null";
  private List<T> loadedItems;

  public TextBuilderTrie() {
    this.loadedItems = new ArrayList<>();
  }

  @Override
  public void saveToFile(String pathToFile, List<T> itemsToSave) throws IOException {
    if (itemsToSave.isEmpty()) {
      return;
    }
    StringBuilder sb = new StringBuilder();

    for (T node : itemsToSave) {
      if (node == null) {
        sb.append(NULL);
      }

      if (node instanceof InnerTrieNode) {
        sb.append(node.getClass().getName());
      }

      if (node instanceof LeafTrieNode) {
        sb.append(node.getClass().getName());
        sb.append(DELIMITER);
        sb.append(((LeafTrieNode) node).getAddressOfData());
        sb.append(DELIMITER);
        sb.append(((LeafTrieNode) node).getDataSizeInMainBlock());
        sb.append(DELIMITER);
        sb.append(((LeafTrieNode) node).getDataSizeInReserveBlock());
        sb.append(DELIMITER);
        sb.append(((LeafTrieNode) node).getOverflowBlocksCount());
      }
      sb.append(System.lineSeparator());
    }

    try (DataOutputStream dataOutputStream =
        new DataOutputStream(new FileOutputStream(pathToFile))) {
      dataOutputStream.writeBytes(sb.toString());
    }
  }

  @Override
  public void loadFromFile(String pathToFile) throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToFile))) {

      while (true) {
        String line = bufferedReader.readLine();

        if (line == null) {
          break;
        }

        String[] data = line.split(DELIMITER);

        String className = data[0];

        if (className.equals(NULL)) {
          loadedItems.add(null);
          continue;
        }

        Class<?> classToCreate = Class.forName(className);

        Object createdObject = classToCreate.getConstructors()[0].newInstance();

        if (createdObject instanceof LeafTrieNode) {
          int addressOfData = Integer.parseInt(data[1]);
          int dataSizeInMainBlock = Integer.parseInt(data[2]);
          int dataSizeInReserveBlock = Integer.parseInt(data[3]);
          int overflowBlocksCount = Integer.parseInt(data[4]);

          ((LeafTrieNode) createdObject).setAddressOfData(addressOfData);
          ((LeafTrieNode) createdObject).setDataSizeInMainBlock(dataSizeInMainBlock);
          ((LeafTrieNode) createdObject).setDataSizeInReserveBlocks(dataSizeInReserveBlock);
          ((LeafTrieNode) createdObject).setOverflowBlocksCount(overflowBlocksCount);

          loadedItems.add((T) createdObject);
        }

        if (createdObject instanceof InnerTrieNode) {
          loadedItems.add((T) createdObject);
        }
      }

      // setting relations between nodes
      Stack<TrieNode> parentStack = new Stack<>();
      Stack<Boolean> orderStack = new Stack<>();

      TrieNode root = null;

      for (TrieNode current : loadedItems) {
        if (root == null) {
          root = current;
          parentStack.push(root);
          // preorder - so starting with left side of tree
          orderStack.push(true);
        }
        boolean attachLeftSon = orderStack.pop();

        InnerTrieNode parent = (InnerTrieNode) parentStack.peek();
        if (attachLeftSon) {
          parent.setLeftSon(current);
          orderStack.push(false);
        } else {
          parent.setRightSon(current);
          parentStack.pop();
        }

        // add new parent to stack
        if (current instanceof InnerTrieNode) {
          parentStack.push(current);
          // reset orders
          orderStack.push(true);
        }
      }

    } catch (ClassNotFoundException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<T> getLoadedData() {
    return loadedItems;
  }

  @Override
  public void clearLoadedData() {
    loadedItems = new ArrayList<>();
  }
}

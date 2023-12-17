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

      // setting relations
      Stack<InnerTrieNode> stackOfParents = new Stack<>();
      boolean shouldAssignLeftSon = true;
      InnerTrieNode root = null;

      for (int i = 0; i < loadedItems.size(); i++) {
        TrieNode currentNode = loadedItems.get(i);
        if (i == 0) {
          // is root, skip
          stackOfParents.push((InnerTrieNode) currentNode);
          root = (InnerTrieNode) currentNode;
          continue;
        }

        if (stackOfParents.isEmpty()) {
          continue;
        }

        InnerTrieNode parent = stackOfParents.peek();

        if (parent.equals(root) && root.getLeftSon() != null) {
          shouldAssignLeftSon = false;
        }

        currentNode.setParent(parent);

        if (shouldAssignLeftSon) {
          shouldAssignLeftSon = false;
          parent.setLeftSon(currentNode);
        } else {
          parent.setRightSon(currentNode);
          // parent has both children, pop
          shouldAssignLeftSon = true;
          stackOfParents.pop();
        }

        if (currentNode instanceof InnerTrieNode) {
          // reset flag and put as next parent
          shouldAssignLeftSon = true;
          stackOfParents.push((InnerTrieNode) currentNode);
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
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

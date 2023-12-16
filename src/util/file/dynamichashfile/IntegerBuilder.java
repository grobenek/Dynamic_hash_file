package util.file.dynamichashfile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import util.file.IFileBuilder;

public class IntegerBuilder implements IFileBuilder<Integer> {
  private List<Integer> loadedItems;

  public IntegerBuilder() {
    this.loadedItems = new ArrayList<>();
  }

  @Override
  public void saveToFile(String pathToFile, List<Integer> itemsToSave) throws IOException {
    StringBuilder sb = new StringBuilder();

    for (Integer i : itemsToSave) {
      sb.append(i);
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

        Integer i = Integer.parseInt(line);
        loadedItems.add(i);
      }
    }
  }

  @Override
  public List<Integer> getLoadedData() {
    return loadedItems;
  }

  @Override
  public void clearLoadedData() {
    loadedItems = new ArrayList<>();
  }
}

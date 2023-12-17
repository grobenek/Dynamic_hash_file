package util.file.dynamichashfile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import structure.dynamichashfile.entity.record.Record;
import util.file.IFileBuilder;

public class DynamicHashFileInfoBuilder implements IFileBuilder<DynamicHashFileInfo> {
  private static final String DELIMITER = " ";
  private List<DynamicHashFileInfo> loadedItems;

  public DynamicHashFileInfoBuilder() {
    this.loadedItems = new ArrayList<>();
  }

  @Override
  public void saveToFile(String pathToFile, List<DynamicHashFileInfo> itemsToSave)
      throws IOException {
    StringBuilder sb = new StringBuilder();

    for (DynamicHashFileInfo dynamicHashFileInfo : itemsToSave) {
      sb.append(dynamicHashFileInfo.blockingFactorOfMainFile());
      sb.append(DELIMITER);
      sb.append(dynamicHashFileInfo.blockingFactorOfOverflowFile());
      sb.append(DELIMITER);
      sb.append(dynamicHashFileInfo.tClass().getName());
      sb.append(DELIMITER);
      sb.append(dynamicHashFileInfo.pathToMainFile());
      sb.append(DELIMITER);
      sb.append(dynamicHashFileInfo.pathToOverflowFile());
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

        int blockingFactorOfMainFile = Integer.parseInt(data[0]);
        int blockingFactorOfOverflowFile = Integer.parseInt(data[1]);
        Class<?> tClass = Class.forName(data[2]);
        String pathToMainFile = data[3];
        String pathToOverflowFile = data[4];

        loadedItems.add(
            new DynamicHashFileInfo(
                blockingFactorOfMainFile,
                blockingFactorOfOverflowFile,
                (Class<? extends Record>) tClass,
                pathToMainFile,
                pathToOverflowFile));
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<DynamicHashFileInfo> getLoadedData() {
    return loadedItems;
  }

  @Override
  public void clearLoadedData() {
    loadedItems = new ArrayList<>();
  }
}

package util.file;

import java.io.IOException;
import java.util.List;

public class IOManager<T> {
  IFileBuilder<T> fileBuilder;

  public IOManager(IFileBuilder<T> fileBuilder) {
    this.fileBuilder = fileBuilder;
  }

  public void saveToFile(String pathToFile, List<T> itemsToSave) throws IOException {
    fileBuilder.saveToFile(pathToFile, itemsToSave);
  }

  public void loadFromFile(String pathToFile) throws IOException {
    fileBuilder.loadFromFile(pathToFile);
  }
}

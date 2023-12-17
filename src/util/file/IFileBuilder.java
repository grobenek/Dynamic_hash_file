package util.file;

import java.io.IOException;
import java.util.List;

public interface IFileBuilder<T> {
  void saveToFile(String pathToFile, List<T> itemsToSave) throws IOException;

  void loadFromFile(String pathToFile) throws IOException;

  List<T> getLoadedData();

  void clearLoadedData();
}

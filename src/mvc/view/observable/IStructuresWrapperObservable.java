package mvc.view.observable;

import structure.dynamichashfile.DynamicHashFile;

public interface IStructuresWrapperObservable extends IObservable {
  DynamicHashFile<?>[] getHashFiles();
}

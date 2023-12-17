package util.file.dynamichashfile;

import structure.dynamichashfile.entity.record.Record;

public record DynamicHashFileInfo(
    int blockingFactorOfMainFile,
    int blockingFactorOfOverflowFile,
    Class<? extends Record> tClass,
    String pathToMainFile,
    String pathToOverflowFile) {}

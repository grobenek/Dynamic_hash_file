package structure.dynamichashfile;

import java.io.*;

public class LimitedString implements IConvertableToBytes<LimitedString> {
  private static final int CHAR_BYTE_SIZE = 2;
  private final int maxLength;
  private String string;

  public LimitedString(int maxLength, String string) {
    this.maxLength = maxLength;
    this.string = string;
  }

  public LimitedString() {
    this.maxLength = 0;
    this.string = "";
  }

  public static int getCharByteSize() {
    return CHAR_BYTE_SIZE;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public String getString() {
    return string.length() <= maxLength ? string : string.substring(0, maxLength);
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(maxLength);
      outputStream.writeBytes(getMaxStringLength());

      return byteArrayOutputStream.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  private String getMaxStringLength() {
    int lengthDifference = maxLength - getString().length();

    if (lengthDifference > 0) {
      string = string.concat("x".repeat(lengthDifference));
    }

    return string;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public LimitedString fromByteArray(byte[] byteArray) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {

      int maxRange = inputStream.readInt();
      String string = new String(inputStream.readNBytes(maxRange));

      return new LimitedString(maxRange, string);
    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LimitedString other)) {
      return false;
    }

    return maxLength == other.maxLength && getString().equals(other.getString());
  }
}

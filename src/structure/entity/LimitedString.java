package structure.entity;

import java.io.*;
import structure.dynamichashfile.constant.ElementByteSize;

public class LimitedString implements IConvertableToBytes {
  private static final int STATIC_ATTRIBUTES_BYTE_SIZE =
      2 * ElementByteSize.intByteSize(); // two ints
  public static final String FILLER = "x";
  private int maxLength;
  private String string;

  public LimitedString(int maxLength, String string) {
    this.maxLength = maxLength;
    this.string = string;
  }

  public LimitedString() {
    this.maxLength = 0;
    this.string = "";
  }

  public static int getStaticAttributesByteSize() {
    return STATIC_ATTRIBUTES_BYTE_SIZE;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public String getTruncatedString() {
    return string.length() <= maxLength ? string : string.substring(0, maxLength);
  }

  @Override
  public byte[] toByteArray() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {

      outputStream.writeInt(maxLength);
      outputStream.writeInt(string.length());
      outputStream.writeBytes(getAdjustedString());

      return byteArrayOutputStream.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  private String getAdjustedString() {
    int lengthDifference = maxLength - string.length();

    if (lengthDifference > 0) {
      return string.concat(FILLER.repeat(lengthDifference));
    }

    return string.substring(0, maxLength);
  }

  @Override
  public String toString() {
    return getTruncatedString();
  }

  @Override
  public void fromByteArray(byte[] byteArray) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {

      maxLength = inputStream.readInt();
      int stringLength = inputStream.readInt();
      string = new String(inputStream.readNBytes(stringLength));

    } catch (IOException e) {
      throw new IllegalStateException("Error during conversion to byte array.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LimitedString other)) {
      return false;
    }

    return maxLength == other.maxLength && getTruncatedString().equals(other.getTruncatedString());
  }
}

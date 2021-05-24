import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var lines = Files.readAllLines(Path.of("../input/day08.txt"));

    System.out.println("Part 1:");
    System.out.println(encodingOverhead(lines.stream()));

    System.out.println("Part 2:");
    System.out.println(encodingOverhead(lines.stream().map(DoubleEncoder::encode)));
  }

  static long encodingOverhead(Stream<String> entries) {
    return entries.map(ListEntryParser::parse).mapToLong(e -> e.codeLength - e.memoryLength).sum();
  }

  static List<String> testEntries = Arrays.asList("\"\"", "\"abc\"", "\"aaa\\\"aaa\"", "\"\\x27\"");

  record ListEntry(String entry, int codeLength, long memoryLength) { }

  static class ListEntryParser {
    public static ListEntry parse(String entry) {
      var unquoted = entry.substring(1, entry.length() - 1);
      // just put any character in place since we only care about the length
      unquoted = unquoted.replaceAll("\\\\\\\\", "!")
        .replaceAll("\\\\\\\"", "!")
        .replaceAll("\\\\x[0-9a-f]{2}", "!");
      return new ListEntry(entry, entry.length(), unquoted.length());
    }
  }

  static class DoubleEncoder {
    public static String encode(String s) {
      return String.format(
        "\"%s\"", s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"", "\\\\\\\"")
      );
    }
  }
}
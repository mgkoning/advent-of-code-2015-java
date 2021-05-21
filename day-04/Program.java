import java.security.MessageDigest;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.lang.StringBuilder;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {

  public static void main(String... args) throws java.io.IOException {
    var secret = Files.readString(Path.of("../input/day04.txt"));
    System.out.println("Part 1:");
    var part1 = findPrefix(secret, "00000", 0);
    System.out.println(part1.fst);
    System.out.println("Part 2:");
    var part2 = findPrefix(secret, "000000", part1.fst + 1);
    System.out.println(part2.fst);
  }

  private static Tuple<Integer, String> findPrefix(String salt, String desiredPrefix, int startAt) {
    var prefixSize = (int)ceil(desiredPrefix.length() / 2.0);
    return IntStream.iterate(startAt, i -> i+1)
      .mapToObj(i -> mine(salt, i, prefixSize))
      .filter(s -> s.snd.startsWith(desiredPrefix))
      .findFirst()
      .get();
  }

  private static Tuple<Integer, String> mine(String salt, int num, int prefixSize) {
    try {
      var md = MessageDigest.getInstance("MD5");
      var bytes = md.digest((salt + num).getBytes());
      var bob = new StringBuilder();
      for(int b = 0; b < prefixSize; b += 1) { bob.append(String.format("%02x", bytes[b])); }
      return new Tuple<>(num, bob.toString());
    } catch (java.security.NoSuchAlgorithmException x) { return null; }
  }

  record Tuple<T, U>(T fst, U snd) { }
}
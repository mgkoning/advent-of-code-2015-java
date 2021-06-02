import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;
import static java.util.Optional.*;

public class Program {
  static BiPredicate<Integer, Integer> lessThan = (x, y) -> x < y;
  static BiPredicate<Integer, Integer> moreThan = (x, y) -> x > y;
  static BiPredicate<Integer, Integer> equals = (x, y) -> x == y;

  public static void main(String... args) throws java.io.IOException {
    var inputLines = Files.readAllLines(Path.of("../input/day16.txt"));
    var sues = inputLines.stream()
      .map(SueParser::parse).map(Optional::orElseThrow).collect(toList());
    System.out.println("Part 1:");
    var targets = Map.of(
      "children", 3, "cats", 7, "samoyeds", 2, "pomeranians", 3, "akitas", 0,
      "vizslas", 0, "goldfish", 5, "trees", 3, "cars", 2, "perfumes", 1
    );
    var part1Matchers = Map.<String, BiPredicate<Integer, Integer>>of();
    System.out.println(
      sues.stream()
        .filter(s -> matches(targets, part1Matchers, s))
        .map(s -> s.number())
        .findFirst().orElseThrow()
    );
    System.out.println("Part 2:");
    var part2Matchers = Map.of("cats", moreThan, "trees", moreThan, "pomeranians", lessThan, "goldfish", lessThan);
    System.out.println(
      sues.stream()
        .filter(s -> matches(targets, part2Matchers, s))
        .map(s -> s.number())
        .findFirst().orElseThrow()
    );
  }

  public static boolean matches(
    Map<String, Integer> targets, Map<String, BiPredicate<Integer, Integer>> matchers, Sue sue
  ) {
    return sue.propertyCounts().entrySet().stream()
      .allMatch(
        e ->  matchers.getOrDefault(e.getKey(), equals).test(e.getValue(), targets.get(e.getKey()))
      );
  }
}

record Sue(int number, Map<String, Integer> propertyCounts) { }

record Tuple<T, U>(T first, U second) { }

class SueParser {
  static Pattern colonPattern = Pattern.compile(": ");
  static Pattern wordsPattern = Pattern.compile(" ");
  static Pattern propertiesPattern = Pattern.compile(", ");

  public static Optional<Sue> parse(String line) {
    var numberAndProperties = splitOnColon(line);
    return tryParseInt(words(numberAndProperties[0])[1])
      .map(number -> new Sue(number, properties(numberAndProperties[1])));
  }

  static String[] splitOnColon(String val) {
    return colonPattern.split(val, 2);
  }

  static String[] words(String sentence) {
    return wordsPattern.split(sentence);
  }

  static Map<String, Integer> properties(String spec) {
    return Arrays.stream(propertiesPattern.split(spec))
      .map(SueParser::splitOnColon)
      .map(parts -> tryParseInt(parts[1]).map(count -> new Tuple<>(parts[0], count)).orElseThrow())
      .collect(toMap(t -> t.first(), t -> t.second()));
  }

  static Optional<Integer> tryParseInt(String val) {
    try {
      return of(Integer.parseInt(val));
    } catch (NumberFormatException n) {
      return empty();
    }
  }
}
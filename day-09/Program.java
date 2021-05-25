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
    var lines = Files.readAllLines(Path.of("../input/day09.txt"));
    var distances = lines.stream()
      .map(DistanceParser::parse)
      .map(Optional::orElseThrow)
      .flatMap(d -> Stream.of(d, d.reverse()))
      .collect(toMap(d -> new Leg(d.from, d.to), d -> d.distance));
    System.out.println("Part 1:");
    var allLocations = distances.keySet().stream().map(d -> d.from).collect(toSet());
    var locationPermutations = getPermutations(allLocations);
    var journeyLengths = locationPermutations.stream()
      .map(p -> journeyLength(p, distances)).sorted().collect(toList());
    System.out.println(journeyLengths.get(0));
    System.out.println("Part 2:");
    System.out.println(last(journeyLengths).orElseThrow());
  }

  static <E> Optional<E> last(List<E> list) {
    return list.size() < 1 ? Optional.empty() : Optional.of(list.get(list.size() - 1));
  }

  static List<List<String>> getPermutations(Set<String> locations) {
    if (locations.isEmpty()) {
      return Arrays.asList(Arrays.<String>asList());
    }
    return locations.stream()
      .flatMap(
        l -> getPermutations(without(locations, l)).stream()
          .map(ls -> Stream.concat(Stream.of(l), ls.stream()).collect(toList()))
      )
      .collect(toList());
  }

  static int journeyLength(List<String> destinationsInOrder, Map<Leg, Integer> distance) {
    return StreamSupport.stream(
      zip(destinationsInOrder, destinationsInOrder.stream().skip(1).collect(toList()), Leg::new)
        .spliterator(),
      false
    ).map(leg -> distance.get(leg)).collect(summingInt(d -> d));
  }

  static <T, U, R> Iterable<R> zip(
    Iterable<T> left, Iterable<U> right, BiFunction<? super T, ? super U, R> combine
  ) {
    var zipIterator = new Iterator<R>() {
      Iterator<T> l = left.iterator();
      Iterator<U> r = right.iterator();
      public boolean hasNext() { return l.hasNext() && r.hasNext(); }
      public R next() { return combine.apply(l.next(), r.next()); }
    };
    return () -> zipIterator;
  }

  static <T> Set<T> without(Set<T> set, T member) {
    return set.stream().filter(t -> !t.equals(member)).collect(toSet());
  }

  record Distance(String from, String to, int distance) {
    public Distance reverse() { return new Distance(to, from, distance); }
  }

  record Leg(String from, String to) { }

  static class DistanceParser {
    private static Pattern linePattern = Pattern.compile("([a-zA-Z]+) to ([a-zA-Z]+) = (\\d+)");
    public static Optional<Distance> parse(String s) {
      var matcher = linePattern.matcher(s);
      return !matcher.matches() ? Optional.empty() :
        tryParse(matcher.group(3)).map(d -> new Distance(matcher.group(1), matcher.group(2), d));
    }

    static Optional<Integer> tryParse(String s) {
      try { return Optional.of(Integer.valueOf(s)); }
      catch (NumberFormatException n) { return Optional.empty(); }
    }
  }
}
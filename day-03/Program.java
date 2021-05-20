import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  record Coord(int x, int y) {
    public static Coord zero() { return new Coord(0, 0); }
    public static Coord add(Coord one, Coord other) {
      return new Coord(one.x + other.x, one.y + other.y);
    }
    public Coord add(Coord other) {
      return add(this, other);
    }
  }

  private static Map<Character, Coord> directionMap = Map.of(
    '<', new Coord(-1, 0),
    '>', new Coord(1, 0),
    '^', new Coord(0, -1), 
    'v', new Coord(0, 1)
  );

  record Tuple<T, U>(T fst, U snd) { }

  public static void main(String... args) throws java.io.IOException {
    var input = Files.readString(Path.of("../input/day03.txt"));
    System.out.println("Part 1:");
    System.out.println(allVisits(input.chars().mapToObj(c -> (char)c)).size());
    System.out.println("Part 2:");
    var pairs = getPairs(input);
    HashSet<Coord> santaVisits = allVisits(pairs.fst.stream()),
      robotVisits = allVisits(pairs.snd.stream());
    santaVisits.addAll(robotVisits);
    System.out.println(santaVisits.size());
  }

  private static HashSet<Coord> allVisits(Stream<Character> instructions) {
    return foldLeft(
      instructions.map(c -> directionMap.get(c)),
      new Tuple<>(Coord.zero(), new HashSet<Coord>()),
      (acc, next) -> {
        var newPos = acc.fst.add(next);
        acc.snd.add(newPos);
        return new Tuple<>(newPos, acc.snd);
      }
    ).snd;
  }

  private static Tuple<List<Character>, List<Character>> getPairs(String s) {
    ArrayList<Character> left = new ArrayList<>(), right = new ArrayList<>();
    var iterator = s.chars().mapToObj(c -> (char)c).iterator();
    while(iterator.hasNext()) {
      left.add(iterator.next());
      if (iterator.hasNext()) {
        right.add(iterator.next());
      }
    }
    return new Tuple<>(left, right);
  }

  private static void testIt() {
    System.out.println(foldLeft(Stream.of(1,2,3,4), "0", (s, i) -> s + i.toString()));
    scanLeft(Stream.of(1,2,3,4), "0", (s, i) -> s + i.toString()).forEach(s -> System.out.println(s));
  }

  public static <T, U> U foldLeft(Stream<T> stream, U seed, BiFunction<U, ? super T, U> accumulator) {
    var acc = seed;
    var iterator = stream.iterator();
    while(iterator.hasNext()) {
      acc = accumulator.apply(acc, iterator.next());
    }
    return acc;
  }

  /*
  C#:
  public static IEnumerable<U> ScanLeft<T, U>(IEnumerable<T> values, U seed, Func<U, T, U> accumulator) {
    yield return seed;
    var current = seed;
    foreach(var value in values) {
      current = accumulator(current, value);
      yield return current;
    }
  }
  */

  public static <T, U> Stream<U> scanLeft(Stream<T> stream, U seed, BiFunction<U, ? super T, U> accumulator) {
    var scanningIterator = new Iterator<U>() {
      Supplier<U> nextSupplier = () -> seed;
      BooleanSupplier hasNextSupplier = () -> true;
      Iterator<T> inner = stream.iterator();
      public boolean hasNext() { return this.hasNextSupplier.getAsBoolean(); }
      public U next() { 
        var result = nextSupplier.get();
        hasNextSupplier = () -> inner.hasNext();
        nextSupplier = () -> accumulator.apply(result, inner.next());
        return result;
      }
    };
    Iterable<U> scanningIterable = () -> scanningIterator;
    return StreamSupport.stream(scanningIterable.spliterator(), false);
  }
  // example usage:
  // scanLeft(Stream.of(1,2,3,4), "0", (s, i) -> s + i.toString()).forEach(s -> System.out.println(s));
  // output:
  // 0
  // 01
  // 012
  // 0123
  // 01234

}
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
  public static void main(String... args) throws java.io.IOException {
    var happinesses = Files.readAllLines(Path.of("../input/day13.txt"))
      .stream()
      .map(Program::parseHappiness)
      .collect(toList());
    System.out.println("Part 1:");
    System.out.println(solve(happinesses));
    System.out.println("Part 2:");
    System.out.println(solve(addMyself(happinesses)));
  }

  static long solve(List<Happiness> happinesses) {
    var happinessMap = happinesses.stream()
      .collect(toMap(h -> new Tuple<>(h.one(), h.other()), h -> h.change()));
    var personSet = happinesses.stream().map(h -> h.one()).collect(toSet());
    var personPermutations = permutations(personSet).stream()
      // repeat the first person at the end since the table is circular
      .map(p -> Stream.concat(p.stream(), Stream.of(p.get(0))).collect(toList()))
      .collect(toList());
    return personPermutations.stream()
      .mapToLong(p -> totalHappinessChange(happinessMap, p))
      .max()
      .getAsLong();
  }

  static long totalHappinessChange(
    Map<Tuple<String, String>, Long> happinessMap, List<String> people
  ) {
    return zip(people.stream(), people.stream().skip(1))
      .mapToLong(t -> happinessMap.get(t) + happinessMap.get(t.reverse()))
      .sum();
  }

  static String me = "Me";
  static List<Happiness> addMyself(List<Happiness> before) {
    var personSet = before.stream().map(h -> h.one()).collect(toSet());
    return Stream.concat(
      before.stream(),
      personSet.stream()
        .flatMap(p -> Stream.of(new Happiness(p, me, 0), new Happiness(me, p, 0)))
    ).collect(toList());
  }

  static <T> List<List<T>> permutations(Set<T> elements) {
    if (elements.isEmpty()) {
      return Arrays.asList(Arrays.<T>asList());
    }
    return elements.stream()
      .flatMap(
        l -> permutations(without(elements, l)).stream()
          .map(ls -> Stream.concat(Stream.of(l), ls.stream()).collect(toList()))
      )
      .collect(toList());
  }

  static <T> Set<T> without(Set<T> set, T member) {
    return set.stream().filter(t -> !t.equals(member)).collect(toSet());
  }

  static <T, U> Stream<Tuple<T, U>> zip(Stream<T> left, Stream<U> right) {
    Iterable<Tuple<T, U>> iterable = () -> new ZipIterator<>(left, right, Tuple::new);
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  static Happiness parseHappiness(String line) {
    // example: Alice would gain 54 happiness units by sitting next to Bob.
    var parts = line.split("[ .]");
    var factor = parts[2].equals("lose") ? -1 : 1;
    return new Happiness(parts[0], parts[10], factor * Integer.parseInt(parts[3]));
  }

}

record Happiness(String one, String other, long change) { }
record Tuple<T, U>(T first, U second) { 
  public Tuple<U, T> reverse() { return new Tuple<>(second, first); }
}

class ZipIterator<T, U, R> implements Iterator<R> {
  public ZipIterator(Stream<T> ts, Stream<U> us, BiFunction<? super T, ? super U, R> combine) {
    this.ts = ts.iterator();
    this.us = us.iterator();
    this.combine = combine;
  }

  Iterator<T> ts;
  Iterator<U> us;
  BiFunction<? super T, ? super U, R> combine;

  public boolean hasNext() { return ts.hasNext() && us.hasNext(); }

  public R next() { return combine.apply(ts.next(), us.next()); }
}
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
    var reindeer = Files.readAllLines(Path.of("../input/day14.txt"))
      .stream()
      .map(ReindeerParser::parse)
      .map(Optional::orElseThrow)
      .collect(toList());
    var contestLength = 2503;
    System.out.println("Part 1:");
    System.out.println(winnerAt(reindeer, contestLength).second());
    System.out.println("Part 2:");
    System.out.println(IntStream.rangeClosed(1, contestLength).parallel()
      .mapToObj(i -> winnerAt(reindeer, i).first())
      .collect(groupingBy(i -> i, counting()))
      .values().stream().mapToLong(i -> i)
      .max().orElseThrow()
    );

  }

  static Tuple<Reindeer, Long> winnerAt(List<Reindeer> reindeer, long time) {
    return reindeer.stream()
      .map(r -> new Tuple<>(r, r.distanceAt(time)))
      .sorted(Comparator.comparingLong((Tuple<Reindeer,Long> t) -> t.second()).reversed())
      .findFirst().orElseThrow();
  }

  static class ReindeerParser {
    static Pattern reindeerLine = Pattern.compile(
      "(\\p{IsLetter}+) can fly (\\d+) km/s for (\\d+) seconds, but then must rest for (\\d+) seconds."
    );
    public static Optional<Reindeer> parse(String line) {
      var matcher = reindeerLine.matcher(line);
      if (!matcher.matches()) { return empty(); }
      return tryParseLong(matcher.group(2))
        .flatMap(speed -> tryParseLong(matcher.group(3))
          .flatMap(sprintTime -> tryParseLong(matcher.group(4))
            .map(restTime -> new Reindeer(matcher.group(1), speed, sprintTime, restTime, sprintTime + restTime))
          )
        );
    }

    static Optional<Long> tryParseLong(String value) {
      try {
        return of(Long.parseLong(value));
      } catch (NumberFormatException n) {
        return empty();
      }
    }
  }
}

record Tuple<T, U>(T first, U second) { }

record Reindeer(String name, long speed, long sprintTime, long restTime, long period) {
  public long distanceAt(long when) {
    var fullPeriods = when / period;
    var partialPeriod = when % period;
    return (fullPeriods * sprintTime + Math.min(partialPeriod, sprintTime)) * speed;
  }
}
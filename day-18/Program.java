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
    var inputLines = Files.readAllLines(Path.of("../input/day18.txt"));
    var startSituation = activatedLights(inputLines);
    var maxBound = 99;
    System.out.println("Part 1:");
    var part1 = runConway(maxBound, startSituation, List.<Coord>of());
    System.out.println(part1.size());

    System.out.println("Part 2:");
    var faultyPart2 = List.of(
      new Coord(0, 0), new Coord(0, maxBound), new Coord(maxBound, 0), new Coord(maxBound, maxBound)
    );
    var part2 = runConway(maxBound, startSituation, faultyPart2);
    System.out.println(part2.size());
  }

  static Set<Coord> runConway(int maxBound, Set<Coord> startSituation, List<Coord> faulty) {
    return Stream.iterate(startSituation, previous -> nextLights(maxBound, faulty, previous))
      .skip(100).findFirst().orElseThrow();
  }

  static Set<Coord> nextLights(int maxBound, List<Coord> faulty, Set<Coord> previous) {
    return Stream.concat(
      Coord.allCoordsBetween(new Coord(0, 0), new Coord(maxBound, maxBound))
        .filter(c -> willBeOn(previous, c)),
      faulty.stream()
    )
    .collect(toSet());
  }

  static boolean willBeOn(Set<Coord> previous, Coord target) {
    var activatedNeighbours = target.neighbours().filter(n -> previous.contains(n)).count();
    return activatedNeighbours == 3 ||
      previous.contains(target) && activatedNeighbours == 2;
  }

  static Set<Coord> activatedLights(List<String> lines) {
    var result = new HashSet<Coord>();
    for(var y = 0; y < lines.size(); y++) {
      var line = lines.get(y);
      for(var x = 0; x < line.length(); x ++) {
        if (line.charAt(x) != '#') { continue; }
        result.add(new Coord(x, y));
      }
    }
    return result;
  }
}

record Coord(int x, int y) {
  public static Stream<Coord> allCoordsBetween(Coord from, Coord to) {
    return IntStream.rangeClosed(from.x, to.x).mapToObj(i -> i)
      .flatMap(x -> IntStream.rangeClosed(from.y, to.y).mapToObj(i -> i)
        .map(y -> new Coord(x, y)));
  }

  public Stream<Coord> neighbours() {
    return allCoordsBetween(new Coord(x - 1, y - 1), new Coord(x + 1, y + 1))
      .filter(neighbour -> neighbour.x != x || neighbour.y != y);
  }
}
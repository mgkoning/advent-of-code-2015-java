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
    var lines = Files.readAllLines(Path.of("../input/day06.txt"));
    var instructions = lines.stream()
      .map(InstructionParser::parse)
      .filter(Optional::isPresent)
      .map(Optional::orElseThrow)
      .collect(toList());
    System.out.println("Part 1:");
    Map<Coord, Integer> lights = new HashMap<>();
    for(var instruction: instructions) {
      for(var coord: allCoordsBetween(instruction.from, instruction.to).collect(toList())) {
        lights.put(coord, operationPart1(lights.getOrDefault(coord, 0), instruction.operation));
      }
    }
    System.out.println(brightness(lights));
    
    System.out.println("Part 1 (fold):");
    System.out.println(brightness(runInstructions(instructions, Program::operationPart1)));
    
    System.out.println("Part 2:");
    System.out.println(brightness(runInstructions(instructions, Program::operationPart2)));
  }

  static long brightness(Map<Coord, Integer> lights) {
    return lights.values().stream().collect(summingLong(i -> i));
  }

  static Map<Coord, Integer> runInstructions(
    List<Instruction> instructions, BiFunction<Integer, String, Integer> operation
  ) {
    return foldLeft(
      instructionsPerCoord(instructions),
      new HashMap<Coord, Integer>(),
      (acc, next) -> {
        acc.put(next.snd, operation.apply(acc.getOrDefault(next.snd, 0), next.fst));
        return acc;
      }
    );
  }

  static Stream<Tuple<String, Coord>> instructionsPerCoord(List<Instruction> instructions) {
    return instructions.stream()
      .flatMap(i -> 
        allCoordsBetween(i.from, i.to)
          .map(c -> new Tuple<>(i.operation, c)
        )
      );
  }

  static Integer operationPart1(Integer current, String operation) {
    return switch (operation) {
      case "turn on" -> 1;
      case "turn off" -> 0;
      case "toggle" -> 1 - current;
      default -> 0;
    };
  }
  
  static Integer operationPart2(Integer current, String operation) {
    return switch (operation) {
      case "turn on" -> current + 1;
      case "turn off" -> Math.max(0, current -1);
      case "toggle" -> current + 2;
      default -> current;
    };
  }

  static <T, U> U foldLeft(Stream<T> stream, U seed, BiFunction<U, ? super T, U> accumulator) {
    var acc = seed;
    var iterator = stream.iterator();
    while(iterator.hasNext()) {
      acc = accumulator.apply(acc, iterator.next());
    }
    return acc;
  }

  static Stream<Coord> allCoordsBetween(Coord from, Coord to) {
    return IntStream.range(from.x, to.x + 1)
        .mapToObj(i -> i)
        .flatMap(x -> IntStream.range(from.y, to.y + 1).mapToObj(y -> new Coord(x, y)));
  }

  record Tuple<T,U>(T fst, U snd) { }

  record Coord(int x, int y) { }

  record Instruction(String operation, Coord from, Coord to) { }

  static class InstructionParser {
    static Pattern pattern = Pattern.compile("^([a-z ]+) (\\d+),(\\d+) through (\\d+),(\\d+)$");
    public static Optional<Instruction> parse(String s) {
      var matcher = pattern.matcher(s);
      if (!matcher.matches()) { return Optional.empty(); }
      return toCoord(matcher, 2, 3)
        .flatMap(from -> 
          toCoord(matcher, 4, 5)
            .map(to -> new Instruction(matcher.group(1), from, to))
        );
    }

    static Optional<Coord> toCoord(Matcher matcher, int captureX, int captureY) {
      try {
        return Optional.of(new Coord(
          Integer.valueOf(matcher.group(captureX)), Integer.valueOf(matcher.group(captureY))
        ));
      } catch (NumberFormatException n) {
        return Optional.empty();
      }
    }
  }
}
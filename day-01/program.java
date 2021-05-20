import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class program {
  public static void main(String... args) throws java.io.IOException {
    var input = Files.readString(Path.of("../input/day01.txt"));
    IntUnaryOperator upOrDown = i -> i == '(' ? 1 : -1;
    var allChanges = input.chars().map(upOrDown).boxed().collect(Collectors.toList());
    System.out.println("Part 1:");
    System.out.println(allChanges.stream().collect(Collectors.summingInt(i -> i)));

    System.out.println("Part 2:");
 
    var result = 
      allChanges.stream().reduce(
        new Tuple<>(new ArrayList<Integer>(), 0), 
        (acc, next) -> {
          var currentFloor = acc.two + next;
          var newList = new ArrayList<Integer>(acc.one);
          newList.add(currentFloor);
          return Tuple.of(newList, currentFloor);
        },
        (a, b) -> b
      );
    System.out.println(result.one.stream().takeWhile(i -> i != -1).count() + 1);

    var sum = 0;
    for(int i: allChanges) {
      sum += i;
    }
    System.out.println(sum);
    var steps = 0;
    sum = 0;
    for(int i: allChanges) {
      steps += 1;
      sum += i;
      if (sum == -1) { break; }
    }
    System.out.println(steps);
  }

  record Tuple<S, T>(S one, T two) {
    public static <S, T> Tuple<S, T> of(S one, T two) {
      return new Tuple<S, T>(one, two);
    }
  }
}

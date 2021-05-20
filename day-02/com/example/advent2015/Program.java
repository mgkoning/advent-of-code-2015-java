package com.example.advent2015;

import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.*;
import static java.lang.Math.*;

public class Program {
  public static void main(String... args) throws java.io.IOException {
    var out = System.out;
    var presents = Files.lines(Path.of("../input/day02.txt"))
      .map(Program::toPresent).collect(toList());
    
    out.println("Part 1:");
    out.println(presents.stream().mapToInt(p -> p.requiredPaper()).sum());
    out.println("Part 2:");
    out.println(presents.stream().mapToInt(p -> p.requiredRibbon()).sum());
  }

  private static Present toPresent(String spec) {
    var dimensions = Arrays.stream(spec.split("x")).mapToInt(Integer::parseInt).toArray();
    return new Present(dimensions);
  }
}

class Present {
  Present(int[] sides) {
    l = sides[0];
    w = sides[1];
    h = sides[2];
    sortedSides = IntStream.of(sides).sorted().toArray();
  }
  private int l, w, h;
  private final int[] sortedSides;
  public int smallestSide() { return sortedSides[0] * sortedSides[1]; }
  public int requiredPaper() {
    return 2*l*w + 2*w*h + 2*h*l + smallestSide();
  }
  public int volume() { return w*h*l; }
  public int requiredRibbon() { 
    return volume() + 2*sortedSides[0] + 2*sortedSides[1];
  }
}
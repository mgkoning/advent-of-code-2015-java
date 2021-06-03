# Java solutions to Advent of Code 2015

Solutions in Java for the 2015 edition of [Advent of Code](https://adventofcode.com/2015).

Inputs should be placed in `/input/day%d2.txt`.

Running the solution for a specific day (except day 1 and 2):
```
cd day-%d2
javac Program.java && java Program
```

For day 1, use `javac program.java && java program`.

For day 2, use `./run.sh`.

For day 15, the default stack size is insufficient; this is due to the recursive nature of 
the `FList` type and the lack of optimizations for the recursive methods. Increase it to at least
17MiB using `java -Xss17m Program`.

The code base uses features from Java SDK 16.
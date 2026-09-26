Imagine a restaurant.

Normal Stream:
One waiter serves all tables one by one.

Parallel Stream:
Multiple waiters serve different tables simultaneously.

The restaurant manager is ForkJoinPool.

Work is divided among waiters and completed faster.



//play this code
// Online Java Compiler
// Use this editor to write, compile and run your Java code online
import java.util.Arrays;
import java.util.List;
class Main {
    public static void main(String[] args) {
        System.out.println("Start small. Ship something.");
        List<Integer>numbers=Arrays.asList(1,2,3,4,5,6,7,8);
       // numbers.parallelStream().forEachOrdered(System.out::println);
        numbers.parallelStream().forEach(n->System.out.println(Thread.currentThread().getName()+":" + n));
        numbers.parallelStream().forEachOrdered(System.out::println); 
    }
}

Start small. Ship something.
ForkJoinPool.commonPool-worker-1:3
ForkJoinPool.commonPool-worker-1:4
main:5
main:6
ForkJoinPool.commonPool-worker-1:1
ForkJoinPool.commonPool-worker-1:2
main:7
main:8
1
2
3
4
5
6
7
8
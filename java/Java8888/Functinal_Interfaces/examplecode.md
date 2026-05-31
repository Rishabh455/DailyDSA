import java.util.function.Predicate;
import java.util.function.Function;import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.Random;
class Main {
    public static void main(String[] args) {
        Predicate<Integer>isEven=n->n%2==0;
        System.out.println(isEven.test(3));
        Predicate <Integer>isGreater=n->n>100;
        Predicate<Integer>ornot=n->n>=89;
        Predicate<Integer> club=isEven.and(isGreater).or(ornot);
        //System.out.println(club.negate(89));
        Function<String,Integer> getLength=str->str.length();
        Consumer<String>print= str->System.out.println(str);
        print.accept("hey there you!");
        System.out.println(getLength.apply("Javaeeeeeee"));
        Supplier<Integer> random=()->new Random().nextInt(100);
        System.out.println(random.get());
    }
}


//paste it in online copiler for practise
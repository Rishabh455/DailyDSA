
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

class Main {
    public static void main(String[] args) {

        int num[]={1,2,2,3,3,4,4,5,5,5,5,6,76};
        List<Integer>num=Arrays.asList(1,1,1,2,3,3,3,4,4,5,5,6,6,6,6,6);
 Map<Integer,Long>map=num.stream().collect(
    Collectors.groupingBy(Function.identity(),Collectors.counting()));
    System.out.println(map);

        
        System.out.println("Try clicking the Run button.");
    }
}
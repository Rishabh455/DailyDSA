//package java.javaPractise;

public class Entry {
    public static void main(String[] args) {
        Calculator calculator = new Calculator(5, 10);
        int result = calculator.add();
        System.out.println("The sum is: " + result);
    }
}

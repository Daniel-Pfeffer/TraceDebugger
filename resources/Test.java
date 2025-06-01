import java.lang.IllegalArgumentException;

class Test {
    public static void main(String[] args) {
        int i = 2;
        Inner inner = new Inner(i);
        i = inc(); // Should be 0
        System.out.println(i);
        i++;
        var x = methodWith2Args(2, 3.0);
        Object y = "Hello World!";
        String xy = "Hello World!";
        inner.x = 4;
        x += 3;
        x *= 2;
        x /= 2;
        x -= 3;
        x++;
        x--;
        if (i < 10) {
            // throw new IllegalArgumentException("i is less than 10");
            i = 5;
        } else {
            i = 6;
        }
        for (int j = 0; j < 10; j++) {
            i = 7;
        }
        print("Hello, World!" + i);
        print("" + inner.x);
        varargsMethod("Hello", "World", "from", "varargs");
    }

    static int globalCnt = 0;

    public static int inc() {
        var temp = globalCnt;
        globalCnt = globalCnt + 1;
        return temp;
    }

    public static double methodWith2Args(int a, double b) {
        return a + b;
    }

    static class Inner {
        int x;

        public Inner(int x) {
            this.x = x;
        }
    }

    public static void print(String a) {
        System.out.println(a);
    }

    public static void varargsMethod(String... args) {
        for (String arg : args) {
            print(arg);
        }
    }
}
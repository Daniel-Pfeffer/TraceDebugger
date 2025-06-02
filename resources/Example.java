public class Example {
    static int globalCnt = 0;
    static int k = 0;

    public static void main(String[] args) {
        int i = 2; // i = 2

        i += i++ + 2; // i = 2 + 2 + 2 = 6
    }

    public static int inc() {
        return globalCnt++;
    }

    static class Inner {
        int x;

        public Inner(int x) {
            this.x = x;
        }
    }
}
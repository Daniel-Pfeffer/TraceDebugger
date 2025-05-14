class Test {
    public static void main(String[] args) {
        int i = 2;
        Inner inner = new Inner(i);
        i = 3;
        System.out.println("Hello, World!" + i);
        System.out.println(inner.x);
    }


    static class Inner {
        int x;

        public Inner(int x) {
            this.x = x;
        }
    }
}
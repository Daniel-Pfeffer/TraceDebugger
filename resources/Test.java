class Test {
    public static void main(String[] args) {
        int i = 2;
        Inner inner = new Inner(i);
        i = 3;
        print("Hello, World!" + i);
        print(""+inner.x);
    }

    public static void print(String a){
        System.out.println(a);
    }


    static class Inner {
        int x;

        public Inner(int x) {
            this.x = x;
        }
    }
}
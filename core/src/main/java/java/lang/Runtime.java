package java.lang;

public class Runtime {
    private static Runtime currentRuntime = new Runtime();

    public static Runtime getRuntime(){
        return currentRuntime;
    }

    public int availableProcessors(){
        return 1;
    }
}

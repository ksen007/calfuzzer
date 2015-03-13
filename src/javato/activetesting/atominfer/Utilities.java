package javato.activetesting.atominfer;

public class Utilities {

    public static boolean shouldPrint() {
	String val = System.getProperty("javato.activetesting.atominfer.disable_print");
	return val == null;
    }
    
}
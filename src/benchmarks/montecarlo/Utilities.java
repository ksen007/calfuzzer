/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         *
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package benchmarks.montecarlo;
/**
  * Utility methods, all static.
  *
  * @author H W Yau.
  * @version $Revision: 1.4 $ $Date: 1999/02/16 18:53:59 $
  */
public final class Utilities {
  public static boolean DEBUG=false;
  private static String className="Utilities";

  /**
    * Static method which behaves like the Unix `which' command.  OS
    * specific dependencies are handled by the Java.lang.System
    * properties.
    *
    * @param executable The executable to search for.
    * @param pathEnv    The list of paths in which to search, in the style of the
    *                   OS's PATH environment variable.
    * @return           The full pathname of where the executable lives,
    *                   or failing that the error message "<executable> not found.".
    */
  public static String which(String executable, String pathEnv) {
    String executablePath;
    String paths[];

    paths=splitString(System.getProperty("path.separator"),pathEnv);
    for(int i=0; i<paths.length; i++ ) {
      if( paths[i].length() > 0 ) {
	java.io.File pathFile=new java.io.File(paths[i]);
	if( pathFile.isDirectory() ) {
	  String filesInDirectory[];
	  filesInDirectory=pathFile.list();
	  for(int j=0; j<filesInDirectory.length; j++ ) {
	    if( DEBUG ) {
	      System.out.println("DBG: Matching "+filesInDirectory[j]);
	    }
	    if( filesInDirectory[j].equals(executable) ) {
	      executablePath=paths[i]+System.getProperty("file.separator")+executable;
	      return executablePath;
	    }
	  }
	} else {
	  if( DEBUG ) {
	    System.out.println("DBG: path "+paths[i]+" is not a directory!");
	  }
	}
      }
    } /* for i */
    executablePath=executable+" not found.";
    return executablePath;
  }

  /**
    * Static method which behaves like the Perl join() function.
    *
    * @param joinChar    The character on which to join.
    * @param stringArray The array of strings to join.
    * @return            A string of the joined string array.
    */
  public static String joinString(String joinChar,String stringArray[]) {
    return joinString(joinChar,stringArray,0);
  }
  /**
    * Static method which behaves like the Perl join() function.
    *
    * @param joinChar    The character on which to join.
    * @param stringArray The array of strings to join.
    * @param index       The array index on which to start joining.
    * @return            A string of the joined string array.
    */
  public static String joinString(String joinChar,String stringArray[],int index) {
    String methodName="join";
    StringBuffer tmpString;

    int nStrings = java.lang.reflect.Array.getLength(stringArray);
    if( nStrings <= index ) {
      tmpString = new StringBuffer();
    } else {
      tmpString = new StringBuffer(stringArray[index]);
      for(int i=(index+1); i < nStrings; i++) {
	tmpString.append(joinChar).append(stringArray[i]);
      }
    }
    return tmpString.toString();
  }
  /**
    * Static method which behaves like the Perl split() function.
    *
    * @param splitChar The character on which to split.
    * @param arg       The string to be split.
    * @return          A string array of the split string.
    */
  public static String[] splitString(String splitChar,String arg) {
    String methodName="split";

    String myArgs[];
    int nArgs=0;
    int foundIndex=0, fromIndex=0;
    
    while( (foundIndex=arg.indexOf(splitChar,fromIndex)) > -1 ) {
      nArgs++;
      fromIndex=foundIndex+1;
    }
    if( DEBUG ) {
      System.out.println("DBG "+className+"."+methodName+": "+nArgs);
    }
    myArgs = new String[nArgs+1];
    nArgs=0;
    fromIndex=0;
    while( (foundIndex=arg.indexOf(splitChar,fromIndex)) > -1 ) {
      if( DEBUG ) {
	System.out.println("DBG "+className+"."+methodName+": "+fromIndex+" "+foundIndex);
      }
      myArgs[nArgs]=arg.substring(fromIndex,foundIndex);
      nArgs++;
      fromIndex=foundIndex+1;
    }
    myArgs[nArgs]=arg.substring(fromIndex);
    return myArgs;
  }
}


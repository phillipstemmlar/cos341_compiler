import java.io.*;
import java.util.*; 

public class Helper{

	public static Boolean logging = false;
	public static Boolean erroring = true;
	public static Boolean successing = false;

	public static String filetoString(String filename){
		String file = "";
		try {
			File srcFile = new File(filename);
			Scanner srcReader = new Scanner(srcFile);

			while(srcReader.hasNextLine()){
				String line = srcReader.nextLine();
				file += line;
				if(srcReader.hasNextLine()) file += "\n";
			}
			srcReader.close();
			
    } catch (FileNotFoundException e) {
      System.out.println("File could not be read: " + filename);
      e.printStackTrace();
		}
		return file;		
	}

	public static void writeToFile(String filename, String content){	
		if(content.length() == 0) return;
		try {
			FileWriter myWriter = new FileWriter(filename);		
      myWriter.write(content);
			myWriter.close();	
    } catch (IOException e) {
      System.out.println("File could not be written to: " + filename);
			e.printStackTrace();		
    }
	}

	public static String removeLeadingWhitespace(String str){
		while(str.charAt(0) == ' ') str = str.substring(1);
		return str;
	}

	public static int getLineNumber(String input, int charIndex){
		int line = 1;
		for(int i = 0; i < charIndex && i < input.length(); ++i){
			if(input.charAt(i) == '\n') line++;
		}
		return line;
	}

	public static int getColumnNumber(String input, int charIndex){
		int col = 0;
		for(int i = 0; i < charIndex && i < input.length(); ++i){
			if(input.charAt(i) == '\n') col = 0;
			else col++;
		}
		return col + 1;
	}

	private static String boldQuotes(String msg){
		Boolean first = true;
		String newMsg = "";
		char[] msgc = msg.toCharArray();
		for (int i = 0; i < msgc.length; ++i){
			char c = msgc[i];
			String cstr = c + "";
			if(c == '\"'){
				cstr = (first? c + bold: red + c);
				first = !first;
			}
			newMsg += cstr;
		}
		return newMsg;
	}

	public static void error(String prefix, int line, int col, String msg){
		if(erroring)System.out.println(red + prefix + " [line:" + line + ", col:" + col + "]:\n\t" + boldQuotes(msg) + white);
	}

	public static void error(String msg){
		if(erroring)System.out.println(red + boldQuotes(msg) + white);
	}

	public static void success(String str){
		if(successing) System.out.println(green + str + white);
	}

	public static void logln(String str){
		if(logging) System.out.println(str);
	}

	public static void log(String str){
		if(logging) System.out.print(str);
	}

	public static void line(char c){
		if(logging){
			String ln = "";
			for(int i = 0; i < 30; ++i) ln += c;
			logln(ln);
		}
	}

	public static void line(){
		line('=');
	}

	public static final String bold = "\033[0;1m";
	public static final String red = "\033[0;31m";
	public static final String green = "\033[0;32m";
	public static final String blue = "\033[0;34m";
	public static final String white = "\033[0;37m";
	public static final String grey = "\033[1;30m";

}
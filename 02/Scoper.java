public class Scoper {
	private static String PREFIX;
	
	public HashMap<Integer, Scope> scopeTable;

	public Scoper(String prefix){
		PREFIX = prefix;
	}
	
	public void execute(SyntaxNode syntaxTree){
		if(syntaxTree == null) return;
			
	}

}
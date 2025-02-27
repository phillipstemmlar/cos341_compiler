import java.util.*;

public class Compiler {
	private static String inputfile;
	private static final String Lexer_output_file = "Lexer_Tokens_output.txt";
	private static final String Parser_SyntaxTree_vis_output_fle = "Parser_SyntaxTree_Visualized_output.txt";
	private static final String Parser_SyntaxTree_output_file = "Parser_SyntaxTree_output.txt";
	private static final String Parser_SymbolTable_output_file = "Parser_SymbolTable_output.txt";

	private static final String Scoper_SyntaxTree_vis_output_fle = "Scoper_SyntaxTree_Visualized_output.txt";
	private static final String Scoper_SyntaxTree_output_file = "Scoper_SyntaxTree_output.txt";
	private static final String Scoper_SymbolTable_output_file = "Scoper_SymbolTable_output.txt";

	private static final String Typer_SyntaxTree_vis_output_fle = "Typer_SyntaxTree_Visualized_output.txt";
	private static final String Typer_SyntaxTree_output_file = "Typer_SyntaxTree_output.txt";
	private static final String Typer_SymbolTable_output_file = "Typer_SymbolTable_output.txt";

	private static final String LEXER_PREFIX = "Lexical Error";
	private static final String PARSER_PREFIX = "Syntax Error";
	private static final String SCOPER_PREFIX = "Scoping Error";
	private static final String TYPE_PREFIX = "Type Error";
	private static final String VALUE_PREFIX = "Value Error";

	public static void main(String[] args) {
		if(args.length >= 1){
			Compiler comp = new Compiler();
			inputfile = args[0];
			comp.compile();
		}else{
			Helper.error("ERROR: Missing source file as input (1st argument)");
		}
	}
	
	private Lexer lexer;
	private Parser parser;
	private SemanticAnalyzer semanticAnalyzer;

	public List<Token> tokensQ = null;
	public SyntaxNode syntaxTree = null;
	public HashMap<Integer, SyntaxNode> symbolTree = null;
	HashMap<Integer, Scope> scopeTable = null;

	private Compiler(){
		lexer = new Lexer(LEXER_PREFIX);
		parser = new Parser(PARSER_PREFIX);
		semanticAnalyzer = new SemanticAnalyzer(SCOPER_PREFIX,TYPE_PREFIX,VALUE_PREFIX);
	}

	public void compile(){
		tokensQ = lexer.executeToFile(inputfile,Lexer_output_file);
		if(tokensQ != null){
			symbolTree = parser.executeToFile(tokensQ, Parser_SyntaxTree_output_file, Parser_SymbolTable_output_file, Parser_SyntaxTree_vis_output_fle);
			if(symbolTree != null){
				syntaxTree = parser.syntaxTree;
				scopeTable = semanticAnalyzer.executeScopeToFile(symbolTree, Scoper_SyntaxTree_output_file, Scoper_SymbolTable_output_file, Scoper_SyntaxTree_vis_output_fle);
				if(scopeTable != null){
					scopeTable = semanticAnalyzer.executeTypeToFile(symbolTree, Typer_SyntaxTree_output_file, Typer_SymbolTable_output_file, Typer_SyntaxTree_vis_output_fle);
					if(scopeTable != null){}
				}
			}
		}
	}

}
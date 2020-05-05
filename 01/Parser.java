import java.util.*;

public class Parser {
	public static String PREFIX = "Syntax Error";
	public static Boolean lexer_output = false;
	public static String lexer_output_file =  "lexer_output.txt";

	private Lexer lex = null;

	public SyntaxNode syntaxTree = null;

	public static void main(String[] args) {
		String inputfile = args[0];
		String outputfile = args[1];

		Parser LL1 = new Parser();
		Boolean success = LL1.executeToFile(inputfile, outputfile, lexer_output);
	}

	public Parser(){
		lex = new Lexer();
	}

	public Boolean execute(String inputfile, Boolean outputLexer){
		Boolean success = false;
		if(outputLexer) {
			success = lex.executeToFile(inputfile,lexer_output_file);
		}else{
			success = lex.execute(inputfile);
		}
		if(success){
			syntaxTree = parse(lex.tokensQ);
			if(syntaxTree != null) return true;
		}
		return false;
	}

	public Boolean executeToFile(String inputfile, String outputfile, Boolean outputLexer){
		Boolean success = execute(inputfile, outputLexer);
		if(success){
			exportSyntaxNodeToFile(syntaxTree, outputfile);
			return true;
		}
		return false;
	}

	private void exportSyntaxNodeToFile(SyntaxNode root, String filename){
		if(root != null){
			Helper.writeToFile(filename, root.treeString());
		}
	}

	public SyntaxNode parse(List<Token> inputTokensQ){

		inputTokensQ.add((Token.END()));
		Token[] inputTokens = new Token[inputTokensQ.size()];
		for(int inp = 0; inp < inputTokensQ.size(); ++inp){
			inputTokens[inp] = inputTokensQ.get(inp);
		}

		//LL parser table
		HashMap<Token.eToken, HashMap<Token.eToken, Integer> > table = genLL1_table();
		Stack<SyntaxNode> stack = new Stack<>(); 

		SyntaxNode root = new CompositeSyntaxNode(Token.eToken.PROG, "--PROG--");
		//Initialize symbol stack
		stack.push(new LeafSyntaxNode(Token.eToken.END,"--$--"));
		stack.push(root);

		//Initialize symbol index
		Integer p = 0;

		try{
		while(stack.size() > 0){
			if(p < inputTokens.length && inputTokens[p].get() == stack.peek().get()){
				Helper.logln("Matched symbol: " + inputTokens[p].get());
				Helper.logln("------|" + inputTokens[p].str() + "|------");
				if(stack.peek().isLeaf()){
					((LeafSyntaxNode)stack.peek()).val(inputTokens[p].str());
				}		
				p++;
				stack.pop();
			}else if(p >= inputTokens.length || table.get(stack.peek().get()) == null || table.get(stack.peek().get()).get(inputTokens[p].get()) == null){
				// System.out.println(green + "ERROR ENTERED 1" + white);

				int line = (p >= 1)? inputTokens[p-1].row : 0;
				int col = (p >= 1)? inputTokens[p-1].col : 0;

				// System.out.println(blue + stack.peek().get() + white);

				String err = stack.peek().error();
				String found = (stack.peek().includeFound())? " \"" + Helper.bold + inputTokens[p].str() + Helper.red + "\"" : "" ;

				Helper.error(PREFIX,line, col, err + found);
				// Helper.logln(blue + Arrays.toString(stack.toArray()) + white);

				return null;
			}else{
				Integer rule = table.get(stack.peek().get()).get(inputTokens[p].get());

				if(rule == 14 && p+1 < inputTokens.length && inputTokens[p+1].get() == Token.eToken.tok_assignment){
					rule = 15;
					// Helper.logln("==========Rule 15===========");
				}

				SyntaxNode top = null;
				Helper.logln("Rule: " + rule);
				switch(rule){
					case 1:	// PROG → CODE PROC_DEFS_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS_PART, "--proc-defs-part--"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, ""));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 2:	// PROC_DEFS_PART → ; PROC_DEFS
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS, "--proc-defs--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Expected \";\" after instruction but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 3:	// PROC_DEFS_PART → ε
						stack.pop();
						break;

					case 4:// PROC_DEFS → PROC PROC_DEFS_PART2
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS_PART2, "--proc-defs-part2--"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC, "--proc--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 5:// PROC_DEFS_PART2 → PROC_DEFS
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS, "--proc-defs--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 6:	// PROC_DEFS_PART2 → ε
						stack.pop();
						break;

					case 7:// PROC → proc userDefinedIdentifier { PROG }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROG, "--prog--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected after \"proc\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_proc, "Invalid token, expected \"proc\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 8:// CODE → INSTR CODE_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE_PART, "Expected \";\" after instruction but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.INSTR, "--instr--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 9:// CODE_PART → ; CODE
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild((new CompositeSyntaxNode(Token.eToken.CODE, "The last instruction of a code block should not end with a \";\"")).setIncludeFound(false));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Expected \";\" after instruction but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 10:// CODE_PART → ε
						stack.pop();
						break;

					case 11:// INSTR → halt
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_halt, "Invalid token, expected \"halt\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 12:// INSTR → DECL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.DECL, "--decl--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 13:// INSTR → IO
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.IO, "--io--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 14:// INSTR → CALL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CALL, "--call--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 15:// INSTR → ASSIGN
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.ASSIGN, "--assign--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 16:// INSTR → COND_BRANCH
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.COND_BRANCH, "--cond-branch--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 17:// INSTR → COND_LOOP
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.COND_LOOP, "--cond-loop--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 18:// IO → input ( VAR ), ""
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected ')' but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_input, "Invalid token, expected \"input\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 19:// IO → output ( VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected ')' but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_output, "Invalid token, expected \"output\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 20:// DECL → TYPE NAME DECL_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.DECL_PART, "--decl-part--"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NAME, "User defined identifier expected but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.TYPE, "Expected a valid type but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 21:// DECL_PART → ; DECL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.DECL, "--decl--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Expected \";\" after decleration but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 22:// DECL_PART → ε
						stack.pop();
						break;

					case 23:// TYPE → num
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_num, "Expected a valid type but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 24:// TYPE → string
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_string, "Expected a valid type but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 25:// TYPE → bool
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_bool, "Expected a valid type but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 26:// CALL → userDefinedIdentifier
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 27:// NAME → userDefinedIdentifier
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 28:// VAR → userDefinedIdentifier
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 29:// ASSIGN → VAR = VALUE_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VALUE_PART, "--value-part--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 30:// VALUE_PART → stringLiteral
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_string_literal, "String literal expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 31:// VALUE_PART → VAR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 32:// VALUE_PART → NUMEXPR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 33:// VALUE_PART → BOOL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 34:// NUMEXPR → VAR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 35:// NUMEXPR → integerLiteral
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 36:// NUMEXPR → CALC
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CALC, "--calc--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 37:// CALC → add ( NUMEXPR , NUMEXPR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_add, "Invalid token, expected \"add\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 38:// CALC → sub ( NUMEXPR , NUMEXPR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_sub, "Invalid token, expected \"sub\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 39:// CALC → mult ( NUMEXPR , NUMEXPR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_mult, "Invalid token, expected \"mult\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 40:// COND_BRANCH → if ( BOOL ) then { CODE } ELSE_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.ELSE_PART, "--else-part--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_then, "Invalid token, expected \"then\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_if, "Invalid token, expected \"if\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 41:// ELSE_PART → else { CODE }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_else, "Invalid token, expected \"else\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 42:// ELSE_PART → ε
						stack.pop();
						break;



					case 43:// BOOL → eq ( VAR , VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_eq, "Invalid token, expected \"e1\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 44:// BOOL → ( VAR BOOL2
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL2, "--bool2--"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 45:// BOOL2 → < VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_less_than, "Invalid token, expected \"<\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 46:// BOOL2 → > VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_greater_than, "Invalid token, expected \">\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 47:// BOOL → not BOOL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_not, "Invalid token, expected \"not\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 48:// BOOL → and ( BOOL , BOOL )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_and, "Invalid token, expected \"and\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 49:// BOOL → or ( BOOL , BOOL )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_or, "Invalid token, expected \"or\" but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 50:// BOOL → T
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_T, "Boolean literal expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 51:// BOOL → F
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_F, "Boolean literal expected but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 52:// BOOL → VAR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 53:// COND_LOOP → while ( BOOL ) { CODE }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_while, "--while--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 54:// COND_LOOP → for ( VAR = 0 ; VAR < VAR ; VAR = add ( VAR , 1 ) ) { CODE }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_add, "Invalid token, expected \"add\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Invalid token, expected ';' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_less_than, "Invalid token, expected \"<\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Invalid token, expected ';' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_for, "--for--"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					default:
						Helper.error("Parsing table defaulted");
						return null;
				};
			}
		}
		return root;

		}catch(NullPointerException e){

			// Helper.error("=======================================================");
			// System.out.print(blue);

			// System.out.println(e);
			// System.out.println("p:  \t\t" + p);
			// System.out.println(inputTokens[p].get() + " - " + inputTokens[p].str());
			// System.out.println(Arrays.toString(stack.toArray()));

			// //}else if(p >= inputTokens.length || table.get(stack.peek().get()).get(inputTokens[p].get()) == null){
			// System.out.print(green);

			// System.out.println(stack.peek().get());
			// System.out.println(table.get(stack.peek().get()));

			// Helper.error("=======================================================");

		}
		return null;
	}

	private HashMap<Token.eToken, HashMap<Token.eToken, Integer> > genLL1_table(){
		HashMap<Token.eToken, HashMap<Token.eToken, Integer> > table = new HashMap<>();
		/*
		PROG
			halt num string bool input output uDefLIt if while for
		*/
		table.put(Token.eToken.PROG, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.PROG).put(Token.eToken.tok_halt, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_num, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_string, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_bool, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_input, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_output, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_user_defined_identifier, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_if, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_while, 1);
		table.get(Token.eToken.PROG).put(Token.eToken.tok_for, 1);
		
		/*
		PROC_DEFS_PART
			; ε	
			$ }
		*/
		table.put(Token.eToken.PROC_DEFS_PART, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.PROC_DEFS_PART).put(Token.eToken.tok_semi_colon, 2);

		table.get(Token.eToken.PROC_DEFS_PART).put(Token.eToken.tok_close_brace, 3);
		table.get(Token.eToken.PROC_DEFS_PART).put(Token.eToken.END, 3);

		/*
		PROC_DEFS
			proc
		*/
		table.put(Token.eToken.PROC_DEFS, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.PROC_DEFS).put(Token.eToken.tok_proc, 4);

		/*
		PROC_DEFS_PART2
			proc ε
			$ }
		*/		
		table.put(Token.eToken.PROC_DEFS_PART2, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.PROC_DEFS_PART2).put(Token.eToken.tok_proc, 5);

		table.get(Token.eToken.PROC_DEFS_PART2).put(Token.eToken.tok_close_brace, 6);
		table.get(Token.eToken.PROC_DEFS_PART2).put(Token.eToken.END, 6);
		
		/*
		PROC
			proc
		*/	
		table.put(Token.eToken.PROC, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.PROC).put(Token.eToken.tok_proc, 7);
		
		/*
		CODE	
			halt num string bool input output uDefLIt if while for	
			; $ }
		*/
		table.put(Token.eToken.CODE, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.CODE).put(Token.eToken.tok_halt, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_num, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_string, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_halt, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_input, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_output, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_user_defined_identifier, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_if, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_while, 8);
		table.get(Token.eToken.CODE).put(Token.eToken.tok_for, 8);

		//TEMP Cheat
		table.get(Token.eToken.CODE).put(Token.eToken.tok_proc, 4);
		// table.get(Token.eToken.CODE).put(Token.eToken.tok_semi_colon, 2);

		/*
		CODE_PART	
			; ε	
			; $ }
		*/
		table.put(Token.eToken.CODE_PART, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.CODE_PART).put(Token.eToken.tok_semi_colon, 9);
		
		table.get(Token.eToken.CODE_PART).put(Token.eToken.tok_close_brace, 10);
		table.get(Token.eToken.CODE_PART).put(Token.eToken.END, 10);

		/*
		INSTR	
			halt num string bool input output uDefLIt if while for
		*/
		table.put(Token.eToken.INSTR, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_halt, 11);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_num, 12);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_string, 12);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_bool, 12);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_input, 13);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_output, 13);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_user_defined_identifier, 14);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_if, 16);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_while, 17);
		table.get(Token.eToken.INSTR).put(Token.eToken.tok_for, 17);

		/*
		IO	
			input output	
		*/
		table.put(Token.eToken.IO, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.IO).put(Token.eToken.tok_input, 18);
		table.get(Token.eToken.IO).put(Token.eToken.tok_output, 19);
		
		/*
		DECL	
			num string bool	
		*/
		table.put(Token.eToken.DECL, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.DECL).put(Token.eToken.tok_num, 20);
		table.get(Token.eToken.DECL).put(Token.eToken.tok_string, 20);
		table.get(Token.eToken.DECL).put(Token.eToken.tok_bool, 20);

		//TEMP Cheat
		table.get(Token.eToken.DECL).put(Token.eToken.tok_user_defined_identifier, 29);

		/*
		DECL_PART	
			; ε	
			; $ }
		*/
		table.put(Token.eToken.DECL_PART, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.DECL_PART).put(Token.eToken.tok_semi_colon, 21);
		
		table.get(Token.eToken.DECL_PART).put(Token.eToken.tok_close_brace, 22);
		table.get(Token.eToken.DECL_PART).put(Token.eToken.END, 22);

		/*
		TYPE	
			num string bool	
		*/
		table.put(Token.eToken.TYPE, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.TYPE).put(Token.eToken.tok_num, 23);
		table.get(Token.eToken.TYPE).put(Token.eToken.tok_string, 24);
		table.get(Token.eToken.TYPE).put(Token.eToken.tok_bool, 25);

		/*
		CALL	
			uDefLIt	
		*/
		table.put(Token.eToken.CALL, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.CALL).put(Token.eToken.tok_user_defined_identifier, 26);

		/*
		NAME	
			uDefLIt	
		*/
		table.put(Token.eToken.NAME, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.NAME).put(Token.eToken.tok_user_defined_identifier, 27);

		/*
		VAR	
			uDefLIt	
		*/
		table.put(Token.eToken.VAR, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.VAR).put(Token.eToken.tok_user_defined_identifier, 28);

		/*
		ASSIGN	
			uDefLIt	
		*/
		table.put(Token.eToken.ASSIGN, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.ASSIGN).put(Token.eToken.tok_user_defined_identifier, 29);

		/*
		VALUE_PART	
			strLit uDefLIt intLit add sub mult eq ( not and or T F
		*/
		table.put(Token.eToken.VALUE_PART, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_string_literal, 30);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_user_defined_identifier, 31);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_integer_literal, 32);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_add, 32);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_sub, 32);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_mult, 32);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_eq, 33);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_open_parenth, 33);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_not, 33);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_and, 33);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_or, 33);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_T, 33);
		table.get(Token.eToken.VALUE_PART).put(Token.eToken.tok_F, 33);

		/*
		NUMEXPR	
			uDefLIt intLit add sub mult	
		*/
		table.put(Token.eToken.NUMEXPR, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.NUMEXPR).put(Token.eToken.tok_user_defined_identifier, 34);
		table.get(Token.eToken.NUMEXPR).put(Token.eToken.tok_integer_literal, 35);
		table.get(Token.eToken.NUMEXPR).put(Token.eToken.tok_add, 36);
		table.get(Token.eToken.NUMEXPR).put(Token.eToken.tok_sub, 36);
		table.get(Token.eToken.NUMEXPR).put(Token.eToken.tok_mult, 36);

		/*
		CALC	
			add sub mult	
		*/
		table.put(Token.eToken.CALC, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.CALC).put(Token.eToken.tok_add, 37);
		table.get(Token.eToken.CALC).put(Token.eToken.tok_sub, 38);
		table.get(Token.eToken.CALC).put(Token.eToken.tok_mult, 39);

		/*
		COND_BRANCH	
			if	
		*/
		table.put(Token.eToken.COND_BRANCH, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.COND_BRANCH).put(Token.eToken.tok_if, 40);

		/*
		ELSE_PART	
			else ε	
			; $ }
		*/
		table.put(Token.eToken.ELSE_PART, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.ELSE_PART).put(Token.eToken.tok_else, 41);

		table.get(Token.eToken.ELSE_PART).put(Token.eToken.tok_semi_colon, 42);
		table.get(Token.eToken.ELSE_PART).put(Token.eToken.END, 42);

		/*
		BOOL	
			eq ( not and or T F	
		*/
		table.put(Token.eToken.BOOL, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_eq, 43);
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_open_parenth, 44);
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_not, 47);
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_and, 48);
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_or, 49);
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_T, 50);
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_F, 51);

		/*
		BOOL2	
			< >	
		*/
		table.put(Token.eToken.BOOL2, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.BOOL2).put(Token.eToken.tok_less_than, 45);
		table.get(Token.eToken.BOOL2).put(Token.eToken.tok_greater_than, 46);

		/*
		COND_LOOP	
			while for	
		*/
		table.put(Token.eToken.COND_LOOP, new HashMap<Token.eToken, Integer>());
		table.get(Token.eToken.COND_LOOP).put(Token.eToken.tok_while, 53);
		table.get(Token.eToken.COND_LOOP).put(Token.eToken.tok_for, 54);

		return table;
	}

}
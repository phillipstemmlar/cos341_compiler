import java.util.*;
import java.io.*;

public class Parser {
	public static Boolean logging = true;
	public static Boolean erroring = true;
	public static Boolean successing = true;

	public static Boolean lexer_output = true;
	public static String lexer_output_file =  "lexer_output.txt";

	private Lexer lex = null;

	public static void main(String[] args) {
		String inputfile = args[0];
		String outputfile = args[1];

		Parser LL1 = new Parser();
		LL1.execute(inputfile, outputfile);
	}

	public Parser(){
		lex = new Lexer();
	}

	public void execute(String inputfile, String outputfile){
		if(lexer_output) {
			try{
				lex.executeToFile(inputfile,lexer_output_file);
			}catch(Exception e){
				return;
			}
		}

		Queue<Token> tokens = lex.execute(inputfile);
		if(tokens == null) logln(red + "Lexer returned null" + white);

		Node syntaxTree = parse(tokens);

		line();
		if(syntaxTree != null){
			success("Tokens parsed successfully");
			exportNodeToFile(syntaxTree, outputfile);
		}else{
			error("Tokens failed to parse");
		}
	}


	private void exportNodeToFile(Node root, String filename){
		//print root + children to file recursively
		writeToFile(filename, root.treeString());
	}

	private void writeToFile(String filename, String content){	
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

	public Node parse(Queue<Token> inputTokensQ){

		inputTokensQ.add((Token.END()));
		Token[] inputTokens = new Token[inputTokensQ.size()];

		int inp = 0;
		while(inputTokensQ.size() > 0){
			inputTokens[inp] = inputTokensQ.poll();
			inp++;
		}

		//LL parser table
		HashMap<Token.eToken, HashMap<Token.eToken, Integer> > table = genLL1_table();
		Stack<Node> stack = new Stack<>(); 

		Node root = new CompositeNode(Token.eToken.PROG, "--PROG--");
		//Initialize symbol stack
		stack.push(new LeafNode(Token.eToken.END,"--$--"));
		stack.push(root);

		//Initialize symbol index
		Integer p = 0;

		try{
		while(stack.size() > 0){
			if(p < inputTokens.length && inputTokens[p].get() == stack.peek().get()){
				logln("Matched symbol: " + inputTokens[p].get());
				logln("------|" + inputTokens[p].str() + "|------");
				if(stack.peek().isLeaf()){
					((LeafNode)stack.peek()).val(inputTokens[p].str());
				}		
				p++;
				stack.pop();
			}else if(p >= inputTokens.length || table.get(stack.peek().get()) == null || table.get(stack.peek().get()).get(inputTokens[p].get()) == null){
				System.out.println(green + "ERROR ENTERED 1" + white);

				int line = (p >= 1)? inputTokens[p-1].row : 0;
				int col = (p >= 1)? inputTokens[p-1].col : 0;

				System.out.println(blue + stack.peek().get() + white);

				String err = stack.peek().error();
				String found = (stack.peek().includeFound())? " \"" + white + inputTokens[p].str() + red + "\"" : "" ;

				error(err + found, line, col);
				logln(blue + Arrays.toString(stack.toArray()) + white);

				return null;
			}else{
				Integer rule = table.get(stack.peek().get()).get(inputTokens[p].get());

				if(rule == 14 && p+1 < inputTokens.length && inputTokens[p+1].get() == Token.eToken.tok_assignment){
					rule = 15;
					logln("==========Rule 15===========");
				}

				Node top = null;
				logln("Rule: " + rule);
				switch(rule){
					case 1:	// PROG → CODE PROC_DEFS_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.PROC_DEFS_PART, "--proc-defs-part--"));
							cTop.addChild(new CompositeNode(Token.eToken.CODE, ""));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 2:	// PROC_DEFS_PART → ; PROC_DEFS
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.PROC_DEFS, "--proc-defs--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_semi_colon, "Expected \";\" after instruction but found"));
							Node[] children = cTop.childrenArray();
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
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.PROC_DEFS_PART2, "--proc-defs-part2--"));
							cTop.addChild(new CompositeNode(Token.eToken.PROC, "--proc--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 5:// PROC_DEFS_PART2 → PROC_DEFS
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.PROC_DEFS, "--proc-defs--"));
							Node[] children = cTop.childrenArray();
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
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.PROG, "--prog--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected after \"proc\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_proc, "Invalid token, expected \"proc\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 8:// CODE → INSTR CODE_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.CODE_PART, "Expected \";\" after instruction but found"));
							cTop.addChild(new CompositeNode(Token.eToken.INSTR, "--instr--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 9:// CODE_PART → ; CODE
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild((new CompositeNode(Token.eToken.CODE, "The last instruction of a code block should not end with a \";\"")).setIncludeFound(false));
							cTop.addChild(new LeafNode(Token.eToken.tok_semi_colon, "Expected \";\" after instruction but found"));
							Node[] children = cTop.childrenArray();
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
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_halt, "Invalid token, expected \"halt\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 12:// INSTR → DECL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.DECL, "--decl--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 13:// INSTR → IO
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.IO, "--io--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 14:// INSTR → CALL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.CALL, "--call--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 15:// INSTR → ASSIGN
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.ASSIGN, "--assign--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 16:// INSTR → COND_BRANCH
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.COND_BRANCH, "--cond-branch--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 17:// INSTR → COND_LOOP
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.COND_LOOP, "--cond-loop--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 18:// IO → input ( VAR ), ""
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected ')' but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_input, "Invalid token, expected \"input\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 19:// IO → output ( VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected ')' but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_output, "Invalid token, expected \"output\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 20:// DECL → TYPE NAME DECL_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.DECL_PART, "--decl-part--"));
							cTop.addChild(new CompositeNode(Token.eToken.NAME, "User defined identifier expected but found"));
							cTop.addChild(new CompositeNode(Token.eToken.TYPE, "Expected a valid type but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 21:// DECL_PART → ; DECL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.DECL, "--decl--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_semi_colon, "Expected \";\" after decleration but found"));
							Node[] children = cTop.childrenArray();
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
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_num, "Expected a valid type but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 24:// TYPE → string
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_string, "Expected a valid type but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 25:// TYPE → bool
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_bool, "Expected a valid type but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 26:// CALL → userDefinedIdentifier
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 27:// NAME → userDefinedIdentifier
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 28:// VAR → userDefinedIdentifier
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_user_defined_identifier, "User defined identifier expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 29:// ASSIGN → VAR = VALUE_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.VALUE_PART, "--value-part--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 30:// VALUE_PART → stringLiteral
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_string_literal, "String literal expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 31:// VALUE_PART → VAR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 32:// VALUE_PART → NUMEXPR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 33:// VALUE_PART → BOOL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 34:// NUMEXPR → VAR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 35:// NUMEXPR → integerLiteral
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 36:// NUMEXPR → CALC
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.CALC, "--calc--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 37:// CALC → add ( NUMEXPR , NUMEXPR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_add, "Invalid token, expected \"add\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 38:// CALC → sub ( NUMEXPR , NUMEXPR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_sub, "Invalid token, expected \"sub\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 39:// CALC → mult ( NUMEXPR , NUMEXPR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.NUMEXPR, "Number expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_mult, "Invalid token, expected \"mult\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 40:// COND_BRANCH → if ( BOOL ) then { CODE } ELSE_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.ELSE_PART, "--else-part--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_then, "Invalid token, expected \"then\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_if, "Invalid token, expected \"if\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 41:// ELSE_PART → else { CODE }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_else, "Invalid token, expected \"else\" but found"));
							Node[] children = cTop.childrenArray();
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
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_eq, "Invalid token, expected \"e1\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 44:// BOOL → ( VAR BOOL2
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.BOOL2, "--bool2--"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 45:// BOOL2 → < VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_less_than, "Invalid token, expected \"<\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 46:// BOOL2 → > VAR )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_greater_than, "Invalid token, expected \">\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 47:// BOOL → not BOOL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_not, "Invalid token, expected \"not\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 48:// BOOL → and ( BOOL , BOOL )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_and, "Invalid token, expected \"and\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 49:// BOOL → or ( BOOL , BOOL )
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_or, "Invalid token, expected \"or\" but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 50:// BOOL → T
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_T, "Boolean literal expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 51:// BOOL → F
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_F, "Boolean literal expected but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 52:// BOOL → VAR
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;

					case 53:// COND_LOOP → while ( BOOL ) { CODE }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_while, "--while--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 54:// COND_LOOP → for ( VAR = 0 ; VAR < VAR ; VAR = add ( VAR , 1 ) ) { CODE }
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeNode cTop= (CompositeNode)top;
							cTop.addChild(new LeafNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.CODE, "--code--"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_comma, "Invalid token, expected \",\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_add, "Invalid token, expected \"add\" but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_semi_colon, "Invalid token, expected ';' but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_less_than, "Invalid token, expected \"<\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_semi_colon, "Invalid token, expected ';' but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafNode(Token.eToken.tok_for, "--for--"));
							Node[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					default:
						error("Parsing table defaulted");
						return null;
				};
			}
		}
		return root;

		}catch(NullPointerException e){

			// error("=======================================================");
			// System.out.print(blue);

			// System.out.println(e);
			// System.out.println("p:  \t\t" + p);
			// System.out.println(inputTokens[p].get() + " - " + inputTokens[p].str());
			// System.out.println(Arrays.toString(stack.toArray()));

			// //}else if(p >= inputTokens.length || table.get(stack.peek().get()).get(inputTokens[p].get()) == null){
			// System.out.print(green);

			// System.out.println(stack.peek().get());
			// System.out.println(table.get(stack.peek().get()));

			// error("=======================================================");

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

	private static void error(String str, int line, int col){
		if(erroring) System.out.println(red + "Syntax Error [line:" + line + ", col:" + col + "]:\n\t" + str + white);
	}

	private static void error(String str){
		if(erroring) System.out.println(red + str + white);
	}

	private static void success(String str){
		if(successing) System.out.println(green + str + white);
	}

	private static void logln(String str){
		if(logging) System.out.println(str);
	}

	private static void line(char c){
		if(logging){
			String ln = "";
			for(int i = 0; i < 30; ++i) ln += c;
			logln(ln);
		}
	}

	private static void line(){
		line('=');
	}

	public static final String bold = "\033[0;1m";
	public static final String red = "\033[0;31m";
	public static final String green = "\033[0;32m";
	public static final String blue = "\033[0;34m";
	public static final String white = "\033[0;37m";
	public static final String grey = "\033[1;30m";

}
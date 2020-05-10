import java.util.*;

public class Parser {
	private static String PREFIX;
	public SyntaxNode syntaxTree = null;

	public Parser(String prefix){PREFIX = prefix;}

	public HashMap<Integer, SyntaxNode> execute(List<Token> tokensQ){
		if(tokensQ != null){
			syntaxTree = parse(tokensQ);
			if(syntaxTree != null) {
				syntaxTree.genIndex();
				syntaxTree.prune();
				return syntaxTree.symbolTree();
			}
		}
		return null;
	}

	public HashMap<Integer, SyntaxNode> executeToFile(List<Token> tokensQ,String  Parser_SyntaxTree_output_file,String  Parser_SymbolTable_output_file,String  Parser_SyntaxTree_vis_output_fle){
		HashMap<Integer, SyntaxNode> table = execute(tokensQ);
		if(table != null){
			exportSyntaxTreeAndSymbolTable(table, Parser_SyntaxTree_output_file, Parser_SymbolTable_output_file);
			exportSyntaxNodeToFile(syntaxTree, Parser_SyntaxTree_vis_output_fle);
			return table;
		}
		return null;
	}

	private void exportSyntaxTreeAndSymbolTable(HashMap<Integer, SyntaxNode> table, String treeFile, String symbolfile){
		if(table != null){
			String treeIndexString = "";
			String treeSymbolString = "";
			for(Integer key : table.keySet()){
				SyntaxNode node = table.get(key);

				treeSymbolString += node.index + ":" + node.name2() + "\n";
				if(!node.isLeaf()){
					CompositeSyntaxNode comp = (CompositeSyntaxNode)node;
					String childIndexList = "";
					for(SyntaxNode child : comp.children){
						childIndexList += "," + child.index;
					}
					treeIndexString += comp.index + ":" + childIndexList.substring(1) + "\n";
				}
			}
			Helper.writeToFile(treeFile, treeIndexString);
			Helper.writeToFile(symbolfile, treeSymbolString);
		}
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

		SyntaxNode root = new CompositeSyntaxNode(Token.eToken.PROG, "Program source started with invalid token");
		//Initialize symbol stack
		stack.push(new LeafSyntaxNode(Token.eToken.END,"Program source ended with invalid token"));
		stack.push(root);

		//Initialize symbol index
		Integer p = 0;
		while(stack.size() > 0){
			if(p < inputTokens.length && inputTokens[p].get() == stack.peek().get()){
				Helper.logln("Matched symbol: " + inputTokens[p].get());
				Helper.logln("=======|" + inputTokens[p].str() + "|=======");
				if(stack.peek().isLeaf()){
					((LeafSyntaxNode)stack.peek()).val(inputTokens[p].str());
				}		
				p++;
				stack.pop();
			}else if(p >= inputTokens.length || table.get(stack.peek().get()) == null || table.get(stack.peek().get()).get(inputTokens[p].get()) == null){
				String err = stack.peek().error() + ((stack.peek().includeFound())? ((inputTokens[p].str() == "$")? " the end of the source file" :" \"" + inputTokens[p].str() + "\"" ): "" );
				Helper.error(PREFIX,(p >= 1)? inputTokens[p-1].row : 0, (p >= 1)? inputTokens[p-1].col : 0, err);
				return null;
			}else{
				Integer rule = table.get(stack.peek().get()).get(inputTokens[p].get());

				if(rule == 14 && p+1 < inputTokens.length && inputTokens[p+1].get() == Token.eToken.tok_assignment){
					rule = 15;
				}
				if(rule == 21 && inputTokens[p+1].get() != Token.eToken.tok_num && inputTokens[p+1].get() 
								!= Token.eToken.tok_string && inputTokens[p+1].get() != Token.eToken.tok_bool){
					rule = 22;
				}


				SyntaxNode top = null;
				Helper.logln("Rule: " + rule);
				switch(rule){
					case 1:	// PROG → CODE PROC_DEFS_PART
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS_PART, "Expected \";\" after instruction/procedure but found"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS, "Expected \";\" after instruction/procedure but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Expected \";\" after instruction/procedure but found"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS_PART2, "Expected \";\" after procedure but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC, "Procedures must start with the \"proc\" keyword. and not"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 5:// PROC_DEFS_PART2 → PROC_DEFS
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROC_DEFS, "Expected \";\" after procedure but found"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.PROG, "Program source ended with invalid token"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.INSTR, "Instruction started with an invalid token of "));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.DECL, "Decleration must start with a type, which this is not"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 13:// INSTR → IO
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.IO, "IO instructions must start with either \"input\" or \"output\", and not"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 14:// INSTR → CALL
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CALL, "Instructions that call a procedure must start with a procedure name, and not"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 15:// INSTR → ASSIGN
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.ASSIGN, "Cannot assign the the left hand side, it is not a variable"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 16:// INSTR → COND_BRANCH
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.COND_BRANCH, "IF-THEN and IF-THEN-ELSE instructions must start with \"if\" and not with"));
							SyntaxNode[] children = cTop.childrenArray();
							for(int i = 0; i < children.length; ++i)	stack.push(children[i]);
						}
						break;
					case 17:// INSTR → COND_LOOP
						top = stack.peek();
						stack.pop();

						if(!top.isLeaf()){
							CompositeSyntaxNode cTop= (CompositeSyntaxNode)top;
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.COND_LOOP, "Loop instructions must start with either \"for\" or \"while\" and not with"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.DECL_PART, "Decleration must start with a type, which this is not"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.INSTR, "Decleration must start with a type, which this is not"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VALUE_PART, "Only expressions can be assign to a variable, this cannot "));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CALC, "Calculation expressions must start with either \"add\", \"sub\" or \"mult\", and not with"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.ELSE_PART, "Expected a \";\" or an \"else\" after IF-THEN statement, but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_brace, "Invalid token, expected \"}\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "Instruction cannot start with expresion"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "Instruction cannot start with expresion"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL2, "Boolean operator \"<\" or \">\" expected, but found"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "Instruction cannot start with expresion"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_brace, "Invalid token, expected \"{\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_close_parenth, "Invalid token, expected \")\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.BOOL, "Boolean expression expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected \"(\" but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_while, "While loop instructions must start with \"while\" and not with"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.CODE, "Instruction cannot start with expresion"));
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
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_less_than, "Invalid token, expected \"<\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_semi_colon, "Invalid token, expected ';' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_integer_literal, "Integer literal expected but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_assignment, "Invalid token, expected \"=\" but found"));
							cTop.addChild(new CompositeSyntaxNode(Token.eToken.VAR, "Expected a variable but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_open_parenth, "Invalid token, expected '(' but found"));
							cTop.addChild(new LeafSyntaxNode(Token.eToken.tok_for, "For loop instructions must start with \"for\" and not with"));
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
		table.get(Token.eToken.CODE).put(Token.eToken.tok_bool, 8);
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
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_user_defined_identifier, 29);
		table.get(Token.eToken.DECL).put(Token.eToken.tok_user_defined_identifier, 15);
		
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_user_defined_identifier, 22);
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_input, 22);
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_output, 22);
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_halt, 22);
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_if, 22);
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_for, 22);
		// table.get(Token.eToken.DECL).put(Token.eToken.tok_while, 22);

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
		table.get(Token.eToken.ELSE_PART).put(Token.eToken.tok_close_brace, 42);
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
		table.get(Token.eToken.BOOL).put(Token.eToken.tok_user_defined_identifier, 52);

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
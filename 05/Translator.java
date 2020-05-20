import java.util.*;

public class Translator{

	HashMap<Integer, SyntaxNode> symbolTree = null;
	HashMap<Integer, Scope> scopeTable = null;
	int lineNumber = 0;
	int varCount = 0;

	public Translator(){}

	public void execute(HashMap<Integer, SyntaxNode> symTree, HashMap<Integer, Scope> scopeTbl, String BASIC_file){
		if(symTree != null && scopeTbl != null && BASIC_file.length() > 0){
			symbolTree = symTree;
			scopeTable = scopeTbl;

			// System.out.println(symbolTree.get(0).treeString());
			deconstructTree();
			setProcLineNumbers();
			System.out.println(symbolTree.get(0).treeString());

			String output_code = translate();

			Helper.writeToFile(BASIC_file, output_code);
		}
	}

	private String translate(){
		lineNumber = 0;
		return translate(symbolTree.get(0), scopeTable.get(0), true);
	}

	private String translate(SyntaxNode node, Scope scope, Boolean first){
		if(node.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(node.index);
			if(scope == null) return "";
		}
		else if(isPrimitive(node.token)){
			return ((LeafSyntaxNode)node).val();
		}
		else if(isIO(node.token) || isOP(node.token)){
			return TOKENtoBASIC(node.token);
		}
		else if(node.token == Token.eToken.VAR){
			return translate(((CompositeSyntaxNode)node).children.get(0),scope, false) + typeSuffix(node.type);
		}
		else if(node.token == Token.eToken.INSTR){
			SyntaxNode INS = ((CompositeSyntaxNode)node).children.get(0);
			if(INS.token != Token.eToken.DECL){
				incLine();
				return lineNumber + " " + translate(INS,scope, false) + "\n";
			}
			return "";
		}
		else if(node.token == Token.eToken.DECL){				//DECL string
			return "";	//no DECL in BASIC
		}
		else if(node.token == Token.eToken.ASSIGN){			//ASSIGN
			String var = translate(((CompositeSyntaxNode)node).children.get(0),scope, false);
			String value = translate(((CompositeSyntaxNode)node).children.get(1),scope, false);
			return var + " = " + value;
		}
		else if(node.token == Token.eToken.IO){			//OUTPUT	
			String io = translate(((CompositeSyntaxNode)node).children.get(0),scope, false);
			String var = translate(((CompositeSyntaxNode)node).children.get(1),scope, false);
			return io + " " + var;
		}
		else if(node.token == Token.eToken.NUMEXPR){
			return translate(((CompositeSyntaxNode)node).children.get(0),scope, false);
		}
		else if(node.token == Token.eToken.CALC){
			String op = translate(((CompositeSyntaxNode)node).children.get(0),scope, false);
			String n1 = translate(((CompositeSyntaxNode)node).children.get(1),scope, false);
			String n2 = translate(((CompositeSyntaxNode)node).children.get(2),scope, false);
			return n1 + " " + op + " " + n2;
		}
		else if(node.token == Token.eToken.PROC){
			String PROG = translate(((CompositeSyntaxNode)node).children.get(2),scope, false);
			incLine();
			return PROG + lineNumber + " " + "RETURN" + "\n";
		}
		else if(node.token == Token.eToken.CALL){
			LeafSyntaxNode proc_name = (LeafSyntaxNode)(((CompositeSyntaxNode)node).children.get(0));
			Procedure proc = scope.getProcedureByName(proc_name.val());
			return  "GOTO" + " " + proc.basic_line;
		}

		if(!node.isLeaf()){
			String out = "";
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				out += translate(child,scope, false);
			}
			return out;
		}
		return "";
	}

	private void deconstructTree(){
		varCount = Variable.count();
		deconstructTree(symbolTree.get(0), scopeTable.get(0), true);
	}

	private void deconstructTree(SyntaxNode node, Scope scope, Boolean first){
		if(node == null) return;
		else if(node.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(node.index);
			if(scope == null) return;
		}else if(node.token == Token.eToken.CALC){
			//TEST if valid case
			SyntaxNode OP = ((CompositeSyntaxNode)node).children.get(0);
			SyntaxNode N1 = ((CompositeSyntaxNode)node).children.get(1);
			SyntaxNode N2 = ((CompositeSyntaxNode)node).children.get(2);

			SyntaxNode n1 = N1.isLeaf() ? null : ((CompositeSyntaxNode)N1).children.get(0);
			SyntaxNode n2 = N2.isLeaf() ? null : ((CompositeSyntaxNode)N2).children.get(0);

			if(n1 != null && n1.token == Token.eToken.CALC){
				Helper.success("NESTED CALC1");
				SyntaxNode parCODE = n1.parent;
				Helper.success("\t"+ (parCODE != null ? parCODE.name() : "null"));
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
					Helper.success("\t"+ (parCODE != null ? parCODE.name() : "null"));
				}
				if(parCODE != null){
					Helper.success("PARENT CODE1");
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					CompositeSyntaxNode newNUM_EXPR = new CompositeSyntaxNode(Token.eToken.NUMEXPR, "");

					CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					varCount++;
					new_var.val("V" + varCount);
					new_var2.val("V" + varCount);

					newNUM_EXPR.children = ((CompositeSyntaxNode)N1).children;
					((CompositeSyntaxNode)N1).resetChildren();

					//SETUP INSTR
					newASSIGN.addChild(newNUM_EXPR);
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.num;
					newASSIGN.addChild(newVAR);
					newINSTR.addChild(newASSIGN);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					newVAR2.addChild(new_var2);
					newVAR2.type = Variable.Type.num;
					((CompositeSyntaxNode)node).resetChildren();
					((CompositeSyntaxNode)node).addChild(N2);
					((CompositeSyntaxNode)node).addChild(new_var2);
					((CompositeSyntaxNode)node).addChild(OP);

				}
			}

			if(n2 != null && n2.token == Token.eToken.CALC){
				Helper.success("NESTED CALC2");
				SyntaxNode parCODE = n2.parent;
				Helper.success("\t"+ (parCODE != null ? parCODE.name() : "null"));
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
					Helper.success("\t"+ (parCODE != null ? parCODE.name() : "null"));
				}
				if(parCODE != null){
					Helper.success("PARENT CODE2");
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					CompositeSyntaxNode newNUM_EXPR = new CompositeSyntaxNode(Token.eToken.NUMEXPR, "");

					CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					varCount++;
					new_var.val("V" + varCount);
					new_var2.val("V" + varCount);

					newNUM_EXPR.children = ((CompositeSyntaxNode)N2).children;
					((CompositeSyntaxNode)N2).resetChildren();

					//SETUP INSTR
					newASSIGN.addChild(newNUM_EXPR);
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.num;
					newASSIGN.addChild(newVAR);
					newINSTR.addChild(newASSIGN);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					newVAR2.addChild(new_var2);
					newVAR2.type = Variable.Type.num;
					((CompositeSyntaxNode)node).resetChildren();
					((CompositeSyntaxNode)node).addChild(new_var2);
					((CompositeSyntaxNode)node).addChild(N1);
					((CompositeSyntaxNode)node).addChild(OP);

				}
			}

			deconstructTree(n1,scope, false);
			deconstructTree(n2,scope, false);
			return;
		}

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				deconstructTree(child,scope, false);
			}
		}

	}

	private void setProcLineNumbers(){
		lineNumber = 0;
		setProcLineNumbers(symbolTree.get(0), scopeTable.get(0), true);
	}

	private void setProcLineNumbers(SyntaxNode node, Scope scope, Boolean first){
		if(node == null) return;
		else if(node.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(node.index);
			if(scope == null) return;
		}
		else if(node.token == Token.eToken.PROC){
			LeafSyntaxNode proc_name = (LeafSyntaxNode)(((CompositeSyntaxNode)node).children.get(1));
			Procedure proc = scope.getProcedureByName(proc_name.val());
			proc.basic_line = lineNumber;
			setProcLineNumbers(((CompositeSyntaxNode)node).children.get(2), scope, false);
			incLine();
		}else if(node.token == Token.eToken.INSTR){
			incLine();
		}

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				setProcLineNumbers(child,scope, false);
			}
		}
	}

	private void incLine(){
		lineNumber += 10;
	}

	//================TOKEN CHECKERS============

	private Boolean isIO(Token.eToken token){
		return token == Token.eToken.tok_input || token == Token.eToken.tok_output;
	}

	private Boolean isOP(Token.eToken token){
		return token == Token.eToken.tok_add || token == Token.eToken.tok_sub || token == Token.eToken.tok_mult
		|| token == Token.eToken.tok_eq || token == Token.eToken.tok_greater_than || token == Token.eToken.tok_less_than ;
		// || token == Token.eToken.tok_and || token == Token.eToken.tok_or || token == Token.eToken.tok_not
	}

	private Boolean isPrimitive(Token.eToken token){
		return token == Token.eToken.tok_string_literal || token == Token.eToken.tok_integer_literal || token == Token.eToken.tok_user_defined_identifier
		|| token == Token.eToken.tok_T || token == Token.eToken.tok_F;
	}

	//================TOKEN TRANSLATION============

	private String TOKENtoBASIC(Token.eToken token){
		if(token == Token.eToken.tok_input) return "INPUT";
		if(token == Token.eToken.tok_output) return "PRINT";
		if(token == Token.eToken.tok_add) return "+";
		if(token == Token.eToken.tok_sub) return "-";
		if(token == Token.eToken.tok_mult) return "*";
		if(token == Token.eToken.tok_eq) return "=";
		if(token == Token.eToken.tok_less_than) return "<";
		if(token == Token.eToken.tok_greater_than) return ">";
		// if(token == Token.eToken.tok_) return "PRINT";
		// if(token == Token.eToken.tok_) return "PRINT";
		// if(token == Token.eToken.tok_) return "PRINT";
		return "##-TOKENtoBASIC-##";
	}

	private String typeSuffix(Variable.Type type){
		if(type == Variable.Type.num) return "";
		if(type == Variable.Type.string) return "$";
		if(type == Variable.Type.bool) return "";
		return "##-typeSuffix-##";
	}

}

/*
num,string,bool,procedure,undefined,none,notype

tok_and,
tok_or,
tok_not,
tok_add,
tok_sub,
tok_mult,
tok_if,
tok_then,
tok_else,
tok_while,
tok_for,
tok_eq,
tok_input,
tok_output,
tok_halt,
tok_num,
tok_bool,
tok_string,
tok_proc,
tok_T,
tok_F,
tok_user_defined_identifier,
tok_string_literal,
tok_integer_literal,
tok_less_than,
tok_greater_than,
tok_space,
tok_tab,
tok_newline,
tok_open_parenth,
tok_close_parenth,
tok_open_brace,
tok_close_brace,
tok_assignment,
tok_comma,
tok_semi_colon,
tok_none,
//NON-TERMINALS
END,			// $
PROG,
PROC_DEFS,
PROC,
CODE,
INSTR,
IO,
CALL,
DECL,
TYPE,
NAME,
VAR,
ASSIGN,
NUMEXPR,
CALC,
COND_BRANCH,
BOOL,
COND_LOOP,
//Left-Factorization NON-TERMINALS
PROC_DEFS_PART,
PROC_DEFS_PART2,
CODE_PART,
DECL_PART,
VALUE_PART,
ELSE_PART,
BOOL2
*/
import java.util.*;
public class LeafSyntaxNode extends SyntaxNode{

	private String value = "";
	private Integer scopeLevel = -1;
	private static Token.eToken[] pruneTokens = {
		Token.eToken.tok_space,
		Token.eToken.tok_tab,
		Token.eToken.tok_newline,
		Token.eToken.tok_open_parenth,
		Token.eToken.tok_close_parenth,
		Token.eToken.tok_open_brace,
		Token.eToken.tok_close_brace,
		Token.eToken.tok_assignment,
		Token.eToken.tok_comma,
		Token.eToken.tok_semi_colon,
		Token.eToken.tok_none,
	};

	public LeafSyntaxNode(Token.eToken tok, String err){
		super(tok, true, err);
		value = "";
	}

	public LeafSyntaxNode(Token.eToken tok, String err, String val){
		super(tok, true, err);
		value = val;
	}

	public void prune(){

		Boolean shouldPrune = false;
		for(int i = 0; i < pruneTokens.length; ++i) shouldPrune |= (token == pruneTokens[i]); 

		if(shouldPrune){
			if(parent != null) parent.removeChild(this);
		}

	}

	public String name(){
		return index + " " + nodeName() ;
	}

	public String name3(){
		return nodeName() + scopeStr() + typeStr();
	}

	public String name2(){
		return nodeName() + strScope() + strType();
	}

	private String nodeName(){
		String NAME = token + "";
		if(token == Token.eToken.tok_user_defined_identifier){
			if(parent != null){
				if(parent.token == Token.eToken.VAR || parent.token == Token.eToken.NAME) NAME = "variable";
				if(parent.token == Token.eToken.PROC || parent.token == Token.eToken.CALL) NAME = "procedure";
			}
		}
		if(parent != null && parent.token == Token.eToken.TYPE) NAME = "type";
		if(token == Token.eToken.tok_integer_literal)NAME = "num";
		if(token == Token.eToken.tok_T || token == Token.eToken.tok_F)NAME = "bool";
		if(token == Token.eToken.tok_input || token == Token.eToken.tok_output)NAME = "io";
		if(token == Token.eToken.tok_greater_than || token == Token.eToken.tok_less_than)NAME = "operator";
		if(token == Token.eToken.tok_string_literal)return "string " + value;
		if(token == Token.eToken.tok_halt)return "halt";
		if(token == Token.eToken.tok_if)return "if";
		if(token == Token.eToken.tok_then)return "then";
		if(token == Token.eToken.tok_else)return "else";
		if(token == Token.eToken.tok_proc)return "proc";
		if(token == Token.eToken.tok_eq)return "eq";
		if(token == Token.eToken.tok_for)return "for";
		if(token == Token.eToken.tok_while)return "while";
		if(token == Token.eToken.tok_and)return "and";
		if(token == Token.eToken.tok_or)return "or";
		if(token == Token.eToken.tok_not)return "not";
		if(token == Token.eToken.tok_add)return "add";
		if(token == Token.eToken.tok_sub)return "sub";
		if(token == Token.eToken.tok_mult)return "mult";

		return NAME + " '"+ value +"'";
	}

	public String val(){
		return value;
	}

	public void val(String v){
		value = v;
	}

	public String treeString(String prefix) {
		return name3();
	}

	public Integer scope(){
		return scopeLevel;
	}

	public void scope(Integer lvl){
		scopeLevel = lvl;
	}

	private String scopeStr(){
		if(scopeLevel != null && scopeLevel >= 0){
			return "\tScope-Level: " + scopeLevel;
		}
		return "";
	}

	private String strScope(){
		if(scopeLevel != null && scopeLevel >= 0){
			return "\n\t└─" + scopeStr();
		}
		return "";
	}

}
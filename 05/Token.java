public class Token{

	private eToken token;
	private String value;

	public Integer row;
	public Integer col;

	public Token(eToken tokenValue, String strValue, int r, int c){
		token = tokenValue;
		value = strValue;
		row = r;
		col = c;
	}

	public eToken get(){
		return token;
	}
	public void set(eToken tokenValue){
		token = tokenValue;
	}

	public String str(){
		return value;
	}
	public void str(String strValue){
		value = strValue;
	}

	public String toString(){
		return "[" + str() + " | " + get() + "]";
	}

	public static Token END(){
		return new Token(eToken.END,"$",-1,-1);
	}

	public static enum eToken{
		//TERMINALS
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
	}
}
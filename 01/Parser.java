import java.util.*;

public class Parser {
	public static void main(String[] args) {
		String inputfile = args[0];
		String outputfile = args[1];

		Lexer lex = new Lexer();

		lex.executeToFile(inputfile, outputfile);

		// Lexer.Token[] tokens = lex.execute(inputfile, outputfile);
		// if(tokens == null) return;

		// parse(tokens);
	}

	public void parse(Lexer.Token[] inputTokens){

		//LL parser table
		HashMap<Lexer.Token, HashMap<Lexer.Token, Integer> > table = genLL1_table();
		Stack<Lexer.Token> stack = new Stack<>(); 

		//Initialize symbol stack
		stack.push(Lexer.Token.END);
		stack.push(Lexer.Token.PROG);

		//Initialize symbol index
		Integer p = 0;

		while(stack.size() > 0){
			if(inputTokens[p] == stack.peek()){
				System.out.println("Matched symbol: " + inputTokens[p]);
				p++;
				stack.pop();
			}else if(table[stack.peek()][inputTokens[p]] == null){
				console.log("ERROR");
			}else{
				System.out.println("Rule: " + table[stack.peek()][inputTokens[p]]);
				switch(table[stack.peek()][inputTokens[p]]){
					case 1:	// PROG → CODE PROC_DEFS_PART
						stack.pop();
						stack.push(Lexer.Token.PROC_DEFS_PART);
						stack.push(Lexer.Token.CODE);
						break;
					case 2:	// PROC_DEFS_PART → ; PROC_DEFS
						stack.pop();
						stack.push(Lexer.Token.PROC_DEFS);
						stack.push(Lexer.Token.tok_semi_colon);
						break;
					case 3:	// PROC_DEFS_PART → ε
						stack.pop();
						break;

					case 4:// PROC_DEFS → PROC PROC_DEFS_PART2
						stack.pop();
						stack.push(Lexer.Token.PROC_DEFS_PART2);
						stack.push(Lexer.Token.PROC);
						break;
					case 5:// PROC_DEFS_PART2 → PROC_DEFS
						stack.pop();
						stack.push(Lexer.Token.PROC_DEFS);
						break;
					case 6:	// PROC_DEFS_PART2 → ε
						stack.pop();
						break;

					case 7:// PROC → proc userDefinedIdentifier { PROG }
						stack.pop();
						stack.push(Lexer.Token.tok_close_brace);
						stack.push(Lexer.Token.PROG);
						stack.push(Lexer.Token.tok_open_brace);
						stack.push(Lexer.Token.tok_user_defined_identifier);
						stack.push(Lexer.Token.tok_proc);
						break;

					case 8:// CODE → INSTR CODE_PART
						stack.pop();
						stack.push(Lexer.Token.CODE_PART);
						stack.push(Lexer.Token.INSTR);
						break;
					case 9:// CODE_PART → ; CODE
						stack.pop();
						stack.push(Lexer.Token.CODE);
						stack.push(Lexer.Token.tok_semi_colon);
						break;
					case 10:// CODE_PART → ε
						stack.pop();
						break;

					case 11:// INSTR → halt
						stack.pop();
						stack.push(Lexer.Token.tok_halt);
						break;
					case 12:// INSTR → DECL
						stack.pop();
						stack.push(Lexer.Token.DECL);
						break;
					case 13:// INSTR → IO
						stack.pop();
						stack.push(Lexer.Token.IO);
						break;
					case 14:// INSTR → CALL
						stack.pop();
						stack.push(Lexer.Token.CALL);
						break;
					case 15:// INSTR → ASSIGN
						stack.pop();
						stack.push(Lexer.Token.ASSIGN);
						break;
					case 16:// INSTR → COND_BRANCH
						stack.pop();
						stack.push(Lexer.Token.COND_BRANCH);
						break;
					case 17:// INSTR → COND_LOOP
						stack.pop();
						stack.push(Lexer.Token.COND_LOOP);
						break;

					case 18:// IO → input ( VAR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_input);
						break;
					case 19:// IO → output ( VAR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_output);
						break;

					case 20:// DECL → TYPE NAME DECL_PART
						stack.pop();
						stack.push(Lexer.Token.DECL_PART);
						stack.push(Lexer.Token.NAME);
						stack.push(Lexer.Token.TYPE);
						break;
					case 21:// DECL_PART → ; DECL
						stack.pop();
						stack.push(Lexer.Token.DECL);
						stack.push(Lexer.Token.tok_semi_colon);
						break;
					case 22:// DECL_PART → ε
						stack.pop();
						break;

					case 23:// TYPE → num
						stack.pop();
						stack.push(Lexer.Token.tok_num);
						break;
					case 24:// TYPE → string
						stack.pop();
						stack.push(Lexer.Token.tok_string);
						break;
					case 25:// TYPE → bool
						stack.pop();
						stack.push(Lexer.Token.tok_bool);
						break;

					case 26:// CALL → userDefinedIdentifier
						stack.pop();
						stack.push(Lexer.Token.tok_user_defined_identifier);
						break;

					case 27:// NAME → userDefinedIdentifier
						stack.pop();
						stack.push(Lexer.Token.tok_user_defined_identifier);
						break;

					case 28:// VAR → userDefinedIdentifier
						stack.pop();
						stack.push(Lexer.Token.tok_user_defined_identifier);
						break;

					case 29:// ASSIGN → VAR = VALUE_PART
						stack.pop();
						stack.push(Lexer.Token.VALUE_PART);
						stack.push(Lexer.Token.tok_assignment);
						stack.push(Lexer.Token.VAR);
						break;
					case 30:// VALUE_PART → stringLiteral
						stack.pop();
						stack.push(Lexer.Token.tok_string_literal);
						break;
					case 31:// VALUE_PART → VAR
						stack.pop();
						stack.push(Lexer.Token.VAR);
						break;
					case 32:// VALUE_PART → NUMEXPR
						stack.pop();
						stack.push(Lexer.Token.NUMEXPR);
						break;
					case 33:// VALUE_PART → BOOL
						stack.pop();
						stack.push(Lexer.Token.BOOL);
						break;

					case 34:// NUMEXPR → VAR
						stack.pop();
						stack.push(Lexer.Token.VAR);
						break;
					case 35:// NUMEXPR → integerLiteral
						stack.pop();
						stack.push(Lexer.Token.tok_integer_literal);
						break;
					case 36:// NUMEXPR → CALC
						stack.pop();
						stack.push(Lexer.Token.CALC);
						break;

					case 37:// CALC → add ( NUMEXPR , NUMEXPR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.NUMEXPR);
						stack.push(Lexer.Token.tok_comma);
						stack.push(Lexer.Token.NUMEXPR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_add);
						break;
					case 38:// CALC → sub ( NUMEXPR , NUMEXPR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.NUMEXPR);
						stack.push(Lexer.Token.tok_comma);
						stack.push(Lexer.Token.NUMEXPR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_sub);
						break;
					case 39:// CALC → mult ( NUMEXPR , NUMEXPR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.NUMEXPR);
						stack.push(Lexer.Token.tok_comma);
						stack.push(Lexer.Token.NUMEXPR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_mult);
						break;

					case 40:// COND_BRANCH → if ( BOOL ) then { CODE } ELSE_PART
						stack.pop();
						stack.push(Lexer.Token.ELSE_PART);
						stack.push(Lexer.Token.tok_close_brace);
						stack.push(Lexer.Token.CODE);
						stack.push(Lexer.Token.tok_open_brace);
						stack.push(Lexer.Token.tok_then);
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.BOOL);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_if);	
						break;

					case 41:// ELSE_PART → else { CODE }
						stack.pop();
						stack.push(Lexer.Token.tok_close_brace);
						stack.push(Lexer.Token.CODE);
						stack.push(Lexer.Token.tok_open_brace);
						stack.push(Lexer.Token.tok_else);
						break;
					case 42:// ELSE_PART → ε
						stack.pop();
						break;



					case 43:// BOOL → eq ( VAR , VAR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_comma);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_eq);
						break;

					case 44:// BOOL → ( VAR BOOL2
						stack.pop();
						stack.push(Lexer.Token.BOOL2);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						break;

					case 45:// BOOL2 → < VAR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_less_than);
						break;
					case 46:// BOOL2 → > VAR )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_greater_than);
						break;
					case 47:// BOOL → not BOOL
						stack.pop();
						stack.push(Lexer.Token.BOOL);
						stack.push(Lexer.Token.tok_not);
						break;
					case 48:// BOOL → and ( BOOL , BOOL )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_comma);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_and);
						break;
					case 49:// BOOL → or ( BOOL , BOOL )
						stack.pop();
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_comma);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_or);
						break;
					case 50:// BOOL → T
						stack.pop();
						stack.push(Lexer.Token.tok_T);
						break;
					case 51:// BOOL → F
						stack.pop();
						stack.push(Lexer.Token.tok_F);
						break;
					case 52:// BOOL → VAR
						stack.pop();
						stack.push(Lexer.Token.VAR);
						break;

					case 53:// COND_LOOP → while ( BOOL ) { CODE }
						stack.pop();
						stack.push(Lexer.Token.tok_close_brace);
						stack.push(Lexer.Token.CODE);
						stack.push(Lexer.Token.tok_open_brace);
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.BOOL);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_while);
						break;
					case 54:// COND_LOOP → for ( VAR = 0 ; VAR < VAR ; VAR = add ( VAR , 1 ) ) { CODE }
						stack.pop();
						stack.push(Lexer.Token.tok_close_brace);
						stack.push(Lexer.Token.CODE);
						stack.push(Lexer.Token.tok_open_brace);
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.tok_close_parenth);
						stack.push(Lexer.Token.tok_integer_literal);
						stack.push(Lexer.Token.tok_tok_comma);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_add);
						stack.push(Lexer.Token.tok_assignment);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_semi_colon);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_less_than);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_semi_colon);
						stack.push(Lexer.Token.tok_integer_literal);
						stack.push(Lexer.Token.tok_assignment);
						stack.push(Lexer.Token.VAR);
						stack.push(Lexer.Token.tok_open_parenth);
						stack.push(Lexer.Token.tok_for);
						break;
					default:
						System.out.println("Parsing table defaulted");
						return;
						break;
				};
			}
		}

	}

	private HashMap<Lexer.Token, HashMap<Lexer.Token, Integer> > genLL1_table(){
		HashMap<Lexer.Token, HashMap<Lexer.Token, Integer> > table = new HashMap<>();
		/*
		PROG
			halt num string bool input output uDefLIt if while for
		*/
		table[Lexer.Token.PROG][Lexer.Token.tok_halt] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_num] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_string] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_bool] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_input] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_output] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_user_defined_identifier] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_if] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_while] = 1;
		table[Lexer.Token.PROG][Lexer.Token.tok_for] = 1;
		
		/*
		PROC_DEFS_PART
			; ε	
			$ }
		*/		
		table[Lexer.Token.PROC_DEFS_PART][Lexer.Token.tok_semi_colon] = 2;

		table[Lexer.Token.PROC_DEFS_PART][Lexer.Token.tok_open_brace] = 3;
		table[Lexer.Token.PROC_DEFS_PART][Lexer.Token.END] = 3;

		/*
		PROC_DEFS
			proc
		*/		
		table[Lexer.Token.PROC_DEFS][Lexer.Token.tok_proc] = 4;

		/*
		PROC_DEFS_PART2
			proc ε
			$ }
		*/		
		table[Lexer.Token.PROC_DEFS_PART2][Lexer.Token.tok_proc] = 5;

		table[Lexer.Token.PROC_DEFS_PART2][Lexer.Token.tok_open_brace] = 6;
		table[Lexer.Token.PROC_DEFS_PART2][Lexer.Token.END] = 6;
		
		/*
		PROC
			proc
		*/
		table[Lexer.Token.PROC][Lexer.Token.tok_proc] = 7;
		
		/*
		CODE	
			halt num string bool input output uDefLIt if while for	
			; $ }
		*/
		table[Lexer.Token.CODE][Lexer.Token.tok_halt] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_num] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_string] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_bool] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_input] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_output] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_user_defined_identifier] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_if] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_while] = 8;
		table[Lexer.Token.CODE][Lexer.Token.tok_for] = 8;

		/*
		CODE_PART	
			; ε	
			; $ }
		*/
		table[Lexer.Token.CODE_PART][Lexer.Token.tok_semi_colon] = 9;

		table[Lexer.Token.CODE_PART][Lexer.Token.tok_close_brace] = 10;
		table[Lexer.Token.CODE_PART][Lexer.Token.END] = 10;

		/*
		INSTR	
			halt num string bool input output uDefLIt if while for
		*/
		table[Lexer.Token.INSTR][Lexer.Token.tok_halt] = 11;
		table[Lexer.Token.INSTR][Lexer.Token.tok_num] = 12;
		table[Lexer.Token.INSTR][Lexer.Token.tok_string] = 12;
		table[Lexer.Token.INSTR][Lexer.Token.tok_bool] = 12;
		table[Lexer.Token.INSTR][Lexer.Token.tok_input] = 13;
		table[Lexer.Token.INSTR][Lexer.Token.tok_output] = 13;
		table[Lexer.Token.INSTR][Lexer.Token.tok_user_defined_identifier] = 14;
		table[Lexer.Token.INSTR][Lexer.Token.tok_if] = 16;
		table[Lexer.Token.INSTR][Lexer.Token.tok_while] = 17;
		table[Lexer.Token.INSTR][Lexer.Token.tok_for] = 17;

		/*
		IO	
			input output	
		*/
		table[Lexer.Token.IO][Lexer.Token.tok_input] = 18;
		table[Lexer.Token.IO][Lexer.Token.tok_output] = 19;
		
		/*
		DECL	
			num string bool	
		*/
		table[Lexer.Token.DECL][Lexer.Token.tok_num] = 20;
		table[Lexer.Token.DECL][Lexer.Token.tok_string] = 20;
		table[Lexer.Token.DECL][Lexer.Token.tok_bool] = 20;

		/*
		DECL_PART	
			; ε	
			; $ }
		*/
		table[Lexer.Token.DECL_PART][Lexer.Token.tok_semi_colon] = 21;

		table[Lexer.Token.DECL_PART][Lexer.Token.tok_close_brace] =22;
		table[Lexer.Token.DECL_PART][Lexer.Token.END] = 22;

		/*
		TYPE	
			num string bool	
		*/
		table[Lexer.Token.TYPE][Lexer.Token.tok_num] = 23;
		table[Lexer.Token.TYPE][Lexer.Token.tok_string] = 24;
		table[Lexer.Token.TYPE][Lexer.Token.tok_bool] = 25;

		/*
		CALL	
			uDefLIt	
		*/
		table[Lexer.Token.CALL][Lexer.Token.tok_user_defined_identifier] = 26;

		/*
		NAME	
			uDefLIt	
		*/
		table[Lexer.Token.NAME][Lexer.Token.tok_user_defined_identifier] = 27;

		/*
		VAR	
			uDefLIt	
		*/
		table[Lexer.Token.VAR][Lexer.Token.tok_user_defined_identifier] = 28;

		/*
		ASSIGN	
			uDefLIt	
		*/
		table[Lexer.Token.ASSIGN][Lexer.Token.tok_user_defined_identifier] = 29;

		/*
		VALUE_PART	
			strLit uDefLIt intLit add sub mult eq ( not and or T F
		*/
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_string_literal] = 30;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_user_defined_identifier] = 31;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_integer_literal] = 32;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_add] = 32;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_sub] = 32;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_mult] = 32;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_eq] = 33;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_open_parenth] = 33;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_not] = 33;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_and] = 33;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_or] = 33;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_T] = 33;
		table[Lexer.Token.VALUE_PART][Lexer.Token.tok_F] = 33;

		/*
		NUMEXPR	
			uDefLIt intLit add sub mult	
		*/
		table[Lexer.Token.NUMEXPR][Lexer.Token.tok_user_defined_identifier] = 34;
		table[Lexer.Token.NUMEXPR][Lexer.Token.tok_integer_literal] = 24;
		table[Lexer.Token.NUMEXPR][Lexer.Token.tok_add] = 36;
		table[Lexer.Token.NUMEXPR][Lexer.Token.tok_sub] = 36;
		table[Lexer.Token.NUMEXPR][Lexer.Token.tok_mult] = 36;

		/*
		CALC	
			add sub mult	
		*/
		table[Lexer.Token.CALC][Lexer.Token.tok_add] = 37;
		table[Lexer.Token.CALC][Lexer.Token.tok_sub] = 38;
		table[Lexer.Token.CALC][Lexer.Token.tok_mult] = 39;

		/*
		CODE_BRANCH	
			if	
		*/
		table[Lexer.Token.CODE_BRANCH][Lexer.Token.tok_if] = 40;

		/*
		ELSE_PART	
			else ε	
			; $ }
		*/
		table[Lexer.Token.ELSE_PART][Lexer.Token.tok_else] = 41;

		table[Lexer.Token.ELSE_PART][Lexer.Token.tok_close_brace] = 42;
		table[Lexer.Token.ELSE_PART][Lexer.Token.END] = 42;

		/*
		BOOL	
			eq ( not and or T F	
		*/
		table[Lexer.Token.BOOL][Lexer.Token.tok_eq] = 43;
		table[Lexer.Token.BOOL][Lexer.Token.tok_open_parenth] = 44;
		table[Lexer.Token.BOOL][Lexer.Token.tok_not] = 47;
		table[Lexer.Token.BOOL][Lexer.Token.tok_and] = 48;
		table[Lexer.Token.BOOL][Lexer.Token.tok_or] = 49;
		table[Lexer.Token.BOOL][Lexer.Token.tok_T] = 50;
		table[Lexer.Token.BOOL][Lexer.Token.tok_F] = 51;

		/*
		BOOL2	
			< >	
		*/
		table[Lexer.Token.BOOL2][Lexer.Token.tok_less_than] = 45;
		table[Lexer.Token.BOOL2][Lexer.Token.tok_greater_than] = 46;

		/*
		COND_LOOP	
			while for	
		*/
		table[Lexer.Token.COND_LOOP][Lexer.Token.tok_while] = 53;
		table[Lexer.Token.COND_LOOP][Lexer.Token.tok_for] = 54;

		return table;
	}

	public static final String bold = "\033[0;1m";
	public static final String red = "\033[0;31m";
	public static final String green = "\033[0;32m";
	public static final String blue = "\033[0;34m";
	public static final String white = "\033[0;37m";
	public static final String grey = "\033[1;30m";

}
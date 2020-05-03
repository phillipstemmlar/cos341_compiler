import java.util.*;

public class Parser {
	public static void main(String[] args) {
		String inputfile = args[0];
		String outputfile = args[1];

		Lexer lex = new Lexer();

		lex.executeToFile(inputfile, "lexer_output.txt");

		Queue<Token> tokens = lex.execute(inputfile, outputfile);
		if(tokens == null) return;

		Parser LL1 = new Parser();
		Boolean success = LL1.parse(tokens);

		line();
		if(success){
			System.out.println(green + "Tokens parsed successfully" + white);
		}else{
			System.out.println(red + "Tokens failed to parse" + white);
		}
	}

	private static Boolean logging = true;

	public Boolean parse(Queue<Token> inputTokensQ){

		inputTokensQ.add((Token.END()));
		Token[] inputTokens = new Token[inputTokensQ.size()];

		int inp = 0;
		while(inputTokensQ.size() > 0){
			inputTokens[inp] = inputTokensQ.poll();
			inp++;
		}

		//LL parser table
		HashMap<Token.eToken, HashMap<Token.eToken, Integer> > table = genLL1_table();
		Stack<Token.eToken> stack = new Stack<>(); 

		//Initialize symbol stack
		stack.push(Token.eToken.END);
		stack.push(Token.eToken.PROG);

		//Initialize symbol index
		Integer p = 0;

		while(stack.size() > 0){
			if(p < inputTokens.length && inputTokens[p].get() == stack.peek()){
				System.out.println("Matched symbol: " + inputTokens[p].get());
				System.out.println("------|" + inputTokens[p].str() + "|------");
				p++;
				stack.pop();
			}else if(p >= inputTokens.length || table.get(stack.peek()).get(inputTokens[p].get()) == null){
				System.out.println( red + "ERROR" + white);

				// System.out.println("p-1: " + inputTokens[p-1].get());
				// System.out.println("------|" + inputTokens[p-1].str() + "|------");
				// System.out.println("p: " + inputTokens[p].get());
				// System.out.println("------|" + inputTokens[p].str() + "|------");
				// System.out.println("p+1: " + inputTokens[p+1].get());
				// System.out.println("------|" + inputTokens[p+1].str() + "|------");
				// System.out.println("p+2: " + inputTokens[p+2].get());
				// System.out.println("------|" + inputTokens[p+2].str() + "|------");

				System.out.println(Arrays.toString(stack.toArray()));

				return false;
			}else{
				Integer rule = table.get(stack.peek()).get(inputTokens[p].get());

				//TEMP
				// if(rule == 8){
				// 	System.out.println("=====================");
				// 	System.out.println("p: " + inputTokens[p].get());
				// 	System.out.println("------|" + inputTokens[p].str() + "|------");
				// 	System.out.println("=====================");
				// }
				// if(rule == 8 && p+2 < inputTokens.length && inputTokens[p+2].get() == Token.eToken.tok_proc){
				// 	rule = 4;
				// 	System.out.println("==========Rule 4===========");
				// }
				// System.out.println("p: " + p + "\tlen: " + inputTokens.length);
				// if(rule == 9 && p+1 < inputTokens.length && inputTokens[p+1].get() == Token.eToken.tok_close_brace){
				// 	// rule = 3;
				// 	System.out.println("==========Rule 9===========");
				// }
				if(rule == 14 && p+1 < inputTokens.length && inputTokens[p+1].get() == Token.eToken.tok_assignment){
					rule = 15;
					System.out.println("==========Rule 15===========");
				}

				System.out.println("Rule: " + rule);
				switch(rule){
					case 1:	// PROG → CODE PROC_DEFS_PART
						stack.pop();
						stack.push(Token.eToken.PROC_DEFS_PART);
						stack.push(Token.eToken.CODE);
						break;
					case 2:	// PROC_DEFS_PART → ; PROC_DEFS
						stack.pop();
						stack.push(Token.eToken.PROC_DEFS);
						stack.push(Token.eToken.tok_semi_colon);
						break;
					case 3:	// PROC_DEFS_PART → ε
						stack.pop();
						break;

					case 4:// PROC_DEFS → PROC PROC_DEFS_PART2
						stack.pop();
						stack.push(Token.eToken.PROC_DEFS_PART2);
						stack.push(Token.eToken.PROC);
						break;
					case 5:// PROC_DEFS_PART2 → PROC_DEFS
						stack.pop();
						stack.push(Token.eToken.PROC_DEFS);
						break;
					case 6:	// PROC_DEFS_PART2 → ε
						stack.pop();
						break;

					case 7:// PROC → proc userDefinedIdentifier { PROG }
						stack.pop();
						stack.push(Token.eToken.tok_close_brace);
						stack.push(Token.eToken.PROG);
						stack.push(Token.eToken.tok_open_brace);
						stack.push(Token.eToken.tok_user_defined_identifier);
						stack.push(Token.eToken.tok_proc);
						break;

					case 8:// CODE → INSTR CODE_PART
						stack.pop();
						stack.push(Token.eToken.CODE_PART);
						stack.push(Token.eToken.INSTR);
						break;
					case 9:// CODE_PART → ; CODE
						stack.pop();
						stack.push(Token.eToken.CODE);
						stack.push(Token.eToken.tok_semi_colon);
						break;
					case 10:// CODE_PART → ε
						stack.pop();
						break;

					case 11:// INSTR → halt
						stack.pop();
						stack.push(Token.eToken.tok_halt);
						break;
					case 12:// INSTR → DECL
						stack.pop();
						stack.push(Token.eToken.DECL);
						break;
					case 13:// INSTR → IO
						stack.pop();
						stack.push(Token.eToken.IO);
						break;
					case 14:// INSTR → CALL
						stack.pop();
						stack.push(Token.eToken.CALL);
						break;
					case 15:// INSTR → ASSIGN
						stack.pop();
						stack.push(Token.eToken.ASSIGN);
						break;
					case 16:// INSTR → COND_BRANCH
						stack.pop();
						stack.push(Token.eToken.COND_BRANCH);
						break;
					case 17:// INSTR → COND_LOOP
						stack.pop();
						stack.push(Token.eToken.COND_LOOP);
						break;

					case 18:// IO → input ( VAR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_input);
						break;
					case 19:// IO → output ( VAR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_output);
						break;

					case 20:// DECL → TYPE NAME DECL_PART
						stack.pop();
						stack.push(Token.eToken.DECL_PART);
						stack.push(Token.eToken.NAME);
						stack.push(Token.eToken.TYPE);
						break;
					case 21:// DECL_PART → ; DECL
						stack.pop();
						stack.push(Token.eToken.DECL);
						stack.push(Token.eToken.tok_semi_colon);
						break;
					case 22:// DECL_PART → ε
						stack.pop();
						break;

					case 23:// TYPE → num
						stack.pop();
						stack.push(Token.eToken.tok_num);
						break;
					case 24:// TYPE → string
						stack.pop();
						stack.push(Token.eToken.tok_string);
						break;
					case 25:// TYPE → bool
						stack.pop();
						stack.push(Token.eToken.tok_bool);
						break;

					case 26:// CALL → userDefinedIdentifier
						stack.pop();
						stack.push(Token.eToken.tok_user_defined_identifier);
						break;

					case 27:// NAME → userDefinedIdentifier
						stack.pop();
						stack.push(Token.eToken.tok_user_defined_identifier);
						break;

					case 28:// VAR → userDefinedIdentifier
						stack.pop();
						stack.push(Token.eToken.tok_user_defined_identifier);
						break;

					case 29:// ASSIGN → VAR = VALUE_PART
						stack.pop();
						stack.push(Token.eToken.VALUE_PART);
						stack.push(Token.eToken.tok_assignment);
						stack.push(Token.eToken.VAR);
						break;
					case 30:// VALUE_PART → stringLiteral
						stack.pop();
						stack.push(Token.eToken.tok_string_literal);
						break;
					case 31:// VALUE_PART → VAR
						stack.pop();
						stack.push(Token.eToken.VAR);
						break;
					case 32:// VALUE_PART → NUMEXPR
						stack.pop();
						stack.push(Token.eToken.NUMEXPR);
						break;
					case 33:// VALUE_PART → BOOL
						stack.pop();
						stack.push(Token.eToken.BOOL);
						break;

					case 34:// NUMEXPR → VAR
						stack.pop();
						stack.push(Token.eToken.VAR);
						break;
					case 35:// NUMEXPR → integerLiteral
						stack.pop();
						stack.push(Token.eToken.tok_integer_literal);
						break;
					case 36:// NUMEXPR → CALC
						stack.pop();
						stack.push(Token.eToken.CALC);
						break;

					case 37:// CALC → add ( NUMEXPR , NUMEXPR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.NUMEXPR);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.NUMEXPR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_add);
						break;
					case 38:// CALC → sub ( NUMEXPR , NUMEXPR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.NUMEXPR);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.NUMEXPR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_sub);
						break;
					case 39:// CALC → mult ( NUMEXPR , NUMEXPR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.NUMEXPR);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.NUMEXPR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_mult);
						break;

					case 40:// COND_BRANCH → if ( BOOL ) then { CODE } ELSE_PART
						stack.pop();
						stack.push(Token.eToken.ELSE_PART);
						stack.push(Token.eToken.tok_close_brace);
						stack.push(Token.eToken.CODE);
						stack.push(Token.eToken.tok_open_brace);
						stack.push(Token.eToken.tok_then);
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.BOOL);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_if);	
						break;

					case 41:// ELSE_PART → else { CODE }
						stack.pop();
						stack.push(Token.eToken.tok_close_brace);
						stack.push(Token.eToken.CODE);
						stack.push(Token.eToken.tok_open_brace);
						stack.push(Token.eToken.tok_else);
						break;
					case 42:// ELSE_PART → ε
						stack.pop();
						break;



					case 43:// BOOL → eq ( VAR , VAR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_eq);System.out.println("p+1: " + inputTokens[p+1].get());
						// System.out.println("------|" + inputTokens[p+1].str() + "|------");
						// System.out.println("p+2: " + inputTokens[p+2].get());
						// System.out.println("------|" + inputTokens[p+2].str() + "|------");
						break;

					case 44:// BOOL → ( VAR BOOL2
						stack.pop();
						stack.push(Token.eToken.BOOL2);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						break;

					case 45:// BOOL2 → < VAR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_less_than);
						break;
					case 46:// BOOL2 → > VAR )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_greater_than);
						break;
					case 47:// BOOL → not BOOL
						stack.pop();
						stack.push(Token.eToken.BOOL);
						stack.push(Token.eToken.tok_not);
						break;
					case 48:// BOOL → and ( BOOL , BOOL )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_and);
						break;
					case 49:// BOOL → or ( BOOL , BOOL )
						stack.pop();
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_or);
						break;
					case 50:// BOOL → T
						stack.pop();
						stack.push(Token.eToken.tok_T);
						break;
					case 51:// BOOL → F
						stack.pop();
						stack.push(Token.eToken.tok_F);
						break;
					case 52:// BOOL → VAR
						stack.pop();
						stack.push(Token.eToken.VAR);
						break;

					case 53:// COND_LOOP → while ( BOOL ) { CODE }
						stack.pop();
						stack.push(Token.eToken.tok_close_brace);
						stack.push(Token.eToken.CODE);
						stack.push(Token.eToken.tok_open_brace);
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.BOOL);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_while);
						break;
					case 54:// COND_LOOP → for ( VAR = 0 ; VAR < VAR ; VAR = add ( VAR , 1 ) ) { CODE }
						stack.pop();
						stack.push(Token.eToken.tok_close_brace);
						stack.push(Token.eToken.CODE);
						stack.push(Token.eToken.tok_open_brace);
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.tok_close_parenth);
						stack.push(Token.eToken.tok_integer_literal);
						stack.push(Token.eToken.tok_comma);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_add);
						stack.push(Token.eToken.tok_assignment);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_semi_colon);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_less_than);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_semi_colon);
						stack.push(Token.eToken.tok_integer_literal);
						stack.push(Token.eToken.tok_assignment);
						stack.push(Token.eToken.VAR);
						stack.push(Token.eToken.tok_open_parenth);
						stack.push(Token.eToken.tok_for);
						break;
					default:
						System.out.println("Parsing table defaulted");
						return false;
				};
			}
		}
		return true;

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

	private static void line(char c){
		if(logging){
			String ln = "";
			for(int i = 0; i < 30; ++i) ln += c;
			System.out.println(ln);
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
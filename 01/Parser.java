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
			}else{
				System.out.println("Rule: " + table[stack.peek()][inputTokens[p]]);
				switch(table[stack.peek()][inputTokens[p]]){
					case 1:	// PROG → CODE
						break;
					case 2:// PROG → CODE ; PROC_DEFS
						break;
					case 3:// PROC_DEFS → PROC
						break;
					case 4:// PROC_DEFS → PROC PROC_DEFS
						break;
					case 5:// PROC → proc userDefinedIdentifier { PROG }
						break;
					case 6:// CODE → INSTR
						break;
					case 7:// CODE → INSTR ; CODE
						break;

					case 8:// INSTR → halt
						break;
					case 9:// INSTR → DECL
						break;
					case 10:// INSTR → IO
						break;
					case 11:// INSTR → CALL
						break;
					case 12:// INSTR → ASSIGN
						break;
					case 13:// INSTR → COND_BRANCH
						break;
					case 14:// INSTR → COND_LOOP
						break;

					case 15:// IO → input ( VAR )
						break;
					case 16:// IO → output ( VAR )
						break;

					case 17:// CALL → userDefinedIdentifier
						break;

					case 18:// DECL → TYPE NAME
						break;
					case 19:// DECL → TYPE NAME ; DECL
						break;

					case 20:// TYPE → num
						break;
					case 21:// TYPE → string
						break;
					case 22:// TYPE → bool
						break;

					case 23:// NAME → userDefinedIdentifier
						break;

					case 24:// VAR → userDefinedIdentifier
						break;

					case 25:// ASSIGN → VAR = stringLiteral
						break;
					case 26:// ASSIGN → VAR = VAR
						break;
					case 27:// ASSIGN → VAR = NUMEXPR
						break;
					case 28:// ASSIGN → VAR = BOOL
						break;

					case 29:// NUMEXPR → VAR
						break;
					case 30:// NUMEXPR → integerLiteral
						break;
					case 31:// NUMEXPR → CALC
						break;

					case 32:// CALC → add ( NUMEXPR , NUMEXPR )
						break;
					case 33:// CALC → sub ( NUMEXPR , NUMEXPR )
						break;
					case 34:// CALC → mult ( NUMEXPR , NUMEXPR )
						break;

					case 35:// COND_BRANCH → if ( BOOL ) then { CODE }
						break;
					case 36:// COND_BRANCH → if ( BOOL ) then { CODE } else { CODE }
						break;

					case 37:// BOOL → eq ( VAR , VAR )
						break;
					case 38:// BOOL → ( VAR < VAR )
						break;
					case 39:// BOOL → ( VAR > VAR )
						break;
					case 40:// BOOL → not BOOL
						break;
					case 41:// BOOL → and ( BOOL , BOOL )
						break;
					case 42:// BOOL → or ( BOOL , BOOL )
						break;
					case 43:// BOOL → T
						break;
					case 44:// BOOL → F
						break;
					case 45:// BOOL → VAR
						break;

					case 46:// COND_LOOP → while ( BOOL ) { CODE }
						break;
					case 47:// COND_LOOP → for ( VAR = 0 ; VAR < VAR ; VAR = add ( VAR , 1 ) ) { CODE }
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

		//PROG	-	halt, num, string, bool, input, output, uDefLit, if, while, for
		// table[Lexer.Token.PROG][Lexer.Token.tok_halt] = 0;
		// table[Lexer.Token.PROG][Lexer.Token.tok_num] = 0;


		// table[Lexer.Token.PROC_DEFS][Lexer.Token.tok_proc] = 0;
		table[Lexer.Token.PROC][Lexer.Token.tok_proc] = 5;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		//NAME
		table[Lexer.Token.NAME][Lexer.Token.tok_user_defined_identifier] = 23;
		//VAR
		table[Lexer.Token.VAR][Lexer.Token.tok_user_defined_identifier] = 24;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		//CODE_BRANCH	-	if
		// table[Lexer.Token.CODE_BRANCH][Lexer.Token.tok_if] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;

		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		table[Lexer.Token.PROG][Lexer.Token.tok_] = 0;
		


		return table;
	}


}
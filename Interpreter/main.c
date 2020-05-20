#include <stdio.h>
#include <stdlib.h>

#define bool int
#define true 1
#define false 0
#define size_t unsigned long

bool is_digit(char c) { return '0' <= c && c <= '9'; }
bool is_letter(char c) { return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'); }
bool is_whitespace(char c) { return c == ' ' || c == '\n' || c == '\r' || c == '\t'; }
size_t str_count(const char *str) {
  int result = 0;
  while (str[result] != 0) { result++; };
  return result;
}
void str_copy(char *result, const char *source) {
  int idx = 0;
  do {
    result[idx] = source[idx];
  } while (source[idx++] != 0);
}
bool str_equal(const char *a, const char *b) {
  int idx = 0;
  while (a[idx] == b[idx]) {
    if (a[idx++] == 0) return true;
  }
  return false;
}

bool read_number(char *source, size_t size, int *cursor, bool is_signed, int *result) {
  char number[11];
  int num_idx = 0;
  if (is_signed && source[*cursor] == '-')
    number[num_idx++] = '-';
  while (*cursor + num_idx < size) {
    if (num_idx > 16) {
      printf("ERROR: Integers must be a maximum of 11 characters.\n");
      abort();
    }
    char c = source[*cursor + num_idx];
    if (is_digit(c))
      number[num_idx++] = c;
    else
      break;
  }
  if (num_idx > 0) {
    if (num_idx == 1 && number[0] == '-') {
      return false;
    }
    number[num_idx] = '\0';
    *result = atoi(number);
    *cursor += num_idx;
    return true;
  } else {
    return false;
  }
}

bool read_identifier(char *source, size_t size, int *cursor, char **result) {
  char identifier[32];
  int var_idx = 0;
  while (*cursor + var_idx < size) {
    if (var_idx > 32) {
      printf("ERROR: Identifiers must be a maximum of 32 characters.\n");
      abort();
    }
    char c = source[*cursor + var_idx];
    if (is_letter(c) || c == '$') {
      identifier[var_idx++] = c;
    } else if (is_digit(c) && var_idx != 0) {
      identifier[var_idx++] = c;
    } else {
      break;
    }
  }
  if (var_idx == 0) {
    return false;
  } else {
    identifier[var_idx] = 0;
    *result = malloc(var_idx + 1);
    str_copy(*result, identifier);
    *cursor += var_idx;
    return true;
  }
}

bool read_keyword(char *source, size_t size, int *cursor, char *kw) {
  char *result;
  int stored_cursor = *cursor;
  read_identifier(source, size, cursor, &result);
  bool is_eq = str_equal(kw, result);
  free(result);
  if (!is_eq) *cursor = stored_cursor;
  return is_eq;
}

bool read_string(char *source, size_t size, int *cursor, char **result) {
  char store[9];
  bool in_str = false;
  if (source[*cursor] == '"') {
    *cursor += 1;
    in_str = true;
  } else {
    return false;
  }
  
  int str_idx = 0;
  while (*cursor + str_idx < size) {
    if (str_idx > 8) {
      printf("ERROR: Strings must be a maximum of 8 characters.\n");
      abort();
    }
    char c = source[*cursor + str_idx];
    if (c != '"') {
      store[str_idx++] = c;
    } else {
      break;
    }
  }
  
  if (str_idx == 0 && !in_str) {
    return false;
  } else if (str_idx > 0) {
    store[str_idx] = 0;
    *result = malloc(str_idx + 1);
    str_copy(*result, store);
    *cursor += str_idx + 1; // plus the '"'
    return true;
  }
  return false;
}

bool read_op(char *source, size_t size, int *cursor, char *op) {
  if (*cursor >= size) return false;
  char c = source[*cursor];
  switch (c) {
    case '+': case '-': case '*': case '<': case '>': case '=':
      *cursor += 1;
      *op = c;
      return true;
    default:
      return false;
  }
}

void read_whitespace(char *source, size_t size, int *cursor) {
  while (true) {
    char c = source[*cursor];
    if (is_whitespace(c)) {
      *cursor += 1;
    } else {
      break;
    }
  }
}

typedef enum {
  TK_IDENTIFIER, TK_INTEGER, TK_STRING, TK_OP,
  TK_GOTO, TK_GOSUB, TK_INPUT, TK_PRINT, TK_RETURN, TK_END,
  TK_IF, TK_THEN, TK_NOT
} TokenKind;

typedef struct {
  union { char *text; int number; } data;
  TokenKind kind;
} Token;

TokenKind token_kind(char *text) {
  if (str_equal(text, "GOTO"))
    return TK_GOTO;
  else if (str_equal(text, "GOSUB"))
    return TK_GOSUB;
  else if (str_equal(text, "INPUT"))
    return TK_INPUT;
  else if (str_equal(text, "PRINT"))
    return TK_PRINT;
  else if (str_equal(text, "RETURN"))
    return TK_RETURN;
  else if (str_equal(text, "IF"))
    return TK_IF;
  else if (str_equal(text, "THEN"))
    return TK_THEN;
  else if (str_equal(text, "NOT"))
    return TK_NOT;
  else if (str_equal(text, "END"))
    return TK_END;
  
  return TK_IDENTIFIER;
}

Token **parse_tokens(char *source, size_t size) {
  int capacity = 128;
  int count = 0;
  Token **result = malloc(sizeof(Token *) * capacity);
  
  int cursor = 0;
  char *temp;
  char op[1];
  int num;
  while (true) {
    read_whitespace(source, size, &cursor);
    Token *token = malloc(sizeof(Token));
    if (read_identifier(source, size, &cursor, &temp)) {
      token->data.text = temp;
      token->kind = token_kind(temp);
    } else if (read_number(source, size, &cursor, true, &num)) {
      token->data.number = num;
      token->kind = TK_INTEGER;
    } else if (read_string(source, size, &cursor, &temp)) {
      token->data.text = temp;
      token->kind = TK_STRING;
    } else if (read_op(source, size, &cursor, op)) {
      token->data.text = malloc(1);
      token->data.text[0] = op[0];
      token->kind = TK_OP;
    } else if (source[cursor] == 0 || source[cursor] == EOF) {
      free(token);
      break;
    } else {
      printf("ERROR: Unexpected character '%c'\n", source[cursor]);
      abort();
    }
    
    if (count + 1 >= capacity) {
      capacity = (int)(capacity * 1.618);
      result = realloc(result, sizeof(Token *) * capacity);
    }
    result[count++] = token;
  }
  
  result[count] = 0;
  return result;
}

void free_tokens(Token **tokens) {
  int idx = 0;
  while (tokens[idx] != 0) {
    free(tokens[idx++]);
  }
  free(tokens);
}

typedef enum {
  SK_COMMAND, SK_JUMP, SK_ASSIGN, SK_NEGATE, SK_OP
} StmtKind;

typedef struct {
  TokenKind op;
  Token arg;
} StmtCommand;

typedef struct {
  Token cond, target;
} StmtJump;

typedef struct {
  Token target, source;
} StmtAssign;

typedef struct {
  Token target, a, b;
  char op;
} StmtOp;

typedef struct {
  union { StmtCommand command; StmtJump jump; StmtAssign assign; StmtOp op; } stmt;
  int line_number;
  StmtKind kind;
} Stmt;

Stmt *parse_command(Token **tokens, int *cursor, TokenKind op) {
  *cursor += 1; // eat '[op]'
  Token *t = tokens[*cursor];
  if (t != 0) {
    if (op == TK_GOTO || op == TK_GOSUB) {
      if (t->kind == TK_INTEGER) {
        if (t->data.number < 0) {
          printf("ERROR: Cannot have negative line number.\n");
          abort();
        }
      } else {
        printf("ERROR: Expected line number.\n");
        abort();
      }
    } else if (op == TK_PRINT || op == TK_INPUT) {
      if (t->kind != TK_IDENTIFIER) {
        printf("ERROR: Expected identifier.\n");
        abort();
      }
    }
  }
  
  
  Stmt *result = malloc(sizeof(Stmt));
  result->kind = SK_COMMAND;
  StmtCommand command;
  command.op = op;
  if (t != 0) command.arg = *t;
  result->stmt.command = command;
  if (op != TK_RETURN && op != TK_END) {
    *cursor += 1;
  }
  return result;
}

Stmt *parse_jump(Token **tokens, int *cursor) {
  *cursor += 1; // eat 'if'
  
  if (tokens[*cursor]->kind != TK_IDENTIFIER) {
    printf("ERROR: Expected identifer after 'IF'.\n");
    abort();
  }
  Token cond = *tokens[*cursor];
  *cursor += 1;
  
  if (tokens[*cursor]->kind != TK_THEN) {
    printf("ERROR: Expected THEN keyword.\n");
    abort();
  }
  *cursor += 1;
  
  if (tokens[*cursor]->kind != TK_GOTO) {
    printf("ERROR: Expected GOTO keyword.\n");
    abort();
  }
  *cursor += 1;
  
  if (tokens[*cursor]->kind != TK_INTEGER) {
    printf("ERROR: Expected line number.\n");
    abort();
  }
  Token line = *tokens[*cursor];
  *cursor += 1;
  
  Stmt *result = malloc(sizeof(Stmt));
  result->kind = SK_JUMP;
  StmtJump jump;
  jump.cond = cond;
  jump.target = line;
  result->stmt.jump = jump;
  return result;
}

Stmt *parse_assignment(Token **tokens, int *cursor) {
  Token target = *tokens[*cursor];
  *cursor += 1; // eat target
  
  if (tokens[*cursor]->kind != TK_OP) {
    printf("ERROR: Expected '='.\n");
    abort();
  } else if (tokens[*cursor]->data.text[0] != '=') {
    printf("ERROR: Expected '='.\n");
    abort();
  }
  *cursor += 1;
  
  if (tokens[*cursor]->kind == TK_NOT) {
    *cursor += 1;
    if (tokens[*cursor]->kind != TK_IDENTIFIER && tokens[*cursor]->kind != TK_INTEGER) {
      printf("ERROR: Expected identifier after NOT.\n");
      abort();
    }
    Token source = *tokens[*cursor];
    *cursor += 1;
    
    Stmt *result = malloc(sizeof(Stmt));
    result->kind = SK_NEGATE;
    StmtAssign assign;
    assign.target = target;
    assign.source = source;
    result->stmt.assign = assign;
    return result;
  }
  
  if (tokens[*cursor]->kind == TK_STRING) {
    Token a = *tokens[*cursor];
    *cursor += 1;
    if (tokens[*cursor]->kind != TK_OP) {
      Stmt *result = malloc(sizeof(Stmt));
      result->kind = SK_ASSIGN;
      StmtAssign assign;
      assign.target = target;
      assign.source = a;
      result->stmt.assign = assign;
      return result;
    } else if (tokens[*cursor]->data.text[0] != '=') {
      Token b = *tokens[*cursor];
      *cursor += 1;
      Stmt *result = malloc(sizeof(Stmt));
      result->kind = SK_OP;
      StmtOp op;
      op.target = target;
      op.op = '=';
      op.a = a;
      op.b = b;
      result->stmt.op = op;
      return result;
    } else {
      printf("ERROR: Expected '=' for string comparison.\n");
      abort();
    }
  }
  
  Token a = *tokens[*cursor];
  if (a.kind != TK_IDENTIFIER && a.kind != TK_INTEGER) {
    printf("ERROR: Expected variable, integer or string.\n");
    abort();
  }
  *cursor += 1;
  
  Token operator_token = *tokens[*cursor];
  if (operator_token.kind != TK_OP) {
    Stmt *result = malloc(sizeof(Stmt));
    result->kind = SK_ASSIGN;
    StmtAssign assign;
    assign.source = a;
    assign.target = target;
    result->stmt.assign = assign;
    return result;
  }
  char operator = operator_token.data.text[0];
  *cursor += 1;
  
  Token b = *tokens[*cursor];
  if (b.kind != TK_IDENTIFIER && b.kind != TK_INTEGER) {
    printf("ERROR: Expected variable or integer.\n");
    abort();
  }
  *cursor += 1;
  
  Stmt *result = malloc(sizeof(Stmt));
  result->kind = SK_OP;
  StmtOp op;
  op.a = a;
  op.b = b;
  op.op = operator;
  op.target = target;
  result->stmt.op = op;
  return result;
}

Stmt **parse_statements(Token **tokens) {
  int capacity = 32;
  int count = 0;
  Stmt **result = malloc(sizeof(Stmt *) * capacity);
  
  int cursor = 0;
  while (tokens[cursor] != 0) {
    int line_number;
    if (tokens[cursor]->kind != TK_INTEGER) {
      printf("ERROR: Expected line number.\n");
      abort();
    } else {
      line_number = tokens[cursor++]->data.number;
    }
    if (tokens[cursor] == 0) break;
    Stmt *stmt;
    switch (tokens[cursor]->kind) {
      case TK_GOTO:
        stmt = parse_command(tokens, &cursor, TK_GOTO);
        break;
      case TK_INPUT:
        stmt = parse_command(tokens, &cursor, TK_INPUT);
        break;
      case TK_PRINT:
        stmt = parse_command(tokens, &cursor, TK_PRINT);
        break;
      case TK_GOSUB:
        stmt = parse_command(tokens, &cursor, TK_GOSUB);
        break;
      case TK_RETURN:
        stmt = parse_command(tokens, &cursor, TK_RETURN);
        break;
      case TK_END:
        stmt = parse_command(tokens, &cursor, TK_END);
        break;
      case TK_IF:
        stmt = parse_jump(tokens, &cursor);
        break;
      case TK_IDENTIFIER:
        stmt = parse_assignment(tokens, &cursor);
        break;
      default:
        printf("ERROR: Unexpected token.\n");
        abort();
    }
    
    stmt->line_number = line_number;
    
    if (count + 1 >= capacity) {
      capacity = (int)(capacity * 1.618);
      result = realloc(result, sizeof(Stmt *) * capacity);
    }
    result[count++] = stmt;
//    printf("<%d>::<%d>\n", stmt->kind, cursor);
  }
  
  result[count] = 0;
  return result;
}

void free_stmts(Stmt **stmts) {
  int idx = 0;
  while (stmts[idx] != 0) {
    free(stmts[idx++]);
  }
  free(stmts);
}

typedef struct {
  char name[32];
  union { int number; char text[9]; } value;
} Var;

typedef struct {
  Stmt **stmts;
  int *sub_stack;
  Var *vars;
  int *sub_count, *var_count;
} ProgramState;

Var *find_var(char *name, Var *vars, int size) {
  for (int i = 0; i < size; i++) {
    if (str_equal(vars[i].name, name)) {
      return vars + i;
    }
  }
  return 0;
}

Var *append_var(char *name, Var *vars, int *size) {
  if (vars == 0) {
    return 0;
  }
  Var v;
  str_copy(v.name, name);
  vars[(*size)++] = v;
  return vars + (*size - 1);
}

int interpret_command(StmtCommand command, int pc, ProgramState state) {
  switch (command.op) {
    case TK_GOTO: {
      int idx = 0;
      while (state.stmts[idx] != 0) {
        if (state.stmts[idx]->line_number == command.arg.data.number) {
          return idx;
        } else {
          idx++;
        }
      }
      return -1;
    }
      
    case TK_GOSUB: {
      int idx = 0;
      while (state.stmts[idx] != 0) {
        if (state.stmts[idx]->line_number == command.arg.data.number) {
          state.sub_stack[*state.sub_count] = pc;
          *state.sub_count += 1;
          return idx;
        } else {
          idx++;
        }
      }
      return -1;
    }
      
    case TK_PRINT: {
      char *var_name = command.arg.data.text;
      size_t len = str_count(var_name);
      Var *var = find_var(var_name, state.vars, *state.var_count);
      if (var == 0) return -1;
      if (var_name[len - 1] == '$') {
        printf("%s\n", var->value.text);
      } else {
        printf("%d\n", var->value.number);
      }
    }
      return pc + 1;
      
    case TK_INPUT: {
      char *var_name = command.arg.data.text;
      size_t len = str_count(var_name);
      Var *var = find_var(var_name, state.vars, *state.var_count);
      if (var == 0) {
        var = append_var(var_name, state.vars, state.var_count);
      }
      printf("Input value for %s: ", var_name);
      if (var_name[len - 1] == '$') {
        scanf("%s", var->value.text);
      } else {
        int x;
        scanf("%d", &x);
        var->value.number = x;
      }
    }
      return pc + 1;
      
    case TK_RETURN: {
      return state.sub_stack[--(*state.sub_count)] + 1;
    }
      
    case TK_END:
      return -1;
      
    default:
      return -1;
  }
}

int interpret_jump(StmtJump jump, int pc, ProgramState state) {
  Var *cond = find_var(jump.cond.data.text, state.vars, *state.var_count);
  if (cond->value.number != 0) {
    int idx = 0;
    while (state.stmts[idx] != 0) {
      if (state.stmts[idx]->line_number == jump.target.data.number) {
        return idx;
      } else {
        idx++;
      }
    }
    return -1;
  }
  return pc + 1;
}

int interpret_assign(StmtAssign assign, bool is_negate, int pc, ProgramState state) {
  Var *target = find_var(assign.target.data.text, state.vars, *state.var_count);
  
  if (target == 0) {
    target = append_var(assign.target.data.text, state.vars, state.var_count);
  }
  
  switch (assign.source.kind) {
    case TK_STRING:
      str_copy(target->value.text, assign.source.data.text);
      break;
    case TK_INTEGER:
      target->value.number = is_negate
        ? (assign.source.data.number == 0 ? 1 : 0)
        : assign.source.data.number;
      break;
    case TK_IDENTIFIER: {
      Var *source = find_var(assign.source.data.text, state.vars, *state.var_count);
      if (source == 0) {
        printf("No var named '%s'\n", assign.source.data.text);
        return -1;
      }
      size_t assign_len = str_count(assign.source.data.text);
      size_t target_len = str_count(target->name);
      char ac = assign.source.data.text[assign_len - 1];
      char tc = target->name[target_len - 1];
      if (ac == '$' && ac == tc) {
        str_copy(target->value.text, source->value.text);
      } else {
        if (is_negate) {
          target->value.number = source->value.number == 0 ? 1 : 0;
        } else {
          target->value.number = source->value.number;
        }
      }
    }
      break;
      
    default:
      break;
  }
  
  return pc + 1;
}

int interpret_op(StmtOp op, int pc, ProgramState state) {
  Var *target = find_var(op.target.data.text, state.vars, *state.var_count);
  if (target == 0) target = append_var(op.target.data.text, state.vars, state.var_count);
  
  Var a, b;
  bool astr = false;
  bool bstr = false;
  if (op.a.kind == TK_IDENTIFIER) {
    Var *t = find_var(op.a.data.text, state.vars, *state.var_count);
    if (t) {
      a = *t;
    } else {
      return -1;
    }
    size_t alen = str_count(a.name);
    astr = a.name[alen - 1] == '$';
  } else if (op.a.kind == TK_STRING) {
    str_copy(a.value.text, op.a.data.text);
    astr = true;
  } else if (op.a.kind == TK_INTEGER) {
    a.value.number = op.a.data.number;
    astr = false;
  } else {
    return -1;
  }
  if (op.b.kind == TK_IDENTIFIER) {
    Var *t = find_var(op.b.data.text, state.vars, *state.var_count);
    if (t) {
      b = *t;
    } else {
      return -1;
    }
    size_t blen = str_count(b.name);
    bstr = b.name[blen - 1] == '$';
  } else if (op.b.kind == TK_STRING) {
    str_copy(b.value.text, op.b.data.text);
    bstr = true;
  } else if (op.b.kind == TK_INTEGER) {
    b.value.number = op.b.data.number;
    bstr = false;
  } else {
    return -1;
  }
  
  if (astr != bstr) return -1;
  
  if (astr) {
    if (op.op == '=') {
      target->value.number = str_equal(a.value.text, b.value.text);
    } else {
      return -1;
    }
  } else {
    int x = a.value.number;
    int y = b.value.number;
    switch (op.op) {
      case '=': target->value.number = x == y; break;
      case '+': target->value.number = x + y; break;
      case '-': target->value.number = x - y; break;
      case '*': target->value.number = x * y; break;
      case '<': target->value.number = x < y; break;
      case '>': target->value.number = x > y; break;
      default: return -1;
    }
  }
  
  return pc + 1;
}

void interpret(Stmt **stmts) {
  ProgramState state;
  
  state.stmts = stmts;
  state.sub_stack = malloc(sizeof(int) * 128); // subroutine stack. max of 128 calls
  state.vars = malloc(sizeof(Var) * 1024);
  state.sub_count = malloc(sizeof(int));
  state.var_count = malloc(sizeof(int));
  *state.sub_count = 0;
  *state.var_count = 0;
  
  int pc = 0;
  while (stmts[pc] != 0) {
    Stmt stmt = *stmts[pc];
    switch (stmt.kind) {
      case SK_COMMAND:
        pc = interpret_command(stmt.stmt.command, pc, state);
        break;
        
      case SK_JUMP:
        pc = interpret_jump(stmt.stmt.jump, pc, state);
        break;
        
      case SK_ASSIGN: case SK_NEGATE:
        pc = interpret_assign(stmt.stmt.assign, stmt.kind == SK_NEGATE, pc, state);
        break;
        
      case SK_OP:
        pc = interpret_op(stmt.stmt.op, pc, state);
        break;
        
      default:
        break;
    }
    
    if (pc == -1) return;
  }
  
  free(state.sub_count);
  free(state.sub_stack);
  free(state.var_count);
  free(state.vars);
}

int main(int argc, const char * argv[]) {
  if (argc != 2) {
    printf("Usage: expected file name.\n");
    abort();
  }
  
  FILE *file = fopen(argv[1], "r");
  
  if (file == 0) {
    printf("Error: Could not open file named '%s'.\n", argv[0]);
    abort();
  }
  
  fseek(file, 0, SEEK_END);
  long size = ftell(file);
  fseek(file, 0, SEEK_SET);
  char *source = malloc(size + 1);
  if (source) {
    fread(source, 1, size, file);
    source[size] = 0;
  } else {
    fclose(file);
    printf("Error.\n");
    abort();
  }
  
  
  Token **tokens = parse_tokens(source, size);
  if (tokens == 0) {
    printf("Error: Could not parse tokens.\n");
    abort();
  }
  Stmt **stmts = parse_statements(tokens);
  if (stmts == 0) {
    printf("Error: Could not parse statements.\n");
    abort();
  }
  
  interpret(stmts);
  char command[18];
  while (true) {
    printf("> ");
    scanf("%s", command);
    if (str_equal(command, "quit") || str_equal(command, "q")) {
      break;
    } else if (str_equal(command, "run") || str_equal(command, "r")) {
      interpret(stmts);
    }
  }
  
  
  free_stmts(stmts);
  free_tokens(tokens);
  
  return 0;
}

# FIRST and FOLLOW Sets for Parser Grammar

This document contains the FIRST and FOLLOW sets for the grammar used in the recursive descent parser.

## FIRST Sets

The FIRST set of a non-terminal contains all terminals that can appear as the first symbol of any string derived from that non-terminal.

### PROGRAM
- FIRST(PROGRAM) = {'{', 'class'}

### METHODS
- FIRST(METHODS) = FIRST(TYPE) = {'int', 'float', 'void', 'char', 'string', 'boolean'}

### PARAMS
- FIRST(PARAMS) = FIRST(TYPE) ∪ {ε} = {'int', 'float', 'void', 'char', 'string', 'boolean', ε}

### BODY
- FIRST(BODY) = FIRST(TYPE) ∪ FIRST(ASSIGNMENT) ∪ FIRST(CALL_METHOD) ∪ FIRST(RETURN) ∪ FIRST(WHILE) ∪ FIRST(IF) ∪ FIRST(DO_WHILE) ∪ FIRST(FOR) ∪ FIRST(SWITCH) ∪ FIRST(EXPRESSION) ∪ {'break', ε}
- = {'int', 'float', 'void', 'char', 'string', 'boolean', IDENTIFIER, 'return', 'while', 'if', 'do', 'for', 'switch', '(', '!', '-', LITERAL, 'break', ε}

### VARIABLE
- FIRST(VARIABLE) = FIRST(TYPE) = {'int', 'float', 'void', 'char', 'string', 'boolean'}

### ASSIGNMENT
- FIRST(ASSIGNMENT) = {IDENTIFIER}

### CALL_METHOD
- FIRST(CALL_METHOD) = {IDENTIFIER}

### PARAM_VALUES
- FIRST(PARAM_VALUES) = FIRST(EXPRESSION) ∪ {ε} = {IDENTIFIER, '(', '!', '-', LITERAL, ε}

### RETURN
- FIRST(RETURN) = {'return'}

### WHILE
- FIRST(WHILE) = {'while'}

### IF
- FIRST(IF) = {'if'}

### DO_WHILE
- FIRST(DO_WHILE) = {'do'}

### FOR
- FIRST(FOR) = {'for'}

### SWITCH
- FIRST(SWITCH) = {'switch'}

### STATEMENT_BLOCK
- FIRST(STATEMENT_BLOCK) = {'{', FIRST(TYPE), FIRST(ASSIGNMENT), FIRST(CALL_METHOD), FIRST(RETURN), FIRST(WHILE), FIRST(IF), FIRST(DO_WHILE), FIRST(FOR), FIRST(SWITCH), FIRST(EXPRESSION)}
- = {'{', 'int', 'float', 'void', 'char', 'string', 'boolean', IDENTIFIER, 'return', 'while', 'if', 'do', 'for', 'switch', '(', '!', '-', LITERAL}

### EXPRESSION
- FIRST(EXPRESSION) = FIRST(X) = {IDENTIFIER, '(', '!', '-', LITERAL}

### X
- FIRST(X) = FIRST(Y) = {IDENTIFIER, '(', '!', '-', LITERAL}

### Y
- FIRST(Y) = {'!', FIRST(R)} = {'!', IDENTIFIER, '(', '-', LITERAL}

### R
- FIRST(R) = FIRST(E) = {IDENTIFIER, '(', '-', LITERAL}

### E
- FIRST(E) = FIRST(A) = {IDENTIFIER, '(', '-', LITERAL}

### A
- FIRST(A) = FIRST(B) = {IDENTIFIER, '(', '-', LITERAL}

### B
- FIRST(B) = {'-', FIRST(C)} = {'-', IDENTIFIER, '(', LITERAL}

### C
- FIRST(C) = {IDENTIFIER, '(', LITERAL}

### TYPE
- FIRST(TYPE) = {'int', 'float', 'void', 'char', 'string', 'boolean'}

## FOLLOW Sets

The FOLLOW set of a non-terminal A contains all terminals that can appear immediately to the right of A in any sentential form.

### PROGRAM
- FOLLOW(PROGRAM) = {$} (end of input)

### METHODS
- FOLLOW(METHODS) = FOLLOW(TYPE) ∪ {'}'}
- = {'int', 'float', 'void', 'char', 'string', 'boolean', '}'}

### PARAMS
- FOLLOW(PARAMS) = {')'}

### BODY
- FOLLOW(BODY) = {'}', 'break', 'case', 'default'}

### VARIABLE
- FOLLOW(VARIABLE) = {';'}

### ASSIGNMENT
- FOLLOW(ASSIGNMENT) = {';'}

### CALL_METHOD
- FOLLOW(CALL_METHOD) = {';', '+', '-', '*', '/', ')', '<', '>', '==', '!=', '&&', '||', ','}

### PARAM_VALUES
- FOLLOW(PARAM_VALUES) = {')'}

### RETURN
- FOLLOW(RETURN) = {'}', 'break', 'case', 'default'}

### WHILE
- FOLLOW(WHILE) = {'}', ';', 'else', 'break', 'case', 'default'}

### IF
- FOLLOW(IF) = {'}', ';', 'else', 'break', 'case', 'default'}

### DO_WHILE
- FOLLOW(DO_WHILE) = {'}', ';', 'else', 'break', 'case', 'default'}

### FOR
- FOLLOW(FOR) = {'}', ';', 'else', 'break', 'case', 'default'}

### SWITCH
- FOLLOW(SWITCH) = {'}', ';', 'else', 'break', 'case', 'default'}

### STATEMENT_BLOCK
- FOLLOW(STATEMENT_BLOCK) = {'}', ';', 'else', 'while', 'break', 'case', 'default'}

### EXPRESSION
- FOLLOW(EXPRESSION) = {';', ')', ',', ':'}

### X
- FOLLOW(X) = FOLLOW(EXPRESSION) = {';', ')', ',', ':'}

### Y
- FOLLOW(Y) = {'||'} ∪ FOLLOW(X) = {'||', ';', ')', ',', ':'}

### R
- FOLLOW(R) = {'&&'} ∪ FOLLOW(Y) = {'&&', '||', ';', ')', ',', ':'}

### E
- FOLLOW(E) = {'<', '>', '==', '!='} ∪ FOLLOW(R) = {'<', '>', '==', '!=', '&&', '||', ';', ')', ',', ':'}

### A
- FOLLOW(A) = {'+', '-'} ∪ FOLLOW(E) = {'+', '-', '<', '>', '==', '!=', '&&', '||', ';', ')', ',', ':'}

### B
- FOLLOW(B) = {'*', '/'} ∪ FOLLOW(A) = {'*', '/', '+', '-', '<', '>', '==', '!=', '&&', '||', ';', ')', ',', ':'}

### C
- FOLLOW(C) = FOLLOW(B) = {'*', '/', '+', '-', '<', '>', '==', '!=', '&&', '||', ';', ')', ',', ':'}

### TYPE
- FOLLOW(TYPE) = {IDENTIFIER}
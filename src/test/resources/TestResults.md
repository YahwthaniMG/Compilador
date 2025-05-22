# Parser Testing - Results

This document shows the results of running the parser with different test files containing syntax errors.

## Test 1: ConteoPares.txt
### Entry code
```java
class ConteoPares {
    // Error: Falta punto y coma entre condiciones del for
    for (int i = 0 i <= 20; i++) {
        if (i % 2 == 0) {
            String texto = "Es par.";
        }
    }
}
```
### Execution result
```
Token List:
     Value      |             Type      |       Line
----------------------------------------
     class      |          KEYWORD      |       1
ConteoPares     |       IDENTIFIER      |       1
         {      |        DELIMITER      |       1
       for      |          KEYWORD      |       3
         (      |        DELIMITER      |       3
       int      |          KEYWORD      |       3
         i      |       IDENTIFIER      |       3
         =      |         OPERATOR      |       3
         0      |          INTEGER      |       3
         i      |       IDENTIFIER      |       3
        <=      |         OPERATOR      |       3
        20      |          INTEGER      |       3
         ;      |        DELIMITER      |       3
         i      |       IDENTIFIER      |       3
        ++      |         OPERATOR      |       3
         )      |        DELIMITER      |       3
         {      |        DELIMITER      |       3
        if      |          KEYWORD      |       4
         (      |        DELIMITER      |       4
         i      |       IDENTIFIER      |       4
         %      |         OPERATOR      |       4
         2      |          INTEGER      |       4
        ==      |         OPERATOR      |       4
         0      |          INTEGER      |       4
         )      |        DELIMITER      |       4
         {      |        DELIMITER      |       4
    String      |          KEYWORD      |       5
     texto      |       IDENTIFIER      |       5
         =      |         OPERATOR      |       5
 "Es par."      |           STRING      |       5
         ;      |        DELIMITER      |       5
         }      |        DELIMITER      |       6
         }      |        DELIMITER      |       7
         }      |        DELIMITER      |       8
- RULE_PROGRAM
-- class
--- IDENTIFIER: ConteoPares
---- {
Recovered: Skipped to next valid member declaration
--- RULE_VARIABLE
----- RULE_TYPE
----- TYPE: int
--- IDENTIFIER: i
--- =
--- RULE_EXPRESSION
---- RULE_X
----- RULE_Y
------ RULE_R
------- RULE_E
-------- RULE_A
--------- RULE_B
---------- RULE_C
---------- LITERAL: 0
Recovered: Skipped to next statement after missing ';'
---- }
Syntax Error 999 at line 7, token: } (DELIMITER)
Recovery: Skipping trailing tokens after valid program
Parsing completed with recovery.
```
### Analysis
The Parser detects that the FOR function is missing a “;” so it signals this error and skips to the next valid option, until it finishes going through the whole file and indicates that there was a syntactic error.
## Test 2: Suma.txt
### Entry Code
```java
class Suma {
    int a = 25;
    int b = 67;
    void suma_dos_numero( int a, int b){
        int suma = a + b;
    //Falta la llave de cierre
}
```
### Execution result
```
Token List:
     Value      |             Type      |       Line
----------------------------------------
     class      |          KEYWORD      |       1
      Suma      |       IDENTIFIER      |       1
         {      |        DELIMITER      |       1
       int      |          KEYWORD      |       2
         a      |       IDENTIFIER      |       2
         =      |         OPERATOR      |       2
        25      |          INTEGER      |       2
         ;      |        DELIMITER      |       2
       int      |          KEYWORD      |       3
         b      |       IDENTIFIER      |       3
         =      |         OPERATOR      |       3
        67      |          INTEGER      |       3
         ;      |        DELIMITER      |       3
      void      |          KEYWORD      |       4
suma_dos_numero |       IDENTIFIER      |       4
         (      |        DELIMITER      |       4
       int      |          KEYWORD      |       4
         a      |       IDENTIFIER      |       4
         ,      |        DELIMITER      |       4
       int      |          KEYWORD      |       4
         b      |       IDENTIFIER      |       4
         )      |        DELIMITER      |       4
         {      |        DELIMITER      |       4
       int      |          KEYWORD      |       5
      suma      |       IDENTIFIER      |       5
         =      |         OPERATOR      |       5
         a      |       IDENTIFIER      |       5
         +      |         OPERATOR      |       5
         b      |       IDENTIFIER      |       5
         ;      |        DELIMITER      |       5
         }      |        DELIMITER      |       7
- RULE_PROGRAM
-- class
--- IDENTIFIER: Suma
---- {
--- RULE_VARIABLE
----- RULE_TYPE
----- TYPE: int
--- IDENTIFIER: a
--- =
--- RULE_EXPRESSION
---- RULE_X
----- RULE_Y
------ RULE_R
------- RULE_E
-------- RULE_A
--------- RULE_B
---------- RULE_C
---------- LITERAL: 25
---- ;
--- RULE_VARIABLE
----- RULE_TYPE
----- TYPE: int
--- IDENTIFIER: b
--- =
--- RULE_EXPRESSION
---- RULE_X
----- RULE_Y
------ RULE_R
------- RULE_E
-------- RULE_A
--------- RULE_B
---------- RULE_C
---------- LITERAL: 67
---- ;
----- RULE_METHODS
----- RULE_TYPE
----- TYPE: void
----- IDENTIFIER: suma_dos_numero
----- (
------ RULE_PARAMS
----- RULE_TYPE
----- TYPE: int
------ IDENTIFIER: a
------ ,
----- RULE_TYPE
----- TYPE: int
------ IDENTIFIER: b
----- )
----- {
-- RULE_BODY
--- RULE_VARIABLE
----- RULE_TYPE
----- TYPE: int
--- IDENTIFIER: suma
--- =
--- RULE_EXPRESSION
---- RULE_X
----- RULE_Y
------ RULE_R
------- RULE_E
-------- RULE_A
--------- RULE_B
---------- RULE_C
---------- IDENTIFIER: a
------- +
-------- RULE_A
--------- RULE_B
---------- RULE_C
---------- IDENTIFIER: b
-- ;
----- }
Syntax Error 202 at end of file
Parsing completed with recovery.
```
### Analysis
The Parser goes through the contents of the file until the end, detecting that a “}” was missing to finish evaluating all the rules it entered. 

## Test 3: Temperatura.txt
### Entry code
```java
class Temperatura {
    float fahrenheit;
    void celsiusTofahrenheit(float celsius){
        // Error: Falta el operador '=' para la asignación
        fahrenheit  (celsius * 9/5) + 32;
    }
}
```
### Execution result
```
Token List:
     Value      |             Type      |       Line
----------------------------------------
     class      |          KEYWORD      |       1
Temperatura     |       IDENTIFIER      |       1
         {      |        DELIMITER      |       1
     float      |          KEYWORD      |       2
fahrenheit      |       IDENTIFIER      |       2
         ;      |        DELIMITER      |       2
      void      |          KEYWORD      |       3
celsiusTofahrenheit     |       IDENTIFIER      |       3
         (      |        DELIMITER      |       3
     float      |          KEYWORD      |       3
   celsius      |       IDENTIFIER      |       3
         )      |        DELIMITER      |       3
         {      |        DELIMITER      |       3
fahrenheit      |       IDENTIFIER      |       5
         (      |        DELIMITER      |       5
   celsius      |       IDENTIFIER      |       5
         *      |         OPERATOR      |       5
         9      |          INTEGER      |       5
         /      |         OPERATOR      |       5
         5      |          INTEGER      |       5
         )      |        DELIMITER      |       5
         +      |         OPERATOR      |       5
        32      |          INTEGER      |       5
         ;      |        DELIMITER      |       5
         }      |        DELIMITER      |       6
         }      |        DELIMITER      |       7
- RULE_PROGRAM
-- class
--- IDENTIFIER: Temperatura
---- {
--- RULE_VARIABLE
----- RULE_TYPE
----- TYPE: float
--- IDENTIFIER: fahrenheit
---- ;
----- RULE_METHODS
----- RULE_TYPE
----- TYPE: void
----- IDENTIFIER: celsiusTofahrenheit
----- (
------ RULE_PARAMS
----- RULE_TYPE
----- TYPE: float
------ IDENTIFIER: celsius
----- )
----- {
-- RULE_BODY
--- RULE_CALL_METHOD
--- IDENTIFIER: fahrenheit
--- (
---- RULE_PARAM_VALUES
--- RULE_EXPRESSION
---- RULE_X
----- RULE_Y
------ RULE_R
------- RULE_E
-------- RULE_A
--------- RULE_B
---------- RULE_C
---------- IDENTIFIER: celsius
-------- *
--------- RULE_B
---------- RULE_C
---------- LITERAL: 9
-------- /
--------- RULE_B
---------- RULE_C
---------- LITERAL: 5
--- )
Syntax Error 1302 at line 5, token: + (OPERATOR)
-- ; (recovered)
----- }
---- }
Parsing completed with recovery.
```
### Analysis
In this example the parser analyzed the content and concluded that our line: }
fahrenheit (celsius * 9/5) + 32;
has an error, since the “=” sign was forgotten, the parser detected that the sum caused an error since it does not have all the elements it needs.
## Conclusions
Tests show that the parser is able to:

Detect common syntactic errors such as missing parentheses, semicolons, and closing braces.
Recover from errors and continue parsing.
Provide informative error messages indicating the location and nature of the problem.

The recovery mechanism based on the FIRST and FOLLOW sets allows the parser to handle code with multiple syntax errors and complete parsing without stopping abruptly.
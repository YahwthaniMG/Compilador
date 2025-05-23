package javiergs.compiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Main class to run the lexer
 *
 * @author eduardomv
 * @author santiarr
 * @author yawham
 * @version 1.5
 */

public class TheParser {

	private Vector<TheToken> tokens;
	private int currentToken;

	private Map<String, Set<String>> firstSets;
    private Map<String, Set<String>> followSets;

	public TheParser(Vector<TheToken> tokens) {
		this.tokens = tokens;
		currentToken = 0;
		initializeFirstAndFollowSets();
	}

	private void initializeFirstAndFollowSets() {
		firstSets = new HashMap<>();
		followSets = new HashMap<>();
		// Initialize FIRST sets
		initializeFirstSets();
		// Initialize FOLLOW sets
		initializeFollowSets();
	}

	private void initializeFirstSets() {
        // PROGRAM
        Set<String> programFirst = new HashSet<>(Arrays.asList("{", "class"));
        firstSets.put("PROGRAM", programFirst);
        // METHODS
        Set<String> methodsFirst = new HashSet<>(Arrays.asList("int", "float", "void", "char", "string", "boolean"));
        firstSets.put("METHODS", methodsFirst);
        // PARAMS
        Set<String> paramsFirst = new HashSet<>(Arrays.asList("int", "float", "void", "char", "string", "boolean"));
        paramsFirst.add(""); // Representing epsilon
        firstSets.put("PARAMS", paramsFirst);
        // BODY
        Set<String> bodyFirst = new HashSet<>();
        bodyFirst.addAll(methodsFirst); // Add TYPE's FIRST set
        bodyFirst.add("IDENTIFIER"); // For ASSIGNMENT and CALL_METHOD
        bodyFirst.addAll(Arrays.asList("return", "while", "if", "do", "for", "switch", "(", "!", "-"));
        bodyFirst.add("LITERAL"); // For integer, float, etc.
        bodyFirst.add("break");
        bodyFirst.add(""); // epsilon
        firstSets.put("BODY", bodyFirst);
        // VARIABLE
        firstSets.put("VARIABLE", new HashSet<>(methodsFirst));
        // ASSIGNMENT
        Set<String> assignmentFirst = new HashSet<>(Collections.singletonList("IDENTIFIER"));
        firstSets.put("ASSIGNMENT", assignmentFirst);
        // CALL_METHOD
        firstSets.put("CALL_METHOD", new HashSet<>(assignmentFirst));
        // PARAM_VALUES
        Set<String> paramValuesFirst = new HashSet<>(Arrays.asList("IDENTIFIER", "(", "!", "-", "LITERAL", ""));
        firstSets.put("PARAM_VALUES", paramValuesFirst);
        // RETURN
        firstSets.put("RETURN", new HashSet<>(Collections.singletonList("return")));
        // WHILE
        firstSets.put("WHILE", new HashSet<>(Collections.singletonList("while")));
        // IF
        firstSets.put("IF", new HashSet<>(Collections.singletonList("if")));
        // DO_WHILE
        firstSets.put("DO_WHILE", new HashSet<>(Collections.singletonList("do")));
        // FOR
        firstSets.put("FOR", new HashSet<>(Collections.singletonList("for")));
        // SWITCH
        firstSets.put("SWITCH", new HashSet<>(Collections.singletonList("switch")));
        // STATEMENT_BLOCK
        Set<String> statementBlockFirst = new HashSet<>();
        statementBlockFirst.add("{");
        statementBlockFirst.addAll(methodsFirst);
        statementBlockFirst.add("IDENTIFIER");
        statementBlockFirst.addAll(Arrays.asList("return", "while", "if", "do", "for", "switch", "(", "!", "-", "LITERAL"));
        firstSets.put("STATEMENT_BLOCK", statementBlockFirst);
        // EXPRESSION
        Set<String> expressionFirst = new HashSet<>(Arrays.asList("IDENTIFIER", "(", "!", "-", "LITERAL"));
        firstSets.put("EXPRESSION", expressionFirst);
        // X
        firstSets.put("X", new HashSet<>(expressionFirst));
        // Y
        Set<String> yFirst = new HashSet<>(expressionFirst);
        yFirst.add("!");
        firstSets.put("Y", yFirst);
        // R
        firstSets.put("R", new HashSet<>(Arrays.asList("IDENTIFIER", "(", "-", "LITERAL")));
        // E
        firstSets.put("E", new HashSet<>(Arrays.asList("IDENTIFIER", "(", "-", "LITERAL")));
        // A
        firstSets.put("A", new HashSet<>(Arrays.asList("IDENTIFIER", "(", "-", "LITERAL")));
        // B
        firstSets.put("B", new HashSet<>(Arrays.asList("-", "IDENTIFIER", "(", "LITERAL")));
        // C
        firstSets.put("C", new HashSet<>(Arrays.asList("IDENTIFIER", "(", "LITERAL")));
        // TYPE
        firstSets.put("TYPE", new HashSet<>(Arrays.asList("int", "float", "void", "char", "string", "boolean")));
    }
    
	private void initializeFollowSets() {
		// PROGRAM
		Set<String> programFollow = new HashSet<>(Collections.singletonList("$")); // End of input
		followSets.put("PROGRAM", programFollow);
		// METHODS
		Set<String> methodsFollow = new HashSet<>(
				Arrays.asList("int", "float", "void", "char", "string", "boolean", "}"));
		followSets.put("METHODS", methodsFollow);
		// PARAMS
		followSets.put("PARAMS", new HashSet<>(Collections.singletonList(")")));
		// BODY
		followSets.put("BODY", new HashSet<>(Arrays.asList("}", "break", "case", "default")));
		// VARIABLE
		followSets.put("VARIABLE", new HashSet<>(Collections.singletonList(";")));
		// ASSIGNMENT
		followSets.put("ASSIGNMENT", new HashSet<>(Collections.singletonList(";")));
		// CALL_METHOD
		followSets.put("CALL_METHOD",
				new HashSet<>(Arrays.asList(";", "+", "-", "*", "/", ")", "<", ">", "==", "!=", "&&", "||", ",")));
		// PARAM_VALUES
		followSets.put("PARAM_VALUES", new HashSet<>(Collections.singletonList(")")));
		// RETURN
		followSets.put("RETURN", new HashSet<>(Arrays.asList("}", "break", "case", "default")));
		// WHILE
		followSets.put("WHILE", new HashSet<>(Arrays.asList("}", ";", "else", "break", "case", "default")));
		// IF
		followSets.put("IF", new HashSet<>(Arrays.asList("}", ";", "else", "break", "case", "default")));
		// DO_WHILE
		followSets.put("DO_WHILE", new HashSet<>(Arrays.asList("}", ";", "else", "break", "case", "default")));
		// FOR
		followSets.put("FOR", new HashSet<>(Arrays.asList("}", ";", "else", "break", "case", "default")));
		// SWITCH
		followSets.put("SWITCH", new HashSet<>(Arrays.asList("}", ";", "else", "break", "case", "default")));
		// STATEMENT_BLOCK
		followSets.put("STATEMENT_BLOCK",
				new HashSet<>(Arrays.asList("}", ";", "else", "while", "break", "case", "default")));
		// EXPRESSION
		followSets.put("EXPRESSION", new HashSet<>(Arrays.asList(";", ")", ",", ":")));
		// X
		followSets.put("X", new HashSet<>(Arrays.asList(";", ")", ",", ":")));
		// Y
		followSets.put("Y", new HashSet<>(Arrays.asList("||", ";", ")", ",", ":")));
		// R
		followSets.put("R", new HashSet<>(Arrays.asList("&&", "||", ";", ")", ",", ":")));
		// E
		followSets.put("E", new HashSet<>(Arrays.asList("<", ">", "==", "!=", "&&", "||", ";", ")", ",", ":")));
		// A
		followSets.put("A",
				new HashSet<>(Arrays.asList("+", "-", "<", ">", "==", "!=", "&&", "||", ";", ")", ",", ":")));
		// B
		followSets.put("B",
				new HashSet<>(Arrays.asList("*", "/", "+", "-", "<", ">", "==", "!=", "&&", "||", ";", ")", ",", ":")));
		// C
		followSets.put("C",
				new HashSet<>(Arrays.asList("*", "/", "+", "-", "<", ">", "==", "!=", "&&", "||", ";", ")", ",", ":")));
		// TYPE
		followSets.put("TYPE", new HashSet<>(Collections.singletonList("IDENTIFIER")));
	}

	/**
	 * Check if the current token is in the FIRST set of a rule.
	 * @param rule The rule name to check against
	 * @return true if the current token is in the FIRST set of the rule
	 */
	private boolean isInFirstSetOf(String rule) {
		if (currentToken >= tokens.size()) {
			return false;
		}
		String tokenValue = tokens.get(currentToken).getValue();
		String tokenType = tokens.get(currentToken).getType();
		Set<String> first = firstSets.get(rule);
		// Verificar si el valor o tipo del token está directamente en el conjunto FIRST
		if (first.contains(tokenValue) || first.contains(tokenType)) {
			return true;
		}
		// Comprobar si el tipo de token es un literal y el conjunto FIRST contiene "LITERAL"
		if ((tokenType.equals("INTEGER") || tokenType.equals("FLOAT") || 
			tokenType.equals("CHAR") || tokenType.equals("STRING") || 
			tokenType.equals("HEXADECIMAL") || tokenType.equals("BINARY")) && 
			first.contains("LITERAL")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the current token is in the FOLLOW set of a rule.
	 * @param rule The rule name to check against
	 * @return true if the current token is in the FOLLOW set of the rule
	 */
	private boolean isInFollowSetOf(String rule) {
		if (currentToken >= tokens.size()) {
			return false;
		}

		String tokenValue = tokens.get(currentToken).getValue();
		String tokenType = tokens.get(currentToken).getType();

		Set<String> follow = followSets.get(rule);
		return follow.contains(tokenValue) || follow.contains(tokenType)
				|| follow.contains("$") && currentToken >= tokens.size();
	}

	/**
	 * Skip tokens until one is found in either the FIRST or FOLLOW set.
	 * @param rule The rule name to check against
	 * @param errorCode The error code to report
	 * @return true if a token in FIRST was found, false if a token in FOLLOW was found
	 */
	private boolean skipUntilFirstOrFollow(String rule, int errorCode) {
		error(errorCode);

		while (currentToken < tokens.size()) {
			if (isInFirstSetOf(rule)) {
				return true;
			}
			if (isInFollowSetOf(rule)) {
				return false;
			}
			currentToken++;
		}

		return false;
	}

	/**
	 * Enhanced error method that provides more information.
	 * @param errorCode The error code
	 */
	private void error(int errorCode) {
		if (currentToken < tokens.size()) {
			System.out.println("Syntax Error " + errorCode +
					" at line " + tokens.get(currentToken).getLineNumber() +
					", token: " + tokens.get(currentToken).getValue() +
					" (" + tokens.get(currentToken).getType() + ")");
		} else {
			System.out.println("Syntax Error " + errorCode + " at end of file");
		}
	}
	
	public void run() {
		try {
			RULE_PROGRAM();
			if (currentToken != tokens.size()) {
				error(999); // Unexpected tokens at the end
				System.out.println("Recovery: Skipping trailing tokens after valid program");
			}
			System.out.println("Parsing completed with recovery.");
		} catch (Exception e) {
			System.out.println("Critical error during parsing: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void RULE_PROGRAM() {
		System.out.println("- RULE_PROGRAM");	
		if (!isInFirstSetOf("PROGRAM")) {
			boolean foundFirst = skipUntilFirstOrFollow("PROGRAM", 200);
			if (!foundFirst) {
				System.out.println("Recovered: Cannot find valid program start token");
				return;
			}
		}
		if (tokens.get(currentToken).getValue().equals("{")) {
			currentToken++;
			System.out.println("- {");
			RULE_BODY();
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("}")) {
				currentToken++;
				System.out.println("- }");
			} else {
				// Error: Missing closing brace
				// Skip until we find a token in FOLLOW(PROGRAM)
				while (currentToken < tokens.size() && !isInFollowSetOf("PROGRAM")) {
					currentToken++;
				}
				System.out.println("Recovered: Skipped to end of program after missing '}'");
			}
		} else if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("class")) {
			currentToken++;
			System.out.println("-- class");
			if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
				System.out.println("--- IDENTIFIER: " + tokens.get(currentToken).getValue());
				currentToken++;
			} else {
				// Error: Missing class name identifier
				boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 201);
				if (!foundFirst) {
					System.out.println("Recovered: Skipping class body due to missing class name");
					return;
				}
			}
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
				currentToken++;
				System.out.println("---- {");
				while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
					if (isType()) {
						if (isMethodDeclaration()) {
							RULE_METHODS();
						} else {
							RULE_VARIABLE();
							if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								System.out.println("---- ;");
							} else {
								// Error: Missing semicolon
								// Skip to next valid statement start or class end
								while (currentToken < tokens.size() && 
									!isType() && 
									!tokens.get(currentToken).getValue().equals("}")) {
									currentToken++;
								}
								System.out.println("Recovered: Skipped to next statement after missing ';'");
							}
						}
					} else {
						// Error: Invalid class member
						// Skip to next valid statement start or class end
						boolean foundValid = false;
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals("}")) {
							if (isType()) {
								foundValid = true;
								break;
							}
							currentToken++;
						}
						if (!foundValid) {
							System.out.println("Recovered: Skipped to end of class after invalid member");
						} else {
							System.out.println("Recovered: Skipped to next valid member declaration");
						}
					}
				}
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("}")) {
					currentToken++;
					System.out.println("---- }");
				} else {
					// Error: Missing closing brace for class
					// Just report the error since we're already at the end
					error(202);
				}
			} else {
				// Error: Missing opening brace for class body
				error(203);
			}
		} else {
			// Error: Invalid program start
			error(204);
		}
	}

	private void RULE_METHODS() {
		System.out.println("----- RULE_METHODS");
		if (!isInFirstSetOf("METHODS")) {
			boolean foundFirst = skipUntilFirstOrFollow("METHODS", 700);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping METHODS rule");
				return;
			}
		}
		RULE_TYPE();
		if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			System.out.println("----- IDENTIFIER: " + tokens.get(currentToken).getValue());
			currentToken++;
		} else {
			error(8);
			// Skip until we find a "(" or something in FOLLOW(METHODS)
			while (currentToken < tokens.size() && 
				!tokens.get(currentToken).getValue().equals("(") && 
				!isInFollowSetOf("METHODS")) {
				currentToken++;
			}
			if (currentToken >= tokens.size() || !tokens.get(currentToken).getValue().equals("(")) {
				System.out.println("Recovered: Missing method name, skipping method declaration");
				return;
			}
			System.out.println("Recovered: Found opening parenthesis after missing method name");
		}
		if (tokens.get(currentToken).getValue().equals("(")) {
			currentToken++;
			System.out.println("----- (");
			RULE_PARAMS();
			if (tokens.get(currentToken).getValue().equals(")")) {
				currentToken++;
				System.out.println("----- )");
			} else {
				error(9);
				// Skip until we find a "{" or something in FOLLOW(METHODS)
				while (currentToken < tokens.size() && 
					!tokens.get(currentToken).getValue().equals("{") && 
					!isInFollowSetOf("METHODS")) {
					currentToken++;
				}
				if (currentToken >= tokens.size() || !tokens.get(currentToken).getValue().equals("{")) {
					System.out.println("Recovered: Missing closing parenthesis, skipping method declaration");
					return;
				}
				System.out.println("Recovered: Found opening brace after missing closing parenthesis");
			}
			if (tokens.get(currentToken).getValue().equals("{")) {
				currentToken++;
				System.out.println("----- {");
				RULE_BODY();
				if (tokens.get(currentToken).getValue().equals("}")) {
					currentToken++;
					System.out.println("----- }");
				} else {
					error(10);
					// Skip until we find something in FOLLOW(METHODS)
					while (currentToken < tokens.size() && !isInFollowSetOf("METHODS")) {
						currentToken++;
					}
					System.out.println("Recovered: Missing closing brace, skipping to next method or class end");
				}
			} else {
				error(11);
				// Skip until we find something in FOLLOW(METHODS)
				while (currentToken < tokens.size() && !isInFollowSetOf("METHODS")) {
					currentToken++;
				}
				System.out.println("Recovered: Missing method body, skipping to next method or class end");
			}
		} else {
			error(12);
			// Skip until we find something in FOLLOW(METHODS)
			while (currentToken < tokens.size() && !isInFollowSetOf("METHODS")) {
				currentToken++;
			}
			System.out.println("Recovered: Missing method parameter list, skipping to next method or class end");
		}
	}

	private void RULE_PARAMS() {
		System.out.println("------ RULE_PARAMS");
		// Params can be empty (epsilon), so we check if the current token is ")"
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
			// Empty parameter list is valid, do nothing
			return;
		}
		if (!isInFirstSetOf("PARAMS")) {
			boolean foundFirst = skipUntilFirstOrFollow("PARAMS", 600);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping PARAMS rule");
				return;
			}
		}
		if (isType()) {
			RULE_TYPE();
			if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
				System.out.println("------ IDENTIFIER: " + tokens.get(currentToken).getValue());
				currentToken++;
			} else {
				error(13);
				// Skip until we find a comma or ")" to continue
				while (currentToken < tokens.size() && 
					!tokens.get(currentToken).getValue().equals(",") && 
					!tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
				}
				if (currentToken >= tokens.size() || 
					(!tokens.get(currentToken).getValue().equals(",") && 
					!tokens.get(currentToken).getValue().equals(")"))) {
					System.out.println("Recovered: Malformed parameter, skipping parameter list");
					return;
				}
				System.out.println("Recovered: Found comma or closing parenthesis after missing parameter name");
			}
			while (tokens.get(currentToken).getValue().equals(",")) {
				currentToken++;
				System.out.println("------ ,");
				if (isType()) {
					RULE_TYPE();
					if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
						System.out.println("------ IDENTIFIER: " + tokens.get(currentToken).getValue());
						currentToken++;
					} else {
						error(14);
						// Skip until we find a comma or ")" to continue
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals(",") && 
							!tokens.get(currentToken).getValue().equals(")")) {
							currentToken++;
						}
						if (currentToken >= tokens.size() || 
							(!tokens.get(currentToken).getValue().equals(",") && 
							!tokens.get(currentToken).getValue().equals(")"))) {
							System.out.println("Recovered: Malformed parameter after comma, skipping parameter list");
							return;
						}
						System.out.println("Recovered: Found comma or closing parenthesis after missing parameter name");
					}
				} else {
					error(15);
					// Skip until we find a comma or ")" to continue
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals(",") && 
						!tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
					}
					if (currentToken >= tokens.size() || 
						(!tokens.get(currentToken).getValue().equals(",") && 
						!tokens.get(currentToken).getValue().equals(")"))) {
						System.out.println("Recovered: Missing parameter type after comma, skipping parameter list");
						return;
					}
					System.out.println("Recovered: Found comma or closing parenthesis after missing parameter type");
				}
			}
		}
	}

	private void RULE_BODY() {
		System.out.println("-- RULE_BODY");
		while (currentToken < tokens.size() && 
			!(tokens.get(currentToken).getValue().equals("}") || 
				tokens.get(currentToken).getValue().equals("break"))) {
			try {
				if (isType()) {
					RULE_VARIABLE();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("-- ;");
					} else {
						error(1300);
						// Skip until we find a semicolon or the start of another valid statement
						boolean recovered = false;
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals("}") && 
							!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								System.out.println("-- ; (recovered)");
								recovered = true;
								break;
							}
							// Check if we've reached the start of another valid statement
							if (isType() || isAssignment() || isMethodCall() || isReturnStatement() || 
								isWhileStatement() || isIfStatement() || isDoStatement() || 
								isForStatement() || isSwitchStatement()) {
								System.out.println("Recovered: Found start of next statement");
								recovered = true;
								break;
							}
							currentToken++;
						}
						if (!recovered) {
							System.out.println("Recovered: Reached end of body while recovering from error");
							break;
						}
					}
				} else if (isAssignment()) {
					RULE_ASSIGNMENT();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("-- ;");
					} else {
						error(1301);
						// Skip until we find a semicolon or the start of another valid statement
						boolean recovered = false;
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals("}") && 
							!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								System.out.println("-- ; (recovered)");
								recovered = true;
								break;
							}
							// Check if we've reached the start of another valid statement
							if (isType() || isAssignment() || isMethodCall() || isReturnStatement() || 
								isWhileStatement() || isIfStatement() || isDoStatement() || 
								isForStatement() || isSwitchStatement()) {
								System.out.println("Recovered: Found start of next statement");
								recovered = true;
								break;
							}
							currentToken++;
						}
						if (!recovered) {
							System.out.println("Recovered: Reached end of body while recovering from error");
							break;
						}
					}
				} else if (isMethodCall()) {
					RULE_CALL_METHOD();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("-- ;");
					} else {
						error(1302);
						// Similar recovery as above
						boolean recovered = false;
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals("}") && 
							!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								System.out.println("-- ; (recovered)");
								recovered = true;
								break;
							}
							// Check if we've reached the start of another valid statement
							if (isType() || isAssignment() || isMethodCall() || isReturnStatement() || 
								isWhileStatement() || isIfStatement() || isDoStatement() || 
								isForStatement() || isSwitchStatement()) {
								System.out.println("Recovered: Found start of next statement");
								recovered = true;
								break;
							}
							currentToken++;
						}
						if (!recovered) {
							System.out.println("Recovered: Reached end of body while recovering from error");
							break;
						}
					}
				} else if (isReturnStatement()) {
					RULE_RETURN();
				} else if (isWhileStatement()) {
					RULE_WHILE();
				} else if (isIfStatement()) {
					RULE_IF();
				} else if (isDoStatement()) {
					RULE_DO_WHILE();
				} else if (isForStatement()) {
					RULE_FOR();
				} else if (isSwitchStatement()) {
					RULE_SWITCH();
				} else {
					if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1303);
						if (!foundFirst) {
							// Try to skip to the start of another statement or the end of the body
							while (currentToken < tokens.size() && 
								!isType() && !isAssignment() && !isMethodCall() && 
								!isReturnStatement() && !isWhileStatement() && 
								!isIfStatement() && !isDoStatement() && 
								!isForStatement() && !isSwitchStatement() && 
								!tokens.get(currentToken).getValue().equals("}") && 
								!tokens.get(currentToken).getValue().equals("break")) {
								currentToken++;
							}
							
							System.out.println("Recovered: Skipped unrecognized statement");
							continue;
						}
					}
					RULE_EXPRESSION();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("-- ;");
					} else {
						error(1304);
						// Similar recovery as above
						boolean recovered = false;
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals("}") && 
							!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								System.out.println("-- ; (recovered)");
								recovered = true;
								break;
							}
							// Check if we've reached the start of another valid statement
							if (isType() || isAssignment() || isMethodCall() || isReturnStatement() || 
								isWhileStatement() || isIfStatement() || isDoStatement() || 
								isForStatement() || isSwitchStatement()) {
								System.out.println("Recovered: Found start of next statement");
								recovered = true;
								break;
							}
							currentToken++;
						}
						if (!recovered) {
							System.out.println("Recovered: Reached end of body while recovering from error");
							break;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Critical error in BODY: " + e.getMessage());
				// Skip to the next statement or end of body
				while (currentToken < tokens.size() && 
					!isType() && !isAssignment() && !isMethodCall() && 
					!isReturnStatement() && !isWhileStatement() && 
					!isIfStatement() && !isDoStatement() && 
					!isForStatement() && !isSwitchStatement() && 
					!tokens.get(currentToken).getValue().equals("}") && 
					!tokens.get(currentToken).getValue().equals("break")) {
					currentToken++;
				}
				System.out.println("Recovered from critical error in BODY");
			}
		}
	}

	private void RULE_VARIABLE() {
		System.out.println("--- RULE_VARIABLE");
		if (!isInFirstSetOf("VARIABLE")) {
			boolean foundFirst = skipUntilFirstOrFollow("VARIABLE", 500);
			
			if (!foundFirst) {
				System.out.println("Recovered: Skipping VARIABLE rule");
				return;
			}
		}
		RULE_TYPE();
		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
				System.out.println("--- IDENTIFIER: " + tokens.get(currentToken).getValue());
				currentToken++;
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("=")) {
					currentToken++;
					System.out.println("--- =");
					if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 501);
						if (!foundFirst) {
							System.out.println("Recovered: Missing expression in variable initialization");
							return;
						}
					}
					RULE_EXPRESSION();
				}
		} else {
			error(502);
			// Try to recover by finding a semicolon or the next statement
			while (currentToken < tokens.size() && 
				!tokens.get(currentToken).getValue().equals(";") && 
				!isInFollowSetOf("VARIABLE")) {
				currentToken++;
			}
			System.out.println("Recovered: Skipped to next statement after invalid variable declaration");
		}
	}

	private void RULE_ASSIGNMENT() {
		System.out.println("--- RULE_ASSIGNMENT");
		if (!isInFirstSetOf("ASSIGNMENT")) {
			boolean foundFirst = skipUntilFirstOrFollow("ASSIGNMENT", 600);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping ASSIGNMENT rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			System.out.println("--- IDENTIFIER: " + tokens.get(currentToken).getValue());
			currentToken++;
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("=")) {
				currentToken++;
				System.out.println("--- =");
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 601);
					
					if (!foundFirst) {
						System.out.println("Recovered: Missing expression in assignment");
						return;
					}
				}
				RULE_EXPRESSION();
			} else {
				error(602);
				// Try to recover by finding a semicolon or the next statement
				while (currentToken < tokens.size() && 
					!tokens.get(currentToken).getValue().equals(";") && 
					!isInFollowSetOf("ASSIGNMENT")) {
					currentToken++;
				}
				System.out.println("Recovered: Skipped to next statement after invalid assignment");
			}
		} else {
			error(603);
			// Try to recover by finding a semicolon or the next statement
			while (currentToken < tokens.size() && 
				!tokens.get(currentToken).getValue().equals(";") && 
				!isInFollowSetOf("ASSIGNMENT")) {
				currentToken++;
			}
			System.out.println("Recovered: Skipped to next statement after invalid assignment");
		}
	}

	private void RULE_CALL_METHOD() {
		System.out.println("--- RULE_CALL_METHOD");
		if (!isInFirstSetOf("CALL_METHOD")) {
			boolean foundFirst = skipUntilFirstOrFollow("CALL_METHOD", 700);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping CALL_METHOD rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			System.out.println("--- IDENTIFIER: " + tokens.get(currentToken).getValue());
			currentToken++;
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				System.out.println("--- (");
				RULE_PARAM_VALUES();
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					System.out.println("--- )");
				} else {
					error(701);
					// Try to recover by finding a semicolon or the next statement
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals(";") && 
						!isInFollowSetOf("CALL_METHOD")) {
						currentToken++;
					}
					System.out.println("Recovered: Skipped to next statement after method call with missing ')'");
				}
			} else {
				error(702);
				// Not a valid method call, might be intended as something else
				// Back up and let the caller handle it
				currentToken--;
				System.out.println("Recovered: Not a valid method call, backtracking");
			}
		} else {
			error(703);
		}
	}

	private void RULE_PARAM_VALUES() {
		System.out.println("---- RULE_PARAM_VALUES");
		if (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals(")")) {
			if (!isInFirstSetOf("EXPRESSION")) {
				boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 800);
				if (!foundFirst) {
					System.out.println("Recovered: Empty parameter list");
					return;
				}
			}
			RULE_EXPRESSION();
			while (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(",")) {
				currentToken++;
				System.out.println("---- ,");
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 801);
					if (!foundFirst) {
						System.out.println("Recovered: Missing expression after comma in parameter list");
						break;
					}
				}
				RULE_EXPRESSION();
			}
		}
	}

	private void RULE_RETURN() {
		System.out.println("--- RULE_RETURN");
		if (!isInFirstSetOf("RETURN")) {
			boolean foundFirst = skipUntilFirstOrFollow("RETURN", 800);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping RETURN rule");
				return;
			}
		}
		if (tokens.get(currentToken).getValue().equals("return")) {
			currentToken++;
			System.out.println("--- return");
			// Return can have an optional expression or just be "return;"
			if (!tokens.get(currentToken).getValue().equals(";")) {
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 801);
					if (!foundFirst) {
						// If we found a semicolon, that's fine - we'll treat it as "return;"
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
							currentToken++;
							System.out.println("--- ;");
							return;
						}
						// Skip until we find a semicolon or something in FOLLOW(RETURN)
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals(";") && 
							!isInFollowSetOf("RETURN")) {
							currentToken++;
						}
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
							currentToken++;
							System.out.println("--- ;");
						} else {
							System.out.println("Recovered: Missing expression and semicolon in return statement");
						}
						return;
					}
				}
				RULE_EXPRESSION();
			}
			if (tokens.get(currentToken).getValue().equals(";")) {
				currentToken++;
				System.out.println("--- ;");
			} else {
				error(19);
				// Skip until we find something in FOLLOW(RETURN)
				while (currentToken < tokens.size() && !isInFollowSetOf("RETURN")) {
					currentToken++;
				}
				System.out.println("Recovered: Missing semicolon after return statement");
			}
		} else {
			error(28);
			// Skip until we find something in FOLLOW(RETURN)
			while (currentToken < tokens.size() && !isInFollowSetOf("RETURN")) {
				currentToken++;
			}
			System.out.println("Recovered: Invalid return statement");
		}
	}

	private void RULE_WHILE() {
		System.out.println("--- RULE_WHILE");
		if (!isInFirstSetOf("WHILE")) {
			boolean foundFirst = skipUntilFirstOrFollow("WHILE", 900);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping WHILE rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("while")) {
			currentToken++;
			System.out.println("--- while");
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				System.out.println("--- (");
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 901);
					if (!foundFirst) {
						// Try to recover by finding the closing parenthesis
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals(")")) {
							currentToken++;
						}
						if (currentToken < tokens.size()) {
							currentToken++; // Skip the closing parenthesis
							System.out.println("Recovered: Missing condition in while loop");
						} else {
							System.out.println("Recovered: Skipping malformed while loop");
							return;
						}
					} else {
						RULE_EXPRESSION();
					}
				} else {
					RULE_EXPRESSION();
				}
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					System.out.println("--- )");
					if (!isInFirstSetOf("STATEMENT_BLOCK")) {
						boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 902);
						if (!foundFirst) {
							System.out.println("Recovered: Missing statement block in while loop");
							return;
						}
					}
					RULE_STATEMENT_BLOCK();
				} else {
					error(903);
					// Try to recover by looking for a statement block
					if (isInFirstSetOf("STATEMENT_BLOCK")) {
						System.out.println("Recovered: Missing ')' in while condition");
						RULE_STATEMENT_BLOCK();
					} else {
						// Skip to the next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("WHILE")) {
							currentToken++;
						}
						System.out.println("Recovered: Skipped malformed while loop");
					}
				}
			} else {
				error(904);
				// Try to recover by checking if there's an expression anyway
				if (isInFirstSetOf("EXPRESSION")) {
					System.out.println("Recovered: Missing '(' in while condition");
					RULE_EXPRESSION();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
						System.out.println("--- )");
						RULE_STATEMENT_BLOCK();
					} else if (isInFirstSetOf("STATEMENT_BLOCK")) {
						System.out.println("Recovered: Missing ')' in while condition");
						RULE_STATEMENT_BLOCK();
					} else {
						// Skip to the next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("WHILE")) {
							currentToken++;
						}
						System.out.println("Recovered: Skipped malformed while loop");
					}
				} else {
					// Skip to the next statement
					while (currentToken < tokens.size() && 
						!isInFollowSetOf("WHILE")) {
						currentToken++;
					}
					System.out.println("Recovered: Skipped malformed while loop");
				}
			}
		} else {
			error(905);
		}
	}

	private void RULE_IF() {
		System.out.println("--- RULE_IF");
		if (!isInFirstSetOf("IF")) {
			boolean foundFirst = skipUntilFirstOrFollow("IF", 400);
			
			if (!foundFirst) {
				System.out.println("Recovered: Skipping IF rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("if")) {
			currentToken++;
			System.out.println("--- if");
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				System.out.println("--- (");
				RULE_EXPRESSION();
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					System.out.println("--- )");
					RULE_STATEMENT_BLOCK();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("else")) {
						currentToken++;
						System.out.println("--- else");
						
						RULE_STATEMENT_BLOCK();
					}
				} else {
					// Error: Missing closing parenthesis
					boolean foundFollow = false;
					while (currentToken < tokens.size()) {
						if (isInFirstSetOf("STATEMENT_BLOCK")) {
							foundFollow = true;
							break;
						}
						currentToken++;
					}
					if (foundFollow) {
						System.out.println("Recovered: Found statement block after missing ')'");
						RULE_STATEMENT_BLOCK();
						
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("else")) {
							currentToken++;
							System.out.println("--- else");
							RULE_STATEMENT_BLOCK();
						}
					} else {
						System.out.println("Recovered: Skipping IF statement due to syntax errors");
					}
				}
			} else {
				// Error: Missing opening parenthesis
				boolean foundFollow = false;
				while (currentToken < tokens.size()) {
					if (isInFirstSetOf("EXPRESSION")) {
						foundFollow = true;
						break;
					}
					currentToken++;
				}
				if (foundFollow) {
					System.out.println("Recovered: Found expression after missing '('");
					RULE_EXPRESSION();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
						System.out.println("--- )");
						RULE_STATEMENT_BLOCK();
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("else")) {
							currentToken++;
							System.out.println("--- else");
							RULE_STATEMENT_BLOCK();
						}
					} else {
						// Continue recovery...
						if (isInFirstSetOf("STATEMENT_BLOCK")) {
							System.out.println("Recovered: Found statement block after expression");
							RULE_STATEMENT_BLOCK();
							if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("else")) {
								currentToken++;
								System.out.println("--- else");
								RULE_STATEMENT_BLOCK();
							}
						}
					}
				} else {
					System.out.println("Recovered: Skipping IF statement due to syntax errors");
				}
			}
		} else {
			error(401);
		}
	}

	private void RULE_DO_WHILE() {
		System.out.println("--- RULE_DO_WHILE");
		if (!isInFirstSetOf("DO_WHILE")) {
			boolean foundFirst = skipUntilFirstOrFollow("DO_WHILE", 900);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping DO_WHILE rule");
				return;
			}
		}
		if (tokens.get(currentToken).getValue().equals("do")) {
			currentToken++;
			System.out.println("--- do");
			if (!isInFirstSetOf("STATEMENT_BLOCK")) {
				boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 901);
				if (!foundFirst) {
					// Skip to "while" or something in FOLLOW(DO_WHILE)
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals("while") && 
						!isInFollowSetOf("DO_WHILE")) {
						currentToken++;
					}
					if (currentToken >= tokens.size() || !tokens.get(currentToken).getValue().equals("while")) {
						System.out.println("Recovered: Missing statement block and while part in do-while loop");
						return;
					}
					System.out.println("Recovered: Found while after missing statement block");
				} else {
					RULE_STATEMENT_BLOCK();
				}
			} else {
				RULE_STATEMENT_BLOCK();
			}
			if (tokens.get(currentToken).getValue().equals("while")) {
				currentToken++;
				System.out.println("--- while");
				if (tokens.get(currentToken).getValue().equals("(")) {
					currentToken++;
					System.out.println("--- (");
					if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 902);
						if (!foundFirst) {
							// Skip to ")" or something meaningful
							while (currentToken < tokens.size() && 
								!tokens.get(currentToken).getValue().equals(")") && 
								!tokens.get(currentToken).getValue().equals(";") && 
								!isInFollowSetOf("DO_WHILE")) {
								currentToken++;
							}
							if (currentToken >= tokens.size() || 
								(!tokens.get(currentToken).getValue().equals(")") && 
								!tokens.get(currentToken).getValue().equals(";"))) {
								System.out.println("Recovered: Missing condition in do-while loop");
								return;
							}
							System.out.println("Recovered: Found closing parenthesis or semicolon after missing condition");
						} else {
							RULE_EXPRESSION();
						}
					} else {
						RULE_EXPRESSION();
					}
					if (tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
						System.out.println("--- )");
						if (tokens.get(currentToken).getValue().equals(";")) {
							currentToken++;
							System.out.println("--- ;");
						} else {
							error(35);
							// Skip until we find something in FOLLOW(DO_WHILE)
							while (currentToken < tokens.size() && !isInFollowSetOf("DO_WHILE")) {
								currentToken++;
							}
							System.out.println("Recovered: Missing semicolon after do-while condition");
						}
					} else {
						error(36);
						// Try to recover by finding the semicolon
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals(";") && 
							!isInFollowSetOf("DO_WHILE")) {
							currentToken++;
						}
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
							currentToken++;
							System.out.println("Recovered: Found semicolon after missing closing parenthesis");
						} else {
							System.out.println("Recovered: Missing closing parenthesis and semicolon in do-while");
						}
					}
				} else {
					error(37);
					// Try to recover by skipping to next statement
					while (currentToken < tokens.size() && !isInFollowSetOf("DO_WHILE")) {
						currentToken++;
					}
					System.out.println("Recovered: Missing condition parentheses in do-while loop");
				}
			} else {
				error(38);
				// Try to recover by skipping to next statement
				while (currentToken < tokens.size() && !isInFollowSetOf("DO_WHILE")) {
					currentToken++;
				}
				System.out.println("Recovered: Missing while part in do-while loop");
			}
		} else {
			error(39);
			// Try to recover by skipping to next statement
			while (currentToken < tokens.size() && !isInFollowSetOf("DO_WHILE")) {
				currentToken++;
			}
			System.out.println("Recovered: Invalid do-while statement");
		}
	}

	private void RULE_FOR() {
		System.out.println("--- RULE_FOR");
		if (!isInFirstSetOf("FOR")) {
			boolean foundFirst = skipUntilFirstOrFollow("FOR", 1000);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping FOR rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("for")) {
			currentToken++;
			System.out.println("--- for");
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				System.out.println("--- (");
				// Initialization part
				if (isType()) {
					RULE_VARIABLE();
				} else if (!tokens.get(currentToken).getValue().equals(";")) {
					if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1001);
						if (foundFirst) {
							RULE_EXPRESSION();
						}
					} else {
						RULE_EXPRESSION();
					}
				}
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
					currentToken++;
					System.out.println("--- ;");
				} else {
					error(1002);
					// Try to skip to the next part of the for loop
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals(";") && 
						!tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
					}
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("Recovered: Found next semicolon in for loop");
					} else {
						System.out.println("Recovered: Missing semicolon in for loop initialization");
					}
				}
				// Condition part
				if (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals(";")) {
					if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1003);
						if (foundFirst) {
							RULE_EXPRESSION();
						}
					} else {
						RULE_EXPRESSION();
					}
				}
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
					currentToken++;
					System.out.println("--- ;");
				} else {
					error(1004);
					// Try to skip to the closing parenthesis
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
					}
					if (currentToken < tokens.size()) {
						System.out.println("Recovered: Missing semicolon in for loop condition");
					} else {
						System.out.println("Recovered: Malformed for loop");
						return;
					}
				}
				// Increment part
				if (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals(")")) {
					if (isAssignment()) {
						RULE_ASSIGNMENT();
					} else if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1005);
						if (foundFirst) {
							RULE_EXPRESSION();
						}
					} else {
						RULE_EXPRESSION();
					}
				}
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					System.out.println("--- )");
					if (!isInFirstSetOf("STATEMENT_BLOCK")) {
						boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 1006);
						if (!foundFirst) {
							System.out.println("Recovered: Missing statement block in for loop");
							return;
						}
					}
					RULE_STATEMENT_BLOCK();
				} else {
					error(1007);
					// Try to recover by looking for a statement block
					if (isInFirstSetOf("STATEMENT_BLOCK")) {
						System.out.println("Recovered: Missing ')' in for loop");
						RULE_STATEMENT_BLOCK();
					} else {
						// Skip to the next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("FOR")) {
							currentToken++;
						}
						System.out.println("Recovered: Skipped malformed for loop");
					}
				}
			} else {
				error(1008);
				// Try to find a statement block and assume the for loop was intended
				while (currentToken < tokens.size() && 
					!isInFirstSetOf("STATEMENT_BLOCK") && 
					!isInFollowSetOf("FOR")) {
					currentToken++;
				}
				if (isInFirstSetOf("STATEMENT_BLOCK")) {
					System.out.println("Recovered: Assuming empty for loop condition");
					RULE_STATEMENT_BLOCK();
				} else {
					System.out.println("Recovered: Skipped malformed for loop");
				}
			}
		} else {
			error(1009);
		}
	}

	private void RULE_SWITCH() {
		System.out.println("--- RULE_SWITCH");
		if (!isInFirstSetOf("SWITCH")) {
			boolean foundFirst = skipUntilFirstOrFollow("SWITCH", 1100);
			
			if (!foundFirst) {
				System.out.println("Recovered: Skipping SWITCH rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("switch")) {
			currentToken++;
			System.out.println("--- switch");
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				System.out.println("--- (");
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1101);
					if (!foundFirst) {
						// Try to recover by finding the closing parenthesis
						while (currentToken < tokens.size() && 
							!tokens.get(currentToken).getValue().equals(")")) {
							currentToken++;
						}
						if (currentToken < tokens.size()) {
							currentToken++; // Skip the closing parenthesis
							System.out.println("Recovered: Missing expression in switch");
						} else {
							System.out.println("Recovered: Malformed switch statement");
							return;
						}
					} else {
						RULE_EXPRESSION();
					}
				} else {
					RULE_EXPRESSION();
				}
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					System.out.println("--- )");
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
						currentToken++;
						System.out.println("--- {");
						while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
							if (tokens.get(currentToken).getValue().equals("case")) {
								currentToken++;
								System.out.println("---- case");
								if (!isInFirstSetOf("EXPRESSION")) {
									boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1102);
									if (!foundFirst) {
										// Try to recover by finding a colon
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals(":") && 
											!tokens.get(currentToken).getValue().equals("}")) {
											currentToken++;
										}
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
											currentToken++;
											System.out.println("Recovered: Missing expression in case label");
										} else {
											System.out.println("Recovered: Malformed case label");
											continue;
										}
									} else {
										RULE_EXPRESSION();
									}
								} else {
									RULE_EXPRESSION();
								}
								if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
									currentToken++;
									System.out.println("---- :");
									try {
										while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("break")) {
											RULE_BODY();
										}
									} catch (Exception e) {
										System.out.println("Error in case body: " + e.getMessage());
										// Try to recover by finding a break or the next case/default
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals("break") && 
											!tokens.get(currentToken).getValue().equals("case") && 
											!tokens.get(currentToken).getValue().equals("default") && 
											!tokens.get(currentToken).getValue().equals("}")) {
											currentToken++;
										}
									}
									if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("break")) {
										currentToken++;
										System.out.println("---- break");
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
											currentToken++;
											System.out.println("---- ;");
										} else {
											error(1103);
											System.out.println("Recovered: Missing semicolon after break");
										}
									} else {
										// Check if we've reached another case or default
										if (currentToken < tokens.size() && 
											(tokens.get(currentToken).getValue().equals("case") || 
											tokens.get(currentToken).getValue().equals("default") || 
											tokens.get(currentToken).getValue().equals("}"))) {
											System.out.println("Recovered: Missing break statement in case");
										} else {
											// Skip to the next case, default, or closing brace
											while (currentToken < tokens.size() && 
												!tokens.get(currentToken).getValue().equals("case") && 
												!tokens.get(currentToken).getValue().equals("default") && 
												!tokens.get(currentToken).getValue().equals("}")) {
												currentToken++;
											}
											System.out.println("Recovered: Malformed case block");
										}
									}
								} else {
									error(1104);
									// Skip to the next case, default, or closing brace
									while (currentToken < tokens.size() && 
										!tokens.get(currentToken).getValue().equals("case") && 
										!tokens.get(currentToken).getValue().equals("default") && 
										!tokens.get(currentToken).getValue().equals("}")) {
										currentToken++;
									}
									System.out.println("Recovered: Missing colon after case expression");
								}
							} else if (tokens.get(currentToken).getValue().equals("default")) {
								currentToken++;
								System.out.println("---- default");
								if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
									currentToken++;
									System.out.println("---- :");
									try {
										while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
											RULE_BODY();
										}
									} catch (Exception e) {
										System.out.println("Error in default body: " + e.getMessage());
										// Try to recover by finding the closing brace
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals("}")) {
											currentToken++;
										}
									}
								} else {
									error(1105);
									// Skip to the closing brace
									while (currentToken < tokens.size() && 
										!tokens.get(currentToken).getValue().equals("}")) {
										currentToken++;
									}
									System.out.println("Recovered: Missing colon after default");
								}
							} else {
								error(1106);
								// Skip to the next case, default, or closing brace
								while (currentToken < tokens.size() && 
									!tokens.get(currentToken).getValue().equals("case") && 
									!tokens.get(currentToken).getValue().equals("default") && 
									!tokens.get(currentToken).getValue().equals("}")) {
									currentToken++;
								}
								System.out.println("Recovered: Expected case or default in switch");
							}
						}
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("}")) {
							currentToken++;
							System.out.println("--- }");
						} else {
							error(1107);
							System.out.println("Recovered: Missing closing brace in switch statement");
						}
					} else {
						error(1108);
						// Try to recover by skipping to the next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("SWITCH")) {
							currentToken++;
						}
						System.out.println("Recovered: Missing opening brace in switch statement");
					}
				} else {
					error(1109);
					// Try to recover by searching for opening brace
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals("{") && 
						!isInFollowSetOf("SWITCH")) {
						currentToken++;
					}
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
						System.out.println("Recovered: Missing closing parenthesis in switch");
						currentToken++;
						System.out.println("--- {");
						// Complete switch body processing
						while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
							if (tokens.get(currentToken).getValue().equals("case")) {
								currentToken++;
								System.out.println("---- case");
								if (!isInFirstSetOf("EXPRESSION")) {
									boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1112);
									if (!foundFirst) {
										// Intentar recuperarse buscando un colon
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals(":") && 
											!tokens.get(currentToken).getValue().equals("}")) {
											currentToken++;
										}
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
											currentToken++;
											System.out.println("Recovered: Missing expression in case label");
										} else {
											System.out.println("Recovered: Malformed case label");
											continue;
										}
									} else {
										RULE_EXPRESSION();
									}
								} else {
									RULE_EXPRESSION();
								}
								if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
									currentToken++;
									System.out.println("---- :");
									try {
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals("break") && 
											!tokens.get(currentToken).getValue().equals("case") && 
											!tokens.get(currentToken).getValue().equals("default") && 
											!tokens.get(currentToken).getValue().equals("}")) {
											RULE_BODY();
										}
									} catch (Exception e) {
										System.out.println("Error in case body: " + e.getMessage());
										// Recover by finding break, case, default or }
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals("break") && 
											!tokens.get(currentToken).getValue().equals("case") && 
											!tokens.get(currentToken).getValue().equals("default") && 
											!tokens.get(currentToken).getValue().equals("}")) {
											currentToken++;
										}
									}
									if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("break")) {
										currentToken++;
										System.out.println("---- break");
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
											currentToken++;
											System.out.println("---- ;");
										} else {
											error(1113);
											System.out.println("Recovered: Missing semicolon after break");
										}
									} else if (currentToken < tokens.size() && 
											(tokens.get(currentToken).getValue().equals("case") || 
											tokens.get(currentToken).getValue().equals("default") || 
											tokens.get(currentToken).getValue().equals("}"))) {
										System.out.println("Recovered: Missing break statement in case");
									}
								} else {
									error(1114);
									// Skip to next case, default or }
									while (currentToken < tokens.size() && 
										!tokens.get(currentToken).getValue().equals("case") && 
										!tokens.get(currentToken).getValue().equals("default") && 
										!tokens.get(currentToken).getValue().equals("}")) {
										currentToken++;
									}
									System.out.println("Recovered: Missing colon after case expression");
								}
							} else if (tokens.get(currentToken).getValue().equals("default")) {
								currentToken++;
								System.out.println("---- default");
								if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
									currentToken++;
									System.out.println("---- :");
									try {
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals("break") && 
											!tokens.get(currentToken).getValue().equals("case") && 
											!tokens.get(currentToken).getValue().equals("}")) {
											RULE_BODY();
										}
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("break")) {
											currentToken++;
											System.out.println("---- break");
											if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
												currentToken++;
												System.out.println("---- ;");
											} else {
												error(1115);
												System.out.println("Recovered: Missing semicolon after break in default case");
											}
										}
									} catch (Exception e) {
										System.out.println("Error in default body: " + e.getMessage());
										// Recover by finding the closing bracket
										while (currentToken < tokens.size() && 
											!tokens.get(currentToken).getValue().equals("}")) {
											currentToken++;
										}
									}
								} else {
									error(1116);
									// Skip to next closing bracket
									while (currentToken < tokens.size() && 
										!tokens.get(currentToken).getValue().equals("}")) {
										currentToken++;
									}
									System.out.println("Recovered: Missing colon after default");
								}
							} else {
								error(1117);
								// Skip to next case, default or }
								while (currentToken < tokens.size() && 
									!tokens.get(currentToken).getValue().equals("case") && 
									!tokens.get(currentToken).getValue().equals("default") && 
									!tokens.get(currentToken).getValue().equals("}")) {
									currentToken++;
								}
								System.out.println("Recovered: Expected case or default in switch");
							}
						}
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("}")) {
							currentToken++;
							System.out.println("--- }");
						} else {
							error(1118);
							System.out.println("Recovered: Missing closing brace in switch statement");
						}
					} else {
						System.out.println("Recovered: Skipped malformed switch statement");
					}
				}
			} else {
				error(1110);
				// Try to recover by skipping to the next statement
				while (currentToken < tokens.size() && 
					!isInFollowSetOf("SWITCH")) {
					currentToken++;
				}
				System.out.println("Recovered: Missing opening parenthesis in switch statement");
			}
		} else {
			error(1111);
		}
	}

	private void RULE_STATEMENT_BLOCK() {
		System.out.println("---- RULE_STATEMENT_BLOCK");
		if (!isInFirstSetOf("STATEMENT_BLOCK")) {
			boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 1200);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping STATEMENT_BLOCK rule");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
			currentToken++;
			System.out.println("---- {");
			try {
				RULE_BODY();
			} catch (Exception e) {
				System.out.println("Error in statement block body: " + e.getMessage());
				// Try to recover by finding the closing brace
				while (currentToken < tokens.size() && 
					!tokens.get(currentToken).getValue().equals("}")) {
					currentToken++;
				}
			}
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("}")) {
				currentToken++;
				System.out.println("---- }");
			} else {
				error(1201);
				System.out.println("Recovered: Missing closing brace in statement block");
			}
		} else {
			// Single statement
			try {
				if (isType()) {
					RULE_VARIABLE();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("---- ;");
					} else {
						error(1202);
						// Skip to next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("STATEMENT_BLOCK")) {
							currentToken++;
						}
						System.out.println("Recovered: Missing semicolon after variable declaration");
					}
				} else if (isAssignment()) {
					RULE_ASSIGNMENT();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("---- ;");
					} else {
						error(1203);
						// Skip to next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("STATEMENT_BLOCK")) {
							currentToken++;
						}
						System.out.println("Recovered: Missing semicolon after assignment");
					}
				} else if (isMethodCall()) {
					RULE_CALL_METHOD();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("---- ;");
					} else {
						error(1204);
						// Skip to next statement
						while (currentToken < tokens.size() && 
							!isInFollowSetOf("STATEMENT_BLOCK")) {
							currentToken++;
						}
						System.out.println("Recovered: Missing semicolon after method call");
					}
				} else if (isReturnStatement()) {
					RULE_RETURN();
				} else if (isWhileStatement()) {
					RULE_WHILE();
				} else if (isIfStatement()) {
					RULE_IF();
				} else if (isDoStatement()) {
					RULE_DO_WHILE();
				} else if (isForStatement()) {
					RULE_FOR();
				} else if (isSwitchStatement()) {
					RULE_SWITCH();
				} else {
					if (!isInFirstSetOf("EXPRESSION")) {
						boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1205);
						
						if (!foundFirst) {
							System.out.println("Recovered: Unable to parse statement");
							return;
						}
					}
					RULE_EXPRESSION();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						System.out.println("---- ;");
					} else {
						error(1205);
                    // Skip to next statement
                    while (currentToken < tokens.size() && 
                            !isInFollowSetOf("STATEMENT_BLOCK")) {
                        currentToken++;
                    }
                    System.out.println("Recovered: Missing semicolon after expression");
                }
            }
        } catch (Exception e) {
            System.out.println("Error in single statement: " + e.getMessage());
            // Skip to next statement
            while (currentToken < tokens.size() && 
                    !isInFollowSetOf("STATEMENT_BLOCK")) {
                currentToken++;
            }
            System.out.println("Recovered: Skip to next statement after error");
        }
    }
}

	private void RULE_EXPRESSION() {
		System.out.println("--- RULE_EXPRESSION");
		if (!isInFirstSetOf("X")) {
			boolean foundFirst = skipUntilFirstOrFollow("X", 1400);
			
			if (!foundFirst) {
				System.out.println("Recovered: Skipping EXPRESSION rule");
				return;
			}
		}
		RULE_X();
		while (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("||")) {
			currentToken++;
			System.out.println("--- ||");
			if (!isInFirstSetOf("X")) {
				boolean foundFirst = skipUntilFirstOrFollow("X", 1401);
				
				if (!foundFirst) {
					System.out.println("Recovered: Missing operand after ||");
					break;
				}
			}
			RULE_X();
		}
	}

	private void RULE_X() {
		System.out.println("---- RULE_X");
		if (!isInFirstSetOf("Y")) {
			boolean foundFirst = skipUntilFirstOrFollow("Y", 1410);
			
			if (!foundFirst) {
				System.out.println("Recovered: Skipping X rule");
				return;
			}
		}
		RULE_Y();
		while (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("&&")) {
			currentToken++;
			System.out.println("---- &&");
			if (!isInFirstSetOf("Y")) {
				boolean foundFirst = skipUntilFirstOrFollow("Y", 1411);
				if (!foundFirst) {
					System.out.println("Recovered: Missing operand after &&");
					break;
				}
			}
			RULE_Y();
		}
	}

	private void RULE_Y() {
		System.out.println("----- RULE_Y");
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("!")) {
			currentToken++;
			System.out.println("----- !");
			if (!isInFirstSetOf("Y")) {
				boolean foundFirst = skipUntilFirstOrFollow("Y", 1420);
				if (!foundFirst) {
					System.out.println("Recovered: Missing operand after !");
					return;
				}
			}
			RULE_Y();
		} else {
			if (!isInFirstSetOf("R")) {
				boolean foundFirst = skipUntilFirstOrFollow("R", 1421);
				
				if (!foundFirst) {
					System.out.println("Recovered: Skipping Y rule");
					return;
				}
			}
			RULE_R();
		}
	}

	private void RULE_R() {
		System.out.println("------ RULE_R");
		if (!isInFirstSetOf("E")) {
			boolean foundFirst = skipUntilFirstOrFollow("E", 1430);
			
			if (!foundFirst) {
				System.out.println("Recovered: Skipping R rule");
				return;
			}
		}
		RULE_E();
		String currentVal = currentToken < tokens.size() ? tokens.get(currentToken).getValue() : "";
		if (currentToken < tokens.size() && 
			(currentVal.equals("<") || currentVal.equals(">") || 
			currentVal.equals("==") || currentVal.equals("!="))) {
			System.out.println("------ " + currentVal);
			currentToken++;
			if (!isInFirstSetOf("E")) {
				boolean foundFirst = skipUntilFirstOrFollow("E", 1431);
				if (!foundFirst) {
					System.out.println("Recovered: Missing right operand in comparison");
					return;
				}
			}
			RULE_E();
		}
	}

	private void RULE_E() {
		System.out.println("------- RULE_E");
		if (!isInFirstSetOf("A")) {
			boolean foundFirst = skipUntilFirstOrFollow("A", 1440);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping E rule");
				return;
			}
		}
		RULE_A();
		while (currentToken < tokens.size() && 
			(tokens.get(currentToken).getValue().equals("+") || 
				tokens.get(currentToken).getValue().equals("-"))) {
			String operator = tokens.get(currentToken).getValue();
			System.out.println("------- " + operator);
			currentToken++;
			if (!isInFirstSetOf("A")) {
				boolean foundFirst = skipUntilFirstOrFollow("A", 1441);
				if (!foundFirst) {
					System.out.println("Recovered: Missing operand after " + operator);
					break;
				}
			}
			RULE_A();
		}
	}

	private void RULE_A() {
		System.out.println("-------- RULE_A");
		if (!isInFirstSetOf("B")) {
			boolean foundFirst = skipUntilFirstOrFollow("B", 1450);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping A rule");
				return;
			}
		}
		RULE_B();
		while (currentToken < tokens.size() && 
			(tokens.get(currentToken).getValue().equals("*") || 
				tokens.get(currentToken).getValue().equals("/"))) {
			String operator = tokens.get(currentToken).getValue();
			System.out.println("-------- " + operator);
			currentToken++;
			if (!isInFirstSetOf("B")) {
				boolean foundFirst = skipUntilFirstOrFollow("B", 1451);
				if (!foundFirst) {
					System.out.println("Recovered: Missing operand after " + operator);
					break;
				}
			}
			RULE_B();
		}
	}

	private void RULE_B() {
		System.out.println("--------- RULE_B");
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("-")) {
			currentToken++;
			System.out.println("--------- -");
		}
		if (!isInFirstSetOf("C")) {
			boolean foundFirst = skipUntilFirstOrFollow("C", 1460);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping B rule");
				return;
			}
		}
		RULE_C();
	}

	private void RULE_C() {
		System.out.println("---------- RULE_C");
		if (!isInFirstSetOf("C")) {
			boolean foundFirst = skipUntilFirstOrFollow("C", 1470);
			if (!foundFirst) {
				System.out.println("Recovered: Missing operand in expression");
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			if (currentToken + 1 < tokens.size() && tokens.get(currentToken + 1).getValue().equals("(")) {
				RULE_CALL_METHOD();
			} else {
				System.out.println("---------- IDENTIFIER: " + tokens.get(currentToken).getValue());
				currentToken++;
			}
		} else if (currentToken < tokens.size() && 
				(tokens.get(currentToken).getType().equals("INTEGER") ||
					tokens.get(currentToken).getType().equals("FLOAT") ||
					tokens.get(currentToken).getType().equals("CHAR") ||
					tokens.get(currentToken).getType().equals("STRING") ||
					tokens.get(currentToken).getType().equals("HEXADECIMAL") ||
					tokens.get(currentToken).getType().equals("BINARY") ||
					(tokens.get(currentToken).getType().equals("KEYWORD") &&
					(tokens.get(currentToken).getValue().equals("true") ||
					tokens.get(currentToken).getValue().equals("false"))))) {
			System.out.println("---------- LITERAL: " + tokens.get(currentToken).getValue());
			currentToken++;
		} else if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
			currentToken++;
			System.out.println("---------- (");
			if (!isInFirstSetOf("EXPRESSION")) {
				boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1471);
				if (!foundFirst) {
					// Try to find the closing parenthesis
					while (currentToken < tokens.size() && 
						!tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
					}
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
						System.out.println("---------- ) (recovered - empty parentheses)");
					} else {
						System.out.println("Recovered: Missing closing parenthesis");
					}
					return;
				}
			}
			RULE_EXPRESSION();
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
				currentToken++;
				System.out.println("---------- )");
			} else {
				error(1472);
				System.out.println("Recovered: Missing closing parenthesis");
			}
		} else {
			error(1473);
			System.out.println("Recovered: Invalid expression element");
		}
	}

	private void RULE_TYPE() {
		System.out.println("----- RULE_TYPE");
		if (!isInFirstSetOf("TYPE")) {
			boolean foundFirst = skipUntilFirstOrFollow("TYPE", 300);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping TYPE rule");
				return;
			}
		}
		if (currentToken < tokens.size() && 
			tokens.get(currentToken).getType().equals("KEYWORD") &&
			(tokens.get(currentToken).getValue().equals("int") ||
			tokens.get(currentToken).getValue().equals("float") ||
			tokens.get(currentToken).getValue().equals("void") ||
			tokens.get(currentToken).getValue().equals("char") ||
			tokens.get(currentToken).getValue().equals("string") ||
			tokens.get(currentToken).getValue().equals("boolean"))) {
			System.out.println("----- TYPE: " + tokens.get(currentToken).getValue());
			currentToken++;
		} else {
			error(301);
		}
	}

	private boolean isType() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				(tokens.get(currentToken).getValue().equals("int") ||
						tokens.get(currentToken).getValue().equals("float") ||
						tokens.get(currentToken).getValue().equals("void") ||
						tokens.get(currentToken).getValue().equals("char") ||
						tokens.get(currentToken).getValue().equals("string") ||
						tokens.get(currentToken).getValue().equals("boolean"));
	}

	private boolean isMethodDeclaration() {
		int savePos = currentToken;
		try {
			if (isType()) {
				currentToken++;
				if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
					currentToken++;
					return tokens.get(currentToken).getValue().equals("(");
				}
			}
			return false;
		} finally {
			currentToken = savePos;
		}
	}

	private boolean isAssignment() {
		if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			if (currentToken + 1 < tokens.size() &&
					tokens.get(currentToken + 1).getValue().equals("=")) {
				return true;
			}
		}
		return false;
	}

	private boolean isMethodCall() {
		if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			if (currentToken + 1 < tokens.size() &&
					tokens.get(currentToken + 1).getValue().equals("(")) {
				return true;
			}
		}
		return false;
	}

	private boolean isReturnStatement() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("return");
	}

	private boolean isWhileStatement() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("while");
	}

	private boolean isIfStatement() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("if");
	}

	private boolean isDoStatement() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("do");
	}

	private boolean isForStatement() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("for");
	}

	private boolean isSwitchStatement() {
		return tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("switch");
	}

}
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

	private TheSemantic semantic;
	private TheCodeGenerator codeGenerator;
	private String currentMethodReturnType = null;

	private Vector<String> parseTreeLog = new Vector<>();
	private int indentLevel = 0;

	public TheParser(Vector<TheToken> tokens) {
		this.tokens = tokens;
		currentToken = 0;
		this.semantic = new TheSemantic();
		this.codeGenerator = new TheCodeGenerator();
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
		Set<String> programFirst = new HashSet<>(Arrays.asList("{", "class", "int", "float", "void", "char", "string", "boolean", "IDENTIFIER"));
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
		bodyFirst.add("println");
		bodyFirst.add("inputln");
		bodyFirst.add(""); // epsilon
		firstSets.put("BODY", bodyFirst);
		// PRINTLN
		firstSets.put("PRINTLN", new HashSet<>(Collections.singletonList("println")));
		// INPUTLN
		firstSets.put("INPUTLN", new HashSet<>(Collections.singletonList("inputln")));
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
		// PRINTLN
		followSets.put("PRINTLN", new HashSet<>(Arrays.asList(";", "}", "break", "case", "default")));
		// INPUTLN
		followSets.put("INPUTLN", new HashSet<>(Arrays.asList(";", "}", "break", "case", "default")));
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
	 *
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
	 *
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
	 *
	 * @param rule      The rule name to check against
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
	 *
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
		logParseRule("RULE_PROGRAM");
		indentLevel++;

		if (!isInFirstSetOf("PROGRAM")) {
			// Si no es class o {, intentar parsear como declaraciones simples
			if (isType() || isAssignment() || isMethodCall()) {
				System.out.println("-- Parsing simple statements");
				RULE_BODY(); // Parsear como un cuerpo de declaraciones simples
				System.out.println("Parsing completed with recovery.");
				return;
			}

			boolean foundFirst = skipUntilFirstOrFollow("PROGRAM", 200);
			if (!foundFirst) {
				System.out.println("Recovered: Cannot find valid program start token");
				indentLevel--;
				return;
			}
		}

		if (tokens.get(currentToken).getValue().equals("{")) {
			currentToken++;
			logParseRule("{");
			RULE_BODY();
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("}")) {
				currentToken++;
				logParseRule("}");
			} else {
				// Error: Missing closing brace
				while (currentToken < tokens.size() && !isInFollowSetOf("PROGRAM")) {
					currentToken++;
				}
				System.out.println("Recovered: Skipped to end of program after missing '}'");
			}
		} else if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("class")) {
			currentToken++;
			logParseRule("class");
			if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
				logParseRule("IDENTIFIER: " + tokens.get(currentToken).getValue());
				currentToken++;
			} else {
				// Error: Missing class name identifier
				boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 201);
				if (!foundFirst) {
					System.out.println("Recovered: Skipping class body due to missing class name");
					indentLevel--;
					return;
				}
			}
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
				currentToken++;
				logParseRule("{");
				while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
					if (isType()) {
						if (isMethodDeclaration()) {
							RULE_METHODS();
						} else {
							RULE_VARIABLE();
							if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								logParseRule(";");
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
					logParseRule("}");
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
		indentLevel--;
	}

	private void RULE_METHODS() {
		logParseRule("RULE_METHODS");
		indentLevel++;
		if (!isInFirstSetOf("METHODS")) {
			boolean foundFirst = skipUntilFirstOrFollow("METHODS", 700);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping METHODS rule");
				indentLevel--;
				return;
			}
		}
		RULE_TYPE();
		if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			logParseRule("IDENTIFIER: " + tokens.get(currentToken).getValue());
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
			logParseRule("(");
			RULE_PARAMS();
			if (tokens.get(currentToken).getValue().equals(")")) {
				currentToken++;
				logParseRule(")");
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
				logParseRule("{");
				RULE_BODY();
				if (tokens.get(currentToken).getValue().equals("}")) {
					currentToken++;
					logParseRule("}");
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
		indentLevel--;
	}

	private void RULE_PARAMS() {
		logParseRule("RULE_PARAMS");
		indentLevel++;
		// Params can be empty (epsilon), so we check if the current token is ")"
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
			// Empty parameter list is valid, do nothing
			return;
		}
		if (!isInFirstSetOf("PARAMS")) {
			boolean foundFirst = skipUntilFirstOrFollow("PARAMS", 600);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping PARAMS rule");
				indentLevel--;
				return;
			}
		}
		if (isType()) {
			RULE_TYPE();
			if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
				logParseRule("IDENTIFIER: " + tokens.get(currentToken).getValue());
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
				logParseRule(",");
				if (isType()) {
					RULE_TYPE();
					if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
						logParseRule("IDENTIFIER: " + tokens.get(currentToken).getValue());
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
		indentLevel--;
	}

	private void RULE_BODY() {
		logParseRule("-- RULE_BODY");
		indentLevel++;
		while (currentToken < tokens.size() &&
				!(tokens.get(currentToken).getValue().equals("}") ||
						tokens.get(currentToken).getValue().equals("break"))) {
			try {
				if (isType()) {
					RULE_VARIABLE();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						logParseRule("-- ;");
					} else {
						error(1300);
						// Skip until we find a semicolon or the start of another valid statement
						boolean recovered = false;
						while (currentToken < tokens.size() &&
								!tokens.get(currentToken).getValue().equals("}") &&
								!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								logParseRule("-- ; (recovered)");
								recovered = true;
								break;
							}
							// Check if we've reached the start of another valid statement
							if (isType() || isAssignment() || isMethodCall() || isReturnStatement() ||
									isWhileStatement() || isIfStatement() || isDoStatement() ||
									isForStatement() || isSwitchStatement() || isPrintlnStatement()) {
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
						logParseRule(";");
					} else {
						error(1301);
						// Skip until we find a semicolon or the start of another valid statement
						boolean recovered = false;
						while (currentToken < tokens.size() &&
								!tokens.get(currentToken).getValue().equals("}") &&
								!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								logParseRule("; (recovered)");
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
						logParseRule(";");
					} else {
						error(1302);
						// Similar recovery as above
						boolean recovered = false;
						while (currentToken < tokens.size() &&
								!tokens.get(currentToken).getValue().equals("}") &&
								!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								logParseRule("; (recovered)");
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
				}else if (isPrintlnStatement()) { // NUEVO: Agregar verificación para println
					RULE_PRINTLN();
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
						currentToken++;
						logParseRule("-- ;");
					} else {
						error(1305);
					}
				}
				else if (isReturnStatement()) {
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
									!isPrintlnStatement() &&
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
						logParseRule("-- ;");
					} else {
						error(1304);
						// Similar recovery as above
						boolean recovered = false;
						while (currentToken < tokens.size() &&
								!tokens.get(currentToken).getValue().equals("}") &&
								!tokens.get(currentToken).getValue().equals("break")) {
							if (tokens.get(currentToken).getValue().equals(";")) {
								currentToken++;
								logParseRule("; (recovered)");
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
		indentLevel--;
	}

	private void RULE_VARIABLE() {
		logParseRule("RULE_VARIABLE");
		indentLevel++;

		if (!isInFirstSetOf("VARIABLE")) {
			boolean foundFirst = skipUntilFirstOrFollow("VARIABLE", 500);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping VARIABLE rule");
				indentLevel--;
				return;
			}
		}

		String variableType = null;
		String variableName = null;

		// Capturar el tipo
		if (isType()) {
			variableType = tokens.get(currentToken).getValue();
			RULE_TYPE();
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			variableName = tokens.get(currentToken).getValue();
			logParseRule("IDENTIFIER: " + variableName);

			// ANÁLISIS SEMÁNTICO: Declarar variable
			if (variableType != null && variableName != null) {
				semantic.checkVariableDeclaration(variableType, variableName, tokens.get(currentToken).getLineNumber());
			}

			currentToken++;

			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("=")) {
				currentToken++;
				logParseRule("=");

				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 501);
					if (!foundFirst) {
						System.out.println("Recovered: Missing expression in variable initialization");
						indentLevel--;
						return;
					}
				}

				// Capturar el valor antes de evaluar la expresión
				String assignedValue = null;
				if (currentToken < tokens.size()) {
					assignedValue = tokens.get(currentToken).getValue();
					logParseRule("LITERAL: " + assignedValue);
				}

				// Evaluar la expresión y verificar compatibilidad de tipos
				String expressionType = evaluateExpression();
				if (variableType != null && expressionType != null) {
					semantic.checkAssignment(variableType, expressionType, tokens.get(currentToken).getLineNumber());

					// Asignar el valor real a la variable
					if (assignedValue != null && variableName != null) {
						semantic.setVariableValue(variableName, assignedValue, tokens.get(currentToken).getLineNumber());

						// GENERACIÓN DE CÓDIGO: Variable con inicialización
						codeGenerator.generateVariableDeclaration(variableName, assignedValue);
					}
				}
			}
			// Si no hay inicialización, la variable se declara con valor por defecto (ya está en la tabla de símbolos)
		} else {
			error(502);
			while (currentToken < tokens.size() &&
					!tokens.get(currentToken).getValue().equals(";") &&
					!isInFollowSetOf("VARIABLE")) {
				currentToken++;
			}
			System.out.println("Recovered: Skipped to next statement after invalid variable declaration");
		}
		indentLevel--;
	}

	private void RULE_ASSIGNMENT() {
		logParseRule("--- RULE_ASSIGNMENT");
		indentLevel++;

		if (!isInFirstSetOf("ASSIGNMENT")) {
			boolean foundFirst = skipUntilFirstOrFollow("ASSIGNMENT", 600);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping ASSIGNMENT rule");
				indentLevel--;
				return;
			}
		}

		String variableName = null;
		String variableType = null;

		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			variableName = tokens.get(currentToken).getValue();
			logParseRule("--- IDENTIFIER: " + variableName);

			// ANÁLISIS SEMÁNTICO: Verificar que la variable existe
			variableType = semantic.checkVariableUsage(variableName, tokens.get(currentToken).getLineNumber());

			currentToken++;

			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("=")) {
				currentToken++;
				logParseRule("--- =");

				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 601);
					if (!foundFirst) {
						System.out.println("Recovered: Missing expression in assignment");
						indentLevel--;
						return;
					}
				}

				// GENERACIÓN DE CÓDIGO: Generar código para la expresión
				String assignmentValue = generateExpressionCode();

				// Evaluar la expresión y verificar compatibilidad de tipos
				String expressionType = evaluateExpression();
				if (variableType != null && expressionType != null) {
					semantic.checkAssignment(variableType, expressionType, tokens.get(currentToken).getLineNumber());

					// GENERACIÓN DE CÓDIGO: Asignación
					if (assignmentValue != null && variableName != null) {
						// Para valores simples
						codeGenerator.generateAssignment(variableName, assignmentValue);
					} else {
						// Para expresiones complejas, el código ya fue generado en generateExpressionCode
						// Solo necesitamos guardar el resultado
						codeGenerator.generateAssignmentFromStack(variableName);
					}
				}
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
		indentLevel--;
	}

	private void RULE_CALL_METHOD() {
		logParseRule("--- RULE_CALL_METHOD");
		indentLevel++;
		if (!isInFirstSetOf("CALL_METHOD")) {
			boolean foundFirst = skipUntilFirstOrFollow("CALL_METHOD", 700);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping CALL_METHOD rule");
				indentLevel--;
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			logParseRule("IDENTIFIER: " + tokens.get(currentToken).getValue());
			currentToken++;
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				logParseRule("(");
				RULE_PARAM_VALUES();
				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					logParseRule(")");
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
		indentLevel--;
	}

	private void RULE_PARAM_VALUES() {
		logParseRule("RULE_PARAM_VALUES");
		indentLevel++;
		if (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals(")")) {
			if (!isInFirstSetOf("EXPRESSION")) {
				boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 800);
				if (!foundFirst) {
					System.out.println("Recovered: Empty parameter list");
					indentLevel--;
					return;
				}
			}
			RULE_EXPRESSION();
			while (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(",")) {
				currentToken++;
				logParseRule(",");
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
		indentLevel--;
	}

	private void RULE_RETURN() {
		logParseRule("RULE_RETURN");
		indentLevel++;
		if (!isInFirstSetOf("RETURN")) {
			boolean foundFirst = skipUntilFirstOrFollow("RETURN", 800);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping RETURN rule");
				indentLevel--;
				return;
			}
		}
		if (tokens.get(currentToken).getValue().equals("return")) {
			currentToken++;
			logParseRule("return");
			// Return can have an optional expression or just be "return;"
			if (!tokens.get(currentToken).getValue().equals(";")) {
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 801);
					if (!foundFirst) {
						// If we found a semicolon, that's fine - we'll treat it as "return;"
						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
							currentToken++;
							logParseRule(";");
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
							logParseRule(";");
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
				logParseRule(";");
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
		indentLevel--;
	}

	private void RULE_WHILE() {
		logParseRule("--- RULE_WHILE");
		indentLevel++;

		if (!isInFirstSetOf("WHILE")) {
			boolean foundFirst = skipUntilFirstOrFollow("WHILE", 900);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping WHILE rule");
				indentLevel--;
				return;
			}
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("while")) {
			currentToken++;
			logParseRule("--- while");

			// GENERACIÓN DE CÓDIGO: Etiqueta del inicio del while (ANTES de evaluar condición)
			String whileStartLabel = codeGenerator.generateLabel();
			codeGenerator.addLabel(whileStartLabel);

			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				logParseRule("--- (");

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
							indentLevel--;
							return;
						}
					} else {
						// GENERACIÓN DE CÓDIGO: Evaluar condición del while
						generateConditionCode();

						// Evaluar la expresión de condición CORRECTAMENTE
						String conditionType = evaluateExpression();

						// ANÁLISIS SEMÁNTICO: Verificar que la condición sea booleana
						if (conditionType != null) {
							semantic.checkBooleanExpression(conditionType, "while", tokens.get(currentToken).getLineNumber());
						}
					}
				} else {
					// GENERACIÓN DE CÓDIGO: Evaluar condición del while
					generateConditionCode();

					// Evaluar la expresión de condición CORRECTAMENTE
					String conditionType = evaluateExpression();

					// ANÁLISIS SEMÁNTICO: Verificar que la condición sea booleana
					if (conditionType != null) {
						semantic.checkBooleanExpression(conditionType, "while", tokens.get(currentToken).getLineNumber());
					}
				}

				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					logParseRule("--- )");

					// GENERACIÓN DE CÓDIGO: Salto condicional al final del while
					String whileEndLabel = codeGenerator.generateLabel();
					codeGenerator.generateWhileConditionalJump(whileEndLabel);

					// Procesar el cuerpo del while
					RULE_STATEMENT_BLOCK();

					// GENERACIÓN DE CÓDIGO: Salto incondicional al inicio del while
					codeGenerator.generateJump(whileStartLabel);

					// GENERACIÓN DE CÓDIGO: Etiqueta del final del while
					codeGenerator.addLabel(whileEndLabel);

				} else {
					error(29);
					// Try to recover by looking for a statement block
					if (isInFirstSetOf("STATEMENT_BLOCK")) {
						System.out.println("Recovered: Missing ')' in while condition");

						// Generar etiquetas de recuperación
						String whileEndLabel = codeGenerator.generateLabel();
						codeGenerator.generateConditionalJump(whileEndLabel, "false");

						RULE_STATEMENT_BLOCK();

						codeGenerator.generateJump(whileStartLabel);
						codeGenerator.addLabel(whileEndLabel);
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
				error(30);
				// Try to recover by checking if there's an expression anyway
				if (isInFirstSetOf("EXPRESSION")) {
					System.out.println("Recovered: Missing '(' in while condition");

					// Generar código de recuperación
					generateConditionCode();
					evaluateExpression();

					String whileEndLabel = codeGenerator.generateLabel();
					codeGenerator.generateConditionalJump(whileEndLabel, "false");

					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
						currentToken++;
						logParseRule("--- )");
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
						indentLevel--;
						return;
					}

					codeGenerator.generateJump(whileStartLabel);
					codeGenerator.addLabel(whileEndLabel);
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
			error(31);
		}

		indentLevel--;
	}

	private void RULE_IF() {
		logParseRule("--- RULE_IF");
		indentLevel++;

		if (!isInFirstSetOf("IF")) {
			boolean foundFirst = skipUntilFirstOrFollow("IF", 400);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping IF rule");
				indentLevel--;
				return;
			}
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("if")) {
			currentToken++;
			logParseRule("--- if");

			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				logParseRule("--- (");

				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 401);
					if (!foundFirst) {
						System.out.println("Recovered: Missing expression in if condition");
						indentLevel--;
						return;
					}
				} else {
					// GENERACIÓN DE CÓDIGO: Evaluar condición del if
					generateConditionCode();

					// Evaluar la expresión de condición
					String conditionType = evaluateExpression();

					// ANÁLISIS SEMÁNTICO: Verificar que la condición sea booleana
					semantic.checkBooleanExpression(conditionType, "if", tokens.get(currentToken).getLineNumber());
				}

				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					logParseRule("--- )");

					// GENERACIÓN DE CÓDIGO: Salto condicional
					String elseLabel = codeGenerator.generateLabel();
					codeGenerator.generateConditionalJump(elseLabel, "false");

					RULE_STATEMENT_BLOCK();

					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("else")) {
						currentToken++;
						logParseRule("--- else");

						// GENERACIÓN DE CÓDIGO: Salto incondicional para saltar el else
						String endLabel = codeGenerator.generateLabel();
						codeGenerator.generateJump(endLabel);

						// Colocar la etiqueta del else
						codeGenerator.addLabel(elseLabel);

						if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("if")) {
							RULE_IF();
						} else {
							RULE_STATEMENT_BLOCK();
						}

						// Colocar la etiqueta del final
						codeGenerator.addLabel(endLabel);
					} else {
						// Solo hay if, sin else
						codeGenerator.addLabel(elseLabel);
					}
				} else {
					error(402);
					// Recovery logic...
				}
			} else {
				error(403);
				// Recovery logic...
			}
		} else {
			error(404);
		}

		indentLevel--;
	}

	private void RULE_DO_WHILE() {
		logParseRule("RULE_DO_WHILE");
		indentLevel++;
		if (!isInFirstSetOf("DO_WHILE")) {
			boolean foundFirst = skipUntilFirstOrFollow("DO_WHILE", 900);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping DO_WHILE rule");
				indentLevel--;
				return;
			}
		}
		if (tokens.get(currentToken).getValue().equals("do")) {
			currentToken++;
			logParseRule("do");
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
				logParseRule("while");
				if (tokens.get(currentToken).getValue().equals("(")) {
					currentToken++;
					logParseRule("(");
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
						logParseRule(")");
						if (tokens.get(currentToken).getValue().equals(";")) {
							currentToken++;
							logParseRule(";");
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
		indentLevel--;
	}

	private void RULE_FOR() {
		logParseRule("RULE_FOR");
		indentLevel++;
		if (!isInFirstSetOf("FOR")) {
			boolean foundFirst = skipUntilFirstOrFollow("FOR", 1000);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping FOR rule");
				indentLevel--;
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("for")) {
			currentToken++;
			logParseRule("for");
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				logParseRule("(");
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
					logParseRule(";");
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
					logParseRule(";");
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
					logParseRule(")");
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
		indentLevel--;
	}

	private void RULE_SWITCH() {
		logParseRule("--- RULE_SWITCH");
		indentLevel++;
		if (!isInFirstSetOf("SWITCH")) {
			boolean foundFirst = skipUntilFirstOrFollow("SWITCH", 1100);

			if (!foundFirst) {
				System.out.println("Recovered: Skipping SWITCH rule");
				indentLevel--;
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("switch")) {
			currentToken++;
			logParseRule("switch");
			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				logParseRule("(");
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
					logParseRule(")");
					if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
						currentToken++;
						logParseRule("{");
						while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
							if (tokens.get(currentToken).getValue().equals("case")) {
								currentToken++;
								logParseRule("case");
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
									logParseRule(":");
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
										logParseRule("break");
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
											currentToken++;
											logParseRule(";");
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
								logParseRule("default");
								if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
									currentToken++;
									logParseRule(":");
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
							logParseRule("}");
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
						logParseRule("{");
						// Complete switch body processing
						while (currentToken < tokens.size() && !tokens.get(currentToken).getValue().equals("}")) {
							if (tokens.get(currentToken).getValue().equals("case")) {
								currentToken++;
								logParseRule("case");
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
									logParseRule(":");
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
										logParseRule("break");
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
											currentToken++;
											logParseRule(";");
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
								logParseRule("default");
								if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(":")) {
									currentToken++;
									logParseRule(":");
									try {
										while (currentToken < tokens.size() &&
												!tokens.get(currentToken).getValue().equals("break") &&
												!tokens.get(currentToken).getValue().equals("case") &&
												!tokens.get(currentToken).getValue().equals("}")) {
											RULE_BODY();
										}
										if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("break")) {
											currentToken++;
											logParseRule("break");
											if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(";")) {
												currentToken++;
												logParseRule(";");
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
							logParseRule("}");
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
		indentLevel--;
	}

	private void RULE_STATEMENT_BLOCK() {
		logParseRule("---- RULE_STATEMENT_BLOCK");
		indentLevel++;
		if (!isInFirstSetOf("STATEMENT_BLOCK")) {
			boolean foundFirst = skipUntilFirstOrFollow("STATEMENT_BLOCK", 1200);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping STATEMENT_BLOCK rule");
				indentLevel--;
				return;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("{")) {
			currentToken++;
			logParseRule("{");
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
				logParseRule("}");
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
						logParseRule(";");
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
						logParseRule(";");
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
						logParseRule(";");
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
						logParseRule(";");
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
		indentLevel--;
	}

	private void RULE_EXPRESSION() {
		logParseRule("--- RULE_EXPRESSION");
		indentLevel++;
		if (!isInFirstSetOf("X")) {
			boolean foundFirst = skipUntilFirstOrFollow("X", 1400);

			if (!foundFirst) {
				System.out.println("Recovered: Skipping EXPRESSION rule");
				indentLevel--;
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
		indentLevel--;
	}

	private void RULE_X() {
		logParseRule("---- RULE_X");
		indentLevel++;
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
			logParseRule("&&");
			if (!isInFirstSetOf("Y")) {
				boolean foundFirst = skipUntilFirstOrFollow("Y", 1411);
				if (!foundFirst) {
					System.out.println("Recovered: Missing operand after &&");
					break;
				}
			}
			RULE_Y();
		}
		indentLevel--;
	}

	private void RULE_Y() {
		logParseRule("RULE_Y");
		indentLevel++;
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("!")) {
			currentToken++;
			logParseRule("!");
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
		indentLevel--;
	}

	private void RULE_R() {
		logParseRule("------ RULE_R");
		indentLevel++;
		if (!isInFirstSetOf("E")) {
			boolean foundFirst = skipUntilFirstOrFollow("E", 1430);

			if (!foundFirst) {
				System.out.println("Recovered: Skipping R rule");
				indentLevel--;
				return;
			}
		}
		RULE_E();
		String currentVal = currentToken < tokens.size() ? tokens.get(currentToken).getValue() : "";
		if (currentToken < tokens.size() &&
				(currentVal.equals("<") || currentVal.equals(">") ||
						currentVal.equals("==") || currentVal.equals("!="))) {
			logParseRule(currentVal);
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
		indentLevel--;
	}

	private void RULE_E() {
		logParseRule("RULE_E");
		indentLevel++;
		if (!isInFirstSetOf("A")) {
			boolean foundFirst = skipUntilFirstOrFollow("A", 1440);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping E rule");
				indentLevel--;
				return;
			}
		}
		RULE_A();
		while (currentToken < tokens.size() &&
				(tokens.get(currentToken).getValue().equals("+") ||
						tokens.get(currentToken).getValue().equals("-"))) {
			String operator = tokens.get(currentToken).getValue();
			logParseRule(operator);
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
		indentLevel--;
	}

	private void RULE_A() {
		logParseRule("RULE_A");
		indentLevel++;
		if (!isInFirstSetOf("B")) {
			boolean foundFirst = skipUntilFirstOrFollow("B", 1450);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping A rule");
				indentLevel--;
				return;
			}
		}
		RULE_B();
		while (currentToken < tokens.size() &&
				(tokens.get(currentToken).getValue().equals("*") ||
						tokens.get(currentToken).getValue().equals("/"))) {
			String operator = tokens.get(currentToken).getValue();
			logParseRule(operator);
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
		indentLevel--;
	}

	private void RULE_B() {
		logParseRule("RULE_B");
		indentLevel++;
		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("-")) {
			currentToken++;
			logParseRule("-");
		}
		if (!isInFirstSetOf("C")) {
			boolean foundFirst = skipUntilFirstOrFollow("C", 1460);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping B rule");
				indentLevel--;
				return;
			}
		}
		RULE_C();
		indentLevel--;
	}

	private void RULE_C() {
		logParseRule("RULE_C");
		indentLevel++;
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
				logParseRule("IDENTIFIER: " + tokens.get(currentToken).getValue());
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
			logParseRule("LITERAL: " + tokens.get(currentToken).getValue());
			currentToken++;
		} else if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
			currentToken++;
			logParseRule("(");
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
				logParseRule(")");
			} else {
				error(1472);
				System.out.println("Recovered: Missing closing parenthesis");
			}
		} else {
			error(1473);
			System.out.println("Recovered: Invalid expression element");
		}
		indentLevel--;
	}

	private void RULE_TYPE() {
		logParseRule("----- RULE_TYPE");
		indentLevel++;
		if (!isInFirstSetOf("TYPE")) {
			boolean foundFirst = skipUntilFirstOrFollow("TYPE", 300);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping TYPE rule");
				indentLevel--;
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
			logParseRule("TYPE: " + tokens.get(currentToken).getValue());
			currentToken++;
		} else {
			error(301);
		}
		indentLevel--;
	}

	//Metodo para manejar println
	private void RULE_PRINTLN() {
		logParseRule("RULE_PRINTLN");
		indentLevel++;

		if (!isInFirstSetOf("PRINTLN")) {
			boolean foundFirst = skipUntilFirstOrFollow("PRINTLN", 1500);
			if (!foundFirst) {
				System.out.println("Recovered: Skipping PRINTLN rule");
				indentLevel--;
				return;
			}
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("println")) {
			currentToken++;
			logParseRule("println");

			if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals("(")) {
				currentToken++;
				logParseRule("(");

				// Capturar el valor a imprimir ANTES de procesar la expresión
				String printValue = null;
				if (currentToken < tokens.size()) {
					printValue = tokens.get(currentToken).getValue();
					logParseRule("PARAMETER: " + printValue);

					// GENERACIÓN DE CÓDIGO: println
					codeGenerator.generatePrintln(printValue);
				}

				// Procesar la expresión
				if (!isInFirstSetOf("EXPRESSION")) {
					boolean foundFirst = skipUntilFirstOrFollow("EXPRESSION", 1501);
					if (!foundFirst) {
						System.out.println("Recovered: Missing expression in println");
						indentLevel--;
						return;
					}
				}

				RULE_EXPRESSION(); // Procesar la expresión

				if (currentToken < tokens.size() && tokens.get(currentToken).getValue().equals(")")) {
					currentToken++;
					logParseRule(")");
				} else {
					error(1502);
					// Recovery: buscar el siguiente token válido
					while (currentToken < tokens.size() &&
							!tokens.get(currentToken).getValue().equals(";") &&
							!isInFollowSetOf("PRINTLN")) {
						currentToken++;
					}
					System.out.println("Recovered: Missing closing parenthesis in println");
				}
			} else {
				error(1503);
				// Recovery logic
				while (currentToken < tokens.size() &&
						!tokens.get(currentToken).getValue().equals(";") &&
						!isInFollowSetOf("PRINTLN")) {
					currentToken++;
				}
				System.out.println("Recovered: Missing opening parenthesis in println");
			}
		} else {
			error(1504);
		}

		indentLevel--;
	}

	private boolean isPrintlnStatement() {
		return currentToken < tokens.size() &&
				tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("println");
	}

	private boolean isInputlnStatement() {
		return currentToken < tokens.size() &&
				tokens.get(currentToken).getType().equals("KEYWORD") &&
				tokens.get(currentToken).getValue().equals("inputln");
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

	public TheSemantic getSemantic() {
		return semantic;
	}

	private String evaluateExpression() {
		int startToken = currentToken;
		int saveCurrentToken = currentToken; // Guardar posición actual

		try {
			// Primero intentar evaluar la expresión sin avanzar el parser
			String result = evaluateExpressionHelper(startToken);
			return result;
		} finally {
			// Restaurar la posición del token después de la evaluación
			currentToken = saveCurrentToken;
			// Ahora sí procesar la expresión normalmente
			RULE_EXPRESSION();
		}
	}

	private String evaluateExpressionHelper(int startPos) {
		if (startPos >= tokens.size()) {
			return null;
		}

		// Para expresiones simples como literals
		String tokenType = tokens.get(startPos).getType();
		String tokenValue = tokens.get(startPos).getValue();

		// Caso: literal directo
		switch (tokenType) {
			case "INTEGER":
				return "int";
			case "FLOAT":
				return "float";
			case "STRING":
				return "string";
			case "CHAR":
				return "char";
			case "KEYWORD":
				if (tokenValue.equals("true") || tokenValue.equals("false")) {
					return "boolean";
				}
				break;
			case "IDENTIFIER":
				// Buscar el tipo de la variable en la tabla de símbolos
				return semantic.checkVariableUsage(tokenValue, tokens.get(startPos).getLineNumber());
		}

		// Para expresiones más complejas, necesitamos analizar la estructura
		return evaluateComplexExpression(startPos);
	}

	private String evaluateComplexExpression(int startPos) {
		// Buscar operadores en la expresión para determinar el tipo resultado
		int pos = startPos;
		String leftType = null;
		String operator = null;
		String rightType = null;

		// Obtener el primer operando
		if (pos < tokens.size()) {
			String tokenType = tokens.get(pos).getType();
			String tokenValue = tokens.get(pos).getValue();

			switch (tokenType) {
				case "INTEGER":
					leftType = "int";
					break;
				case "FLOAT":
					leftType = "float";
					break;
				case "STRING":
					leftType = "string";
					break;
				case "CHAR":
					leftType = "char";
					break;
				case "KEYWORD":
					if (tokenValue.equals("true") || tokenValue.equals("false")) {
						leftType = "boolean";
					}
					break;
				case "IDENTIFIER":
					leftType = semantic.checkVariableUsage(tokenValue, tokens.get(pos).getLineNumber());
					break;
			}
			pos++;
		}

		// Buscar operador
		while (pos < tokens.size()) {
			String tokenValue = tokens.get(pos).getValue();
			if (isComparisonOperator(tokenValue) || isArithmeticOperator(tokenValue) || isLogicalOperator(tokenValue)) {
				operator = tokenValue;
				pos++;
				break;
			}
			pos++;
		}

		// Si no hay operador, retornar el tipo del operando izquierdo
		if (operator == null) {
			return leftType;
		}

		// Obtener el segundo operando
		if (pos < tokens.size()) {
			String tokenType = tokens.get(pos).getType();
			String tokenValue = tokens.get(pos).getValue();

			switch (tokenType) {
				case "INTEGER":
					rightType = "int";
					break;
				case "FLOAT":
					rightType = "float";
					break;
				case "STRING":
					rightType = "string";
					break;
				case "CHAR":
					rightType = "char";
					break;
				case "KEYWORD":
					if (tokenValue.equals("true") || tokenValue.equals("false")) {
						rightType = "boolean";
					}
					break;
				case "IDENTIFIER":
					rightType = semantic.checkVariableUsage(tokenValue, tokens.get(pos).getLineNumber());
					break;
			}
		}
		// Determinar el tipo resultado basado en el operador
		if (isComparisonOperator(operator)) {
			return "boolean"; // Los operadores de comparación siempre retornan boolean
		} else if (isLogicalOperator(operator)) {
			return "boolean"; // Los operadores lógicos siempre retornan boolean
		} else if (isArithmeticOperator(operator)) {
			// Para operadores aritméticos, usar el cubo semántico
			return semantic.checkOperation(leftType, rightType, operator, tokens.get(startPos).getLineNumber());
		}

		return leftType; // Por defecto, retornar el tipo del operando izquierdo
	}

	// Métodos auxiliares para detectar tipos de operadores
	private boolean isLogicalOperator(String op) {
		return op.equals("&&") || op.equals("||") || op.equals("!");
	}

	private void generateConditionCode() {
		// Guardar posición actual para análisis
		int startPos = currentToken;

		// Para x > 0, necesitamos generar:
		// lod x, 0    (cargar x)
		// lit 0, 0    (cargar 0)
		// opr 11, 0   (operación mayor que)

		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			String leftOperand = tokens.get(currentToken).getValue();
			codeGenerator.generateLoad(leftOperand); // lod x, 0

			// Buscar el operador
			currentToken++; // Saltar identificador
			if (currentToken < tokens.size() && isComparisonOperator(tokens.get(currentToken).getValue())) {
				String operator = tokens.get(currentToken).getValue();

				currentToken++; // Saltar operador
				if (currentToken < tokens.size()) {
					String rightOperand = tokens.get(currentToken).getValue();

					// Si es un literal, generar LIT
					if (tokens.get(currentToken).getType().equals("INTEGER") ||
							tokens.get(currentToken).getType().equals("FLOAT")) {
						codeGenerator.generateLiteral(rightOperand); // lit 0, 0
					} else if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
						codeGenerator.generateLoad(rightOperand); // lod rightVar, 0
					}

					// Generar la operación de comparación
					codeGenerator.generateComparisonOperation(operator); // opr 11, 0
				}
			}
		}

		// Restaurar posición para el parsing normal
		currentToken = startPos;
	}

	// Metodo auxiliar para detectar operadores de comparación
	private boolean isComparisonOperator(String op) {
		return op.equals(">") || op.equals("<") || op.equals("==") || op.equals("!=") ||
				op.equals(">=") || op.equals("<=");
	}

	public Vector<String> getParseTreeLog() {
		return parseTreeLog;
	}

	private void logParseRule(String ruleName) {
		String indent = "  ".repeat(indentLevel);
		parseTreeLog.add(indent + ruleName);
		System.out.println(ruleName);
	}
	//Getter para el generador de código
	public TheCodeGenerator getCodeGenerator() {
		return codeGenerator;
	}

	//Metodo para finalizar la generación de código
	public Vector<String> getIntermediateCode() {
		codeGenerator.setSymbolTable(semantic.getSymbolTable());
		return codeGenerator.generateCode();
	}

	//Metodo para generar código de expresiones
	private String generateExpressionCode() {
		int startPos = currentToken;

		// Para expresiones como "x - 1", necesitamos generar:
		// lod x, 0    (cargar x)
		// lit 1, 0    (cargar 1)
		// opr 3, 0    (operación resta)

		if (currentToken < tokens.size()) {
			String tokenType = tokens.get(currentToken).getType();
			String tokenValue = tokens.get(currentToken).getValue();
			// Caso simple: literal directo
			if (tokenType.equals("INTEGER") || tokenType.equals("FLOAT") ||
					tokenType.equals("STRING") || tokenType.equals("CHAR")) {
				return tokenValue;
			}
			// Caso simple: variable directa
			if (tokenType.equals("IDENTIFIER") &&
					(currentToken + 1 >= tokens.size() ||
							(!isArithmeticOperator(tokens.get(currentToken + 1).getValue())))) {
				return tokenValue;
			}
			// Caso complejo: expresión aritmética (ej: x - 1)
			if (tokenType.equals("IDENTIFIER") &&
					currentToken + 1 < tokens.size() &&
					isArithmeticOperator(tokens.get(currentToken + 1).getValue())) {
				generateArithmeticExpression();
				return null; // El código ya fue generado
			}
			// Casos booleanos
			if (tokenType.equals("KEYWORD")) {
				if (tokenValue.equals("true") || tokenValue.equals("false")) {
					return tokenValue;
				}
			}
		}
		return null;
	}

	//Metodo para generar expresiones aritméticas
	private void generateArithmeticExpression() {
		// Para x - 1:
		if (currentToken < tokens.size() && tokens.get(currentToken).getType().equals("IDENTIFIER")) {
			String leftOperand = tokens.get(currentToken).getValue();
			codeGenerator.generateLoad(leftOperand); // lod x, 0
			currentToken++; // Saltar identificador
			if (currentToken < tokens.size() && isArithmeticOperator(tokens.get(currentToken).getValue())) {
				String operator = tokens.get(currentToken).getValue();
				currentToken++; // Saltar operador
				if (currentToken < tokens.size()) {
					String rightOperand = tokens.get(currentToken).getValue();
					// Si es un literal, generar LIT
					if (tokens.get(currentToken).getType().equals("INTEGER") ||
							tokens.get(currentToken).getType().equals("FLOAT")) {
						codeGenerator.generateLiteral(rightOperand); // lit 1, 0
					} else if (tokens.get(currentToken).getType().equals("IDENTIFIER")) {
						codeGenerator.generateLoad(rightOperand); // lod rightVar, 0
					}
					// Generar la operación aritmética
					codeGenerator.generateArithmeticOperation(operator); // opr 3, 0 (resta)
				}
			}
		}
	}

	//Metodo auxiliar para detectar operadores aritméticos
	private boolean isArithmeticOperator(String op) {
		return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
	}
}
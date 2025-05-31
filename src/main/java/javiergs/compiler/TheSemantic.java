package javiergs.compiler;

import java.util.*;

/**
 * Semantic Analyzer for the compiler
 * Implements semantic analysis including:
 * 1. Symbol table management
 * 2. Type checking using semantic cube
 * 3. Variable declaration and usage verification
 * 4. Method parameter and return type checking
 * 5. Scope management
 *
 * @author javiergs
 * @author eduardomv
 * @author santiarr
 * @author yawham
 * @version 2.0
 */
public class TheSemantic {

    // Symbol table to store variables and methods
    private Hashtable<String, Vector<SymbolTableItem>> symbolTable;

    // Current scope management
    private String currentScope;
    private Stack<String> scopeStack;

    // Semantic cube for type checking operations
    private String[][][] semanticCube;

    // Error collection
    private Vector<String> semanticErrors;

    // Method information storage
    private Hashtable<String, MethodInfo> methodTable;

    // Type constants
    public static final int INT = 0;
    public static final int FLOAT = 1;
    public static final int CHAR = 2;
    public static final int STRING = 3;
    public static final int BOOLEAN = 4;
    public static final int VOID = 5;

    // Operator constants
    public static final int OP_PLUS = 0;
    public static final int OP_MINUS = 1;
    public static final int OP_MULT = 2;
    public static final int OP_DIV = 3;
    public static final int OP_AND = 4;
    public static final int OP_OR = 5;
    public static final int OP_NOT = 6;
    public static final int OP_GT = 7;
    public static final int OP_LT = 8;
    public static final int OP_EQ = 9;
    public static final int OP_NE = 10;
    public static final int OP_ASSIGN = 11;

    /**
     * Constructor - Initializes the semantic analyzer
     */
    public TheSemantic() {
        symbolTable = new Hashtable<>();
        methodTable = new Hashtable<>();
        semanticErrors = new Vector<>();
        scopeStack = new Stack<>();
        currentScope = "global";
        scopeStack.push(currentScope);

        initializeSemanticCube();
    }

    /**
     * Initializes the semantic cube for type checking
     */
    private void initializeSemanticCube() {
        // Create 3D array: [type1][type2][operator] = resultType
        semanticCube = new String[6][6][12];

        // Initialize all combinations to null (invalid)
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 12; k++) {
                    semanticCube[i][j][k] = null;
                }
            }
        }

        // Arithmetic operations (+, -, *, /)
        // int + int = int
        semanticCube[INT][INT][OP_PLUS] = "int";
        semanticCube[INT][INT][OP_MINUS] = "int";
        semanticCube[INT][INT][OP_MULT] = "int";
        semanticCube[INT][INT][OP_DIV] = "int";

        // float + int = float, int + float = float
        semanticCube[FLOAT][INT][OP_PLUS] = "float";
        semanticCube[INT][FLOAT][OP_PLUS] = "float";
        semanticCube[FLOAT][INT][OP_MINUS] = "float";
        semanticCube[INT][FLOAT][OP_MINUS] = "float";
        semanticCube[FLOAT][INT][OP_MULT] = "float";
        semanticCube[INT][FLOAT][OP_MULT] = "float";
        semanticCube[FLOAT][INT][OP_DIV] = "float";
        semanticCube[INT][FLOAT][OP_DIV] = "float";

        // float + float = float
        semanticCube[FLOAT][FLOAT][OP_PLUS] = "float";
        semanticCube[FLOAT][FLOAT][OP_MINUS] = "float";
        semanticCube[FLOAT][FLOAT][OP_MULT] = "float";
        semanticCube[FLOAT][FLOAT][OP_DIV] = "float";

        // String concatenation
        semanticCube[STRING][STRING][OP_PLUS] = "string";
        semanticCube[STRING][INT][OP_PLUS] = "string";
        semanticCube[INT][STRING][OP_PLUS] = "string";
        semanticCube[STRING][FLOAT][OP_PLUS] = "string";
        semanticCube[FLOAT][STRING][OP_PLUS] = "string";
        semanticCube[STRING][CHAR][OP_PLUS] = "string";
        semanticCube[CHAR][STRING][OP_PLUS] = "string";
        semanticCube[STRING][BOOLEAN][OP_PLUS] = "string";
        semanticCube[BOOLEAN][STRING][OP_PLUS] = "string";

        // Logical operations (&&, ||)
        semanticCube[BOOLEAN][BOOLEAN][OP_AND] = "boolean";
        semanticCube[BOOLEAN][BOOLEAN][OP_OR] = "boolean";

        // Comparison operations (<, >, ==, !=)
        semanticCube[INT][INT][OP_GT] = "boolean";
        semanticCube[INT][INT][OP_LT] = "boolean";
        semanticCube[FLOAT][FLOAT][OP_GT] = "boolean";
        semanticCube[FLOAT][FLOAT][OP_LT] = "boolean";
        semanticCube[INT][FLOAT][OP_GT] = "boolean";
        semanticCube[FLOAT][INT][OP_GT] = "boolean";
        semanticCube[INT][FLOAT][OP_LT] = "boolean";
        semanticCube[FLOAT][INT][OP_LT] = "boolean";

        // Equality operations (==, !=)
        semanticCube[INT][INT][OP_EQ] = "boolean";
        semanticCube[INT][INT][OP_NE] = "boolean";
        semanticCube[FLOAT][FLOAT][OP_EQ] = "boolean";
        semanticCube[FLOAT][FLOAT][OP_NE] = "boolean";
        semanticCube[CHAR][CHAR][OP_EQ] = "boolean";
        semanticCube[CHAR][CHAR][OP_NE] = "boolean";
        semanticCube[STRING][STRING][OP_EQ] = "boolean";
        semanticCube[STRING][STRING][OP_NE] = "boolean";
        semanticCube[BOOLEAN][BOOLEAN][OP_EQ] = "boolean";
        semanticCube[BOOLEAN][BOOLEAN][OP_NE] = "boolean";

        // Assignment operations
        semanticCube[INT][INT][OP_ASSIGN] = "int";
        semanticCube[FLOAT][FLOAT][OP_ASSIGN] = "float";
        semanticCube[FLOAT][INT][OP_ASSIGN] = "float"; // int can be assigned to float
        semanticCube[CHAR][CHAR][OP_ASSIGN] = "char";
        semanticCube[STRING][STRING][OP_ASSIGN] = "string";
        semanticCube[BOOLEAN][BOOLEAN][OP_ASSIGN] = "boolean";
    }

    /**
     * Checks if a variable declaration is valid and adds it to symbol table
     * @param type Variable type
     * @param identifier Variable name
     * @param lineNumber Line number for error reporting
     */
    public void checkVariableDeclaration(String type, String identifier, int lineNumber) {
        String key = currentScope + "." + identifier;

        // Check if variable already exists in current scope
        if (symbolTable.containsKey(key)) {
            semanticErrors.add("Semantic Error: Variable '" + identifier +
                    "' is already declared in scope '" + currentScope +
                    "' at line " + lineNumber);
            return;
        }

        // Add variable to symbol table
        Vector<SymbolTableItem> items = new Vector<>();
        String defaultValue = getDefaultValue(type);
        items.add(new SymbolTableItem(type, currentScope, defaultValue));
        symbolTable.put(key, items);

        System.out.println("Variable declared: " + identifier + " of type " + type + " in scope " + currentScope);
    }

    /**
     * Checks if a variable is declared before usage
     * @param identifier Variable name
     * @param lineNumber Line number for error reporting
     * @return Variable type if found, null otherwise
     */
    public String checkVariableUsage(String identifier, int lineNumber) {
        // First check current scope
        String key = currentScope + "." + identifier;
        if (symbolTable.containsKey(key)) {
            return symbolTable.get(key).get(0).getType();
        }

        // Then check global scope
        key = "global." + identifier;
        if (symbolTable.containsKey(key)) {
            return symbolTable.get(key).get(0).getType();
        }

        // Variable not found
        semanticErrors.add("Semantic Error: Variable '" + identifier +
                "' is not declared at line " + lineNumber);
        return null;
    }

    /**
     * Checks if an operation between two types is valid
     * @param type1 First operand type
     * @param type2 Second operand type
     * @param operator Operation
     * @param lineNumber Line number for error reporting
     * @return Result type if valid, null otherwise
     */
    public String checkOperation(String type1, String type2, String operator, int lineNumber) {
        if (type1 == null || type2 == null) {
            return null; // Previous errors
        }

        int t1 = getTypeIndex(type1);
        int t2 = getTypeIndex(type2);
        int op = getOperatorIndex(operator);

        if (t1 == -1 || t2 == -1 || op == -1) {
            semanticErrors.add("Semantic Error: Invalid operation '" + type1 + " " + operator + " " + type2 +
                    "' at line " + lineNumber);
            return null;
        }

        String resultType = semanticCube[t1][t2][op];
        if (resultType == null) {
            semanticErrors.add("Semantic Error: Incompatible types '" + type1 + "' and '" + type2 +
                    "' for operator '" + operator + "' at line " + lineNumber);
            return null;
        }

        return resultType;
    }

    /**
     * Checks assignment compatibility
     * @param leftType Variable type being assigned to
     * @param rightType Expression type being assigned
     * @param lineNumber Line number for error reporting
     * @return true if assignment is valid
     */
    public boolean checkAssignment(String leftType, String rightType, int lineNumber) {
        if (leftType == null || rightType == null) {
            return false;
        }

        String resultType = checkOperation(leftType, rightType, "=", lineNumber);
        return resultType != null;
    }

    /**
     * Checks if expression type is boolean (for conditions)
     * @param expressionType Type of the expression
     * @param context Context (if, while, for)
     * @param lineNumber Line number for error reporting
     * @return true if expression is boolean
     */
    public boolean checkBooleanExpression(String expressionType, String context, int lineNumber) {
        if (expressionType == null) {
            return false;
        }

        if (!expressionType.equals("boolean")) {
            semanticErrors.add("Semantic Error: " + context + " condition must be boolean, found '" +
                    expressionType + "' at line " + lineNumber);
            return false;
        }

        return true;
    }

    /**
     * Declares a method in the method table
     * @param returnType Method return type
     * @param methodName Method name
     * @param parameters Method parameters
     * @param lineNumber Line number for error reporting
     */
    public void declareMethod(String returnType, String methodName, Vector<Parameter> parameters, int lineNumber) {
        if (methodTable.containsKey(methodName)) {
            semanticErrors.add("Semantic Error: Method '" + methodName +
                    "' is already declared at line " + lineNumber);
            return;
        }

        MethodInfo method = new MethodInfo(returnType, methodName, parameters);
        methodTable.put(methodName, method);

        System.out.println("Method declared: " + methodName + " returning " + returnType);
    }

    /**
     * Checks method call parameters
     * @param methodName Method being called
     * @param argumentTypes Types of arguments passed
     * @param lineNumber Line number for error reporting
     * @return Method return type if call is valid
     */
    public String checkMethodCall(String methodName, Vector<String> argumentTypes, int lineNumber) {
        if (!methodTable.containsKey(methodName)) {
            semanticErrors.add("Semantic Error: Method '" + methodName +
                    "' is not declared at line " + lineNumber);
            return null;
        }

        MethodInfo method = methodTable.get(methodName);
        Vector<Parameter> parameters = method.getParameters();

        // Check parameter count
        if (parameters.size() != argumentTypes.size()) {
            semanticErrors.add("Semantic Error: Method '" + methodName + "' expects " +
                    parameters.size() + " parameters, but " + argumentTypes.size() +
                    " were provided at line " + lineNumber);
            return null;
        }

        // Check parameter types
        for (int i = 0; i < parameters.size(); i++) {
            String expectedType = parameters.get(i).getType();
            String actualType = argumentTypes.get(i);

            if (!checkAssignment(expectedType, actualType, lineNumber)) {
                semanticErrors.add("Semantic Error: Parameter " + (i + 1) + " of method '" +
                        methodName + "' expects type '" + expectedType +
                        "' but '" + actualType + "' was provided at line " + lineNumber);
                return null;
            }
        }

        return method.getReturnType();
    }

    /**
     * Checks return statement type
     * @param returnType Type being returned
     * @param expectedType Expected return type of method
     * @param lineNumber Line number for error reporting
     */
    public void checkReturnType(String returnType, String expectedType, int lineNumber) {
        if (expectedType.equals("void") && returnType != null) {
            semanticErrors.add("Semantic Error: Void method cannot return a value at line " + lineNumber);
            return;
        }

        if (!expectedType.equals("void") && returnType == null) {
            semanticErrors.add("Semantic Error: Method must return a value of type '" +
                    expectedType + "' at line " + lineNumber);
            return;
        }

        if (returnType != null && !checkAssignment(expectedType, returnType, lineNumber)) {
            semanticErrors.add("Semantic Error: Return type '" + returnType +
                    "' does not match expected type '" + expectedType +
                    "' at line " + lineNumber);
        }
    }

    /**
     * Enters a new scope
     * @param scopeName Name of the new scope
     */
    public void enterScope(String scopeName) {
        scopeStack.push(currentScope);
        currentScope = scopeName;
        System.out.println("Entering scope: " + scopeName);
    }

    /**
     * Exits current scope
     */
    public void exitScope() {
        System.out.println("Exiting scope: " + currentScope);
        if (!scopeStack.isEmpty()) {
            currentScope = scopeStack.pop();
        }
    }

    /**
     * Gets default value for a type
     */
    private String getDefaultValue(String type) {
        switch (type.toLowerCase()) {
            case "int": return "0";
            case "float": return "0.0";
            case "char": return "''";
            case "string": return "\"\"";
            case "boolean": return "false";
            case "void": return "";
            default: return "";
        }
    }

    /**
     * Gets type index for semantic cube
     */
    private int getTypeIndex(String type) {
        switch (type.toLowerCase()) {
            case "int": return INT;
            case "float": return FLOAT;
            case "char": return CHAR;
            case "string": return STRING;
            case "boolean": return BOOLEAN;
            case "void": return VOID;
            default: return -1;
        }
    }

    /**
     * Gets operator index for semantic cube
     */
    private int getOperatorIndex(String operator) {
        switch (operator) {
            case "+": return OP_PLUS;
            case "-": return OP_MINUS;
            case "*": return OP_MULT;
            case "/": return OP_DIV;
            case "&&": return OP_AND;
            case "||": return OP_OR;
            case "!": return OP_NOT;
            case ">": return OP_GT;
            case "<": return OP_LT;
            case "==": return OP_EQ;
            case "!=": return OP_NE;
            case "=": return OP_ASSIGN;
            default: return -1;
        }
    }

    // Getters
    public Vector<String> getSemanticErrors() {
        return semanticErrors;
    }

    public Hashtable<String, Vector<SymbolTableItem>> getSymbolTable() {
        return symbolTable;
    }

    public boolean hasErrors() {
        return !semanticErrors.isEmpty();
    }

    public void printErrors() {
        for (String error : semanticErrors) {
            System.out.println(error);
        }
    }

    public void printSymbolTable() {
        System.out.println("\nSymbol Table:");
        System.out.println("Scope.Variable\t|\tType\t|\tValue");
        System.out.println("----------------------------------------");
        for (String key : symbolTable.keySet()) {
            SymbolTableItem item = symbolTable.get(key).get(0);
            System.out.println(key + "\t|\t" + item.getType() + "\t|\t" + item.getValue());
        }
    }

    public void setVariableValue(String identifier, String value, int lineNumber) {
        // Buscar primero en scope actual
        String key = currentScope + "." + identifier;
        if (symbolTable.containsKey(key)) {
            symbolTable.get(key).get(0).setValue(value);
            return;
        }

        // Luego en scope global
        key = "global." + identifier;
        if (symbolTable.containsKey(key)) {
            symbolTable.get(key).get(0).setValue(value);
            return;
        }

        // Si no existe, reportar error
        semanticErrors.add("Semantic Error: Variable '" + identifier +
                "' is not declared at line " + lineNumber);
    }
}

/**
 * Represents an item in the symbol table
 */
class SymbolTableItem {
    private String type;
    private String scope;
    private String value;

    public SymbolTableItem(String type, String scope, String value) {
        this.type = type;
        this.scope = scope;
        this.value = value;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

/**
 * Represents method information
 */
class MethodInfo {
    private String returnType;
    private String name;
    private Vector<Parameter> parameters;

    public MethodInfo(String returnType, String name, Vector<Parameter> parameters) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
    }

    // Getters
    public String getReturnType() { return returnType; }
    public String getName() { return name; }
    public Vector<Parameter> getParameters() { return parameters; }
}

/**
 * Represents a method parameter
 */
class Parameter {
    private String type;
    private String name;

    public Parameter(String type, String name) {
        this.type = type;
        this.name = name;
    }

    // Getters
    public String getType() { return type; }
    public String getName() { return name; }
}
package javiergs.compiler;

import java.util.*;

/**
 * Code Generator for the compiler
 * Generates intermediate code compatible with the Virtual Machine
 *
 * @author eduardomv
 * @author santiarr
 * @author yawham
 * @version 1.0
 */
public class TheCodeGenerator {

    private Vector<String> intermediateCode;
    private Hashtable<String, Vector<SymbolTableItem>> symbolTable;
    private int labelCounter;

    public TheCodeGenerator() {
        intermediateCode = new Vector<>();
        labelCounter = 0;
    }

    /**
     * Sets the symbol table from semantic analysis
     */
    public void setSymbolTable(Hashtable<String, Vector<SymbolTableItem>> symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Generates the complete intermediate code
     */
    public Vector<String> generateCode() {
        Vector<String> completeCode = new Vector<>();

        // First, add symbol table entries
        generateSymbolTableEntries(completeCode);

        // Add labels to symbol table ONLY if there are any
        if (hasLabels()) {
            addLabelsToSymbolTable(completeCode);
        }

        // Add separator
        completeCode.add("@");

        // Add generated instructions with label resolution
        addInstructionsWithLabelResolution(completeCode);

        // Add program termination
        completeCode.add("opr 0, 0");

        return completeCode;
    }

    /**
     * Verificar si hay etiquetas en el código
     */
    private boolean hasLabels() {
        for (String instruction : intermediateCode) {
            if (instruction.endsWith(":")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Agregar etiquetas a la tabla de símbolos
     */
    private void addLabelsToSymbolTable(Vector<String> code) {
        int instructionNumber = 1;
        Map<String, Integer> labelPositions = new HashMap<>();
        int realInstructionCount = 1;

        for (String instruction : intermediateCode) {
            if (instruction.endsWith(":")) {
                String labelName = instruction.substring(0, instruction.length() - 1);
                labelPositions.put(labelName, realInstructionCount);
            } else {
                realInstructionCount++;
            }
        }

        for (Map.Entry<String, Integer> entry : labelPositions.entrySet()) {
            // Formato correcto: #labelName, int, global, position
            code.add(entry.getKey() + ", int, global, " + entry.getValue());
        }
    }

    /**
     * Agregar instrucciones resolviendo las etiquetas
     */
    private void addInstructionsWithLabelResolution(Vector<String> code) {
        for (String instruction : intermediateCode) {
            if (!instruction.endsWith(":")) {
                // No es una etiqueta, agregar la instrucción
                code.add(instruction);
            }
            // Las etiquetas se omiten del código final (ya están en la tabla de símbolos)
        }
    }

    /**
     * Generates symbol table entries for the intermediate code
     */
    private void generateSymbolTableEntries(Vector<String> code) {
        if (symbolTable != null) {
            Enumeration<String> keys = symbolTable.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                SymbolTableItem item = symbolTable.get(key).get(0);

                // Extract variable name (remove scope prefix)
                String varName = key.substring(key.indexOf('.') + 1);

                // Format: variableName, type, scope, defaultValue
                String entry = varName + ", " + item.getType() + ", " + item.getScope() + ", " + getDefaultValueForVM(item.getType());
                code.add(entry);
            }
        }
    }

    /**
     * Generates code for assignment when the value is already in the stack
     * (resultado de una expresión compleja)
     */
    public void generateAssignmentFromStack(String varName) {
        // El resultado de la expresión ya está en el stack
        // Solo necesitamos almacenarlo
        intermediateCode.add("sto " + varName + ", 0");
    }

    /**
     * Gets default value for VM format
     */
    private String getDefaultValueForVM(String type) {
        switch (type.toLowerCase()) {
            case "int": return "0";
            case "float": return "0.0";
            case "char": return "''";
            case "string": return "\"\"";
            case "boolean": return "false";
            default: return "0";
        }
    }

    /**
     * Generates code for variable declaration with initialization
     */
    public void generateVariableDeclaration(String varName, String value) {
        // LIT value, 0
        intermediateCode.add("lit " + value + ", 0");

        // STO varName, 0
        intermediateCode.add("sto " + varName + ", 0");
    }

    /**
     * Generates code for variable assignment
     */
    public void generateAssignment(String varName, String value) {
        // If it's a literal value
        if (isLiteral(value)) {
            intermediateCode.add("lit " + value + ", 0");
        } else {
            // If it's a variable, load it
            intermediateCode.add("lod " + value + ", 0");
        }

        // Store in destination variable
        intermediateCode.add("sto " + varName + ", 0");
    }

    /**
     * Generates code for arithmetic operations
     */
    public void generateArithmeticOperation(String op) {
        switch (op) {
            case "+":
                intermediateCode.add("opr 2, 0"); // Addition
                break;
            case "-":
                intermediateCode.add("opr 3, 0"); // Subtraction
                break;
            case "*":
                intermediateCode.add("opr 4, 0"); // Multiplication
                break;
            case "/":
                intermediateCode.add("opr 5, 0"); // Division
                break;
        }
    }

    /**
     * Generates code for comparison operations
     */
    public void generateComparisonOperation(String op) {
        switch (op) {
            case ">":
                intermediateCode.add("opr 11, 0"); // Greater than
                break;
            case "<":
                intermediateCode.add("opr 12, 0"); // Less than
                break;
            case "==":
                intermediateCode.add("opr 15, 0"); // Equal
                break;
            case "!=":
                intermediateCode.add("opr 16, 0"); // Not equal
                break;
        }
    }

    /**
     * Generates code for logical operations
     */
    public void generateLogicalOperation(String op) {
        switch (op) {
            case "&&":
                intermediateCode.add("opr 9, 0"); // AND
                break;
            case "||":
                intermediateCode.add("opr 8, 0"); // OR
                break;
            case "!":
                intermediateCode.add("opr 10, 0"); // NOT
                break;
        }
    }

    /**
     * Generates code for println statement
     */
    public void generatePrintln(String value) {
        if (isLiteral(value)) {
            intermediateCode.add("lit " + value + ", 0");
        } else {
            intermediateCode.add("lod " + value + ", 0");
        }
        intermediateCode.add("opr 21, 0"); // Print with newline
    }

    /**
     * Generates code for print statement (without newline)
     */
    public void generatePrint(String value) {
        if (isLiteral(value)) {
            intermediateCode.add("lit " + value + ", 0");
        } else {
            intermediateCode.add("lod " + value + ", 0");
        }
        intermediateCode.add("opr 20, 0"); // Print without newline
    }

    /**
     * Generates labels for control structures
     */
    public String generateLabel() {
        return "#label" + (++labelCounter);
    }

    /**
     * Generates conditional jump - CORREGIDO para VM
     * Para while: queremos saltar cuando la condición es FALSE
     * Para if: queremos saltar cuando la condición es FALSE
     */
    public void generateConditionalJump(String label, String condition) {
        if (condition.equals("false")) {
            // Saltar cuando el resultado en el stack es 0 (false)
            // JMC salta cuando el valor es igual al parámetro
            intermediateCode.add("jmc " + label + ", 0");
        } else if (condition.equals("true")) {
            // Saltar cuando el resultado en el stack es 1 (true)
            intermediateCode.add("jmc " + label + ", 1");
        } else {
            // Usar el valor tal como viene
            intermediateCode.add("jmc " + label + ", " + condition);
        }
    }

    /**
     * Generates unconditional jump
     */
    public void generateJump(String label) {
        intermediateCode.add("jmp " + label + ", 0");
    }

    /**
     * Metodo específico para while loops
     */
    public void generateWhileConditionalJump(String endLabel) {
        // En while: si la condición es false (0), saltar al final
        // La VM evalúa: si stack_top == 0, salta a endLabel
        intermediateCode.add("jmc " + endLabel + ", 0");
    }

    /**
     * Metodo específico para if statements
     */
    public void generateIfConditionalJump(String elseLabel) {
        // En if: si la condición es false (0), saltar al else
        intermediateCode.add("jmc " + elseLabel + ", 0");
    }

    /**
     * Adds a label to the code
     */
    public void addLabel(String label) {
        // Agregar la etiqueta directamente al código intermedio
        intermediateCode.add(label + ":");
    }

    /**
     * Generates code to load a variable onto stack
     */
    public void generateLoad(String varName) {
        intermediateCode.add("lod " + varName + ", 0");
    }


    /**
     * Generates code to load a literal onto stack
     */
    public void generateLiteral(String value) {
        intermediateCode.add("lit " + value + ", 0");
    }

    /**
     * Generates code for inputln statement
     */
    public void generateInputln() {
        intermediateCode.add("opr 22, 0"); // Input operation
    }

    /**
     * Metodo específico para do-while loops
     */
    public void generateDoWhileConditionalJump(String startLabel) {
        // En do-while: si la condición es true (1), volver al inicio
        intermediateCode.add("jmc " + startLabel + ", 1");
    }

    /**
     * Checks if a value is a literal
     */
    private boolean isLiteral(String value) {
        // Check if it's a number
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e1) {
            try {
                Float.parseFloat(value);
                return true;
            } catch (NumberFormatException e2) {
                // Check if it's a string literal
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    return true;
                }
                // Check if it's a char literal
                if (value.startsWith("'") && value.endsWith("'")) {
                    return true;
                }
                // Check if it's a boolean
                if (value.equals("true") || value.equals("false")) {
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Gets the current intermediate code
     */
    public Vector<String> getIntermediateCode() {
        return new Vector<>(intermediateCode);
    }

    /**
     * Clears the intermediate code
     */
    public void clear() {
        intermediateCode.clear();
        labelCounter = 0;
    }
}
package javiergs.compiler;

import javiergs.vm.Symbol;
import javiergs.vm.Instruction;
import java.util.*;

/**
 * Programmatic Interpreter for executing intermediate code without UI
 * Sends output to CompilerUI's Screen and Console areas
 *
 * @author javiergs
 * @author eduardomv
 * @author santiarr
 * @author yawham
 * @version 1.0
 */
public class ProgrammaticInterpreter {

    private final Hashtable<String, Vector<Symbol>> symbolTable;
    private final Vector<Instruction> instructions;
    private final Stack<Symbol> register_zero;
    private final CompilerUI compilerUI;
    private boolean exit = false;
    private int pc;

    public ProgrammaticInterpreter(CompilerUI compilerUI) {
        this.compilerUI = compilerUI;
        symbolTable = new Hashtable<>();
        instructions = new Vector<>();
        register_zero = new Stack<>();
    }

    public void executeCode(String text) {
        // Clear previous state
        symbolTable.clear();
        instructions.clear();
        register_zero.clear();
        exit = false;
        pc = 0;

        // Parse and execute
        parseCode(text);
        executeInstructions();
    }

    private void parseCode(String text) {
        StringTokenizer st = new StringTokenizer(text, "\n");
        compilerUI.writeConsoleArea("Loading program...");

        // Read symbol table
        String line = st.nextToken();
        while (line != null && !line.trim().equals("@")) {
            if (line.charAt(0) == '#') {
                compilerUI.writeConsoleArea("Loading label: " + line);
                insertSymbolTableLabel(line);
            } else {
                compilerUI.writeConsoleArea("Loading variable: " + line);
                insertSymbolTableVar(line);
            }
            line = st.nextToken();
        }

        // Read instructions
        compilerUI.writeConsoleArea("Loading instructions...");
        while (st.hasMoreElements()) {
            line = st.nextToken();
            if (!line.trim().equals("")) {
                insertInstruction(line);
            }
        }

        compilerUI.writeConsoleArea("Program loaded successfully.");
        compilerUI.writeConsoleArea("Starting execution...");
    }

    private void executeInstructions() {
        while (!exit && pc < instructions.size()) {
            try {
                executionLoop();
                if (!exit) {
                    pc++;
                }
            } catch (Exception e) {
                compilerUI.writeConsoleArea("Runtime error: " + e.getMessage());
                break;
            }
        }

        if (exit) {
            compilerUI.writeConsoleArea("Program terminated normally.");
        } else {
            compilerUI.writeConsoleArea("Program reached end of instructions.");
        }
    }

    private void executionLoop() {
        String cmd = instructions.get(pc).getName().trim();
        String p1 = instructions.get(pc).getParameter1().trim();
        String p2 = instructions.get(pc).getParameter2().trim();

        compilerUI.writeConsoleArea("Executing: " + cmd + " " + p1 + ", " + p2);

        if (cmd.toUpperCase().equals("LIT")) {
            // Load literal
            if (p1.equals("true")) {
                register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
            } else if (p1.equals("false")) {
                register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
            } else if (p1.matches("\\d+")) {
                register_zero.push(newSymbolForTypeAndValue("int", p1));
            } else {
                try {
                    Float.parseFloat(p1);
                    register_zero.push(newSymbolForTypeAndValue("float", p1));
                } catch (Exception ef) {
                    register_zero.push(newSymbolForTypeAndValue("string", p1));
                }
            }
        } else if (cmd.toUpperCase().equals("LOD")) {
            // Load variable
            Symbol value = symbolTable.get(p1).get(0);
            register_zero.push(value);
        } else if (cmd.toUpperCase().equals("STO")) {
            // Store variable
            Symbol value = register_zero.pop();
            Symbol s = symbolTable.get(p1).get(0);
            s.setValue(value.getValue());
        } else if (cmd.toUpperCase().equals("JMP")) {
            // Unconditional jump
            if (p1.matches("\\d+")) {
                pc = Integer.parseInt(p1) - 2;
            } else {
                String v1 = symbolTable.get(p1).get(0).getValue().toString();
                pc = Integer.parseInt(v1) - 2;
            }
        } else if (cmd.toUpperCase().equals("JMC")) {
            // Conditional jump
            String value = register_zero.pop().getValue().toString().trim();

            if (value.equals(p2)) {
                if (p1.matches("\\d+")) {
                    pc = Integer.parseInt(p1) - 2;
                } else {
                    String v1 = symbolTable.get(p1).get(0).getValue().toString();
                    pc = Integer.parseInt(v1) - 2;
                }
            }
        } else if (cmd.toUpperCase().equals("OPR")) {
            executeOperation(Integer.parseInt(p1));
        }
    }

    private void executeOperation(int operation) {
        Symbol value1, value2, result;

        switch (operation) {
            case 0: // Exit
                exit = true;
                break;
            case 1: // Return
                break;
            case 2: // Addition
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                result = calculate(value1.getValue(), value2.getValue(), "+");
                register_zero.push(result);
                break;
            case 3: // Subtraction
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                result = calculate(value1.getValue(), value2.getValue(), "-");
                register_zero.push(result);
                break;
            case 4: // Multiplication
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                result = calculate(value1.getValue(), value2.getValue(), "*");
                register_zero.push(result);
                break;
            case 5: // Division
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                result = calculate(value1.getValue(), value2.getValue(), "/");
                register_zero.push(result);
                break;
            case 8: // OR
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                Boolean v1or = Boolean.parseBoolean(value1.getValue().toString());
                Boolean v2or = Boolean.parseBoolean(value2.getValue().toString());
                if (v1or || v2or) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 9: // AND
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                Boolean v1and = Boolean.parseBoolean(value1.getValue().toString());
                Boolean v2and = Boolean.parseBoolean(value2.getValue().toString());
                if (v1and && v2and) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 10: // NOT
                value1 = register_zero.pop();
                Boolean v1not = Boolean.parseBoolean(value1.getValue().toString());
                if (!v1not) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 11: // Greater than
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                Float v1gt = Float.parseFloat(value1.getValue().toString());
                Float v2gt = Float.parseFloat(value2.getValue().toString());
                if (v1gt > v2gt) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 12: // Less than
                value2 = register_zero.pop();
                value1 = register_zero.pop();
                Float v1lt = Float.parseFloat(value1.getValue().toString());
                Float v2lt = Float.parseFloat(value2.getValue().toString());
                if (v1lt < v2lt) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 15: // Equal
                String s2eq = register_zero.pop().getValue().toString();
                String s1eq = register_zero.pop().getValue().toString();
                if (s1eq.equals(s2eq)) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 16: // Not equal
                String s2ne = register_zero.pop().getValue().toString();
                String s1ne = register_zero.pop().getValue().toString();
                if (!s1ne.equals(s2ne)) {
                    register_zero.push(newSymbolForTypeAndValue("int", "1"));
                } else {
                    register_zero.push(newSymbolForTypeAndValue("int", "0"));
                }
                break;
            case 20: // Print without newline
                compilerUI.writeScreenArea(register_zero.pop().getValue().toString());
                break;
            case 21: // Print with newline
                compilerUI.writeScreenArea(register_zero.pop().getValue().toString() + "\n");
                break;

            case 22: // inputln - leer input del usuario
                String inputValue = compilerUI.readInputLine();

                // Determinar automáticamente el tipo del input
                try {
                    // Intentar convertir a entero
                    int intValue = Integer.parseInt(inputValue);
                    register_zero.push(newSymbolForTypeAndValue("int", inputValue));
                    compilerUI.writeConsoleArea("INPUT processed as integer: " + intValue);
                } catch (NumberFormatException e1) {
                    try {
                        // Intentar convertir a float
                        float floatValue = Float.parseFloat(inputValue);
                        register_zero.push(newSymbolForTypeAndValue("float", inputValue));
                        compilerUI.writeConsoleArea("INPUT processed as float: " + floatValue);
                    } catch (NumberFormatException e2) {
                        // Si no es número, tratarlo como string
                        register_zero.push(newSymbolForTypeAndValue("string", inputValue));
                        compilerUI.writeConsoleArea("INPUT processed as string: " + inputValue);
                    }
                }
                break;
            default:
                compilerUI.writeConsoleArea("Unknown operation: " + operation);
        }
    }

    // Helper methods (similar to Interpreter.java)
    private Symbol newSymbolForType(String type) {
        switch (type) {
            case "int":
                return new Symbol(type, "global", new Integer(0));
            case "float":
                return new Symbol(type, "global", new Float(0));
            case "boolean":
                return new Symbol(type, "global", new Boolean("false"));
            default:
                return new Symbol(type, "global", "");
        }
    }

    private Symbol newSymbolForTypeAndValue(String type, String value) {
        switch (type) {
            case "int":
                return new Symbol(type, "global", new Integer(value));
            case "float":
                return new Symbol(type, "global", new Float(value));
            case "boolean":
                return new Symbol(type, "global", new Boolean(value));
            case "char":
                return new Symbol(type, "global", "" + value);
            case "string":
                return new Symbol(type, "global", "" + value);
            default:
                compilerUI.writeConsoleArea("ERROR: type <" + type + "> not recognized.");
                break;
        }
        return null;
    }

    private void insertSymbolTableVar(String line) {
        try {
            int firstComma = line.indexOf(",");
            int secondComma = line.indexOf(",", firstComma + 1);
            String symbolName = line.substring(0, firstComma).trim();
            String symbolType = line.substring(firstComma + 1, secondComma).trim();
            Vector<Symbol> value = new Vector<Symbol>();
            value.add(newSymbolForType(symbolType));
            symbolTable.put(symbolName, value);
        } catch (Exception e) {
            compilerUI.writeConsoleArea("ERROR: trying to insert a variable into the symbol table.\n" + e);
        }
    }

    private void insertSymbolTableLabel(String line) {
        try {
            int firstComma = line.indexOf(",");
            int secondComma = line.indexOf(",", firstComma + 1);
            int thirdComma = line.indexOf(",", secondComma + 1);

            String symbolName = line.substring(0, firstComma).trim();
            String symbolType = line.substring(firstComma + 1, secondComma).trim();
            String symbolScope = line.substring(secondComma + 1, thirdComma).trim();
            String symbolValue = line.substring(thirdComma + 1).trim();

            Vector<Symbol> item = new Vector<Symbol>();
            item.add(newSymbolForTypeAndValue("int", symbolValue));
            symbolTable.put(symbolName, item);
        } catch (Exception e) {
            compilerUI.writeConsoleArea("ERROR: trying to insert a label into the symbol table.\n" + e);
        }
    }

    private boolean insertInstruction(String line) {
        try {
            Instruction i = new Instruction();
            int pos;
            // instruction
            pos = line.indexOf(' ');
            if (pos == -1) {
                return false;
            }
            i.setName(line.substring(0, pos).trim());
            line = line.substring(pos + 1);
            // first parameter
            pos = line.indexOf(',');
            if (pos == -1) {
                return false;
            }
            String p1 = line.substring(0, pos).trim();
            i.setParameter1(p1);
            line = line.substring(pos + 1).trim();
            // second parameter
            i.setParameter2(line);
            instructions.add(i);
        } catch (Exception e) {
            compilerUI.writeConsoleArea("ERROR: trying to insert instruction.\n" + e);
        }
        return true;
    }

    private Symbol calculate(Object v1, Object v2, String operator) {
        if (operator.equals("+") || operator.equals("-") || operator.equals("*") ||
                operator.equals("/")) {
            if (v1.getClass() == String.class || v2.getClass() == String.class) {
                // only +
                return newSymbolForTypeAndValue("string", v1.toString() + v2.toString());
            } else if (v1.getClass() == Float.class || v2.getClass() == Float.class) {
                float f1 = Float.parseFloat(v1.toString());
                float f2 = Float.parseFloat(v2.toString());
                if (operator.equals("+")) return newSymbolForTypeAndValue("float", "" + (f1 + f2));
                else if (operator.equals("-")) return newSymbolForTypeAndValue("float", "" + (f1 - f2));
                else if (operator.equals("*")) return newSymbolForTypeAndValue("float", "" + (f1 * f2));
                else if (operator.equals("/")) return newSymbolForTypeAndValue("float", "" + (f1 / f2));
            } else {
                int f1 = Integer.parseInt(v1.toString());
                int f2 = Integer.parseInt(v2.toString());
                if (operator.equals("+")) return newSymbolForTypeAndValue("int", "" + (f1 + f2));
                else if (operator.equals("-")) return newSymbolForTypeAndValue("int", "" + (f1 - f2));
                else if (operator.equals("*")) return newSymbolForTypeAndValue("int", "" + (f1 * f2));
                else if (operator.equals("/")) return newSymbolForTypeAndValue("int", "" + (f1 / f2));
            }
        }
        return newSymbolForType("string");
    }
}
package javiergs.vm;

import java.io.IOException;
import java.util.*;

/**
 * Virtual Machine class used to execute the intermediate code.
 * It has a GUI to show the execution of the code including symbol table, instructions and register.
 *
 * @author javiergs
 * @version 1.0
 */
public class Interpreter {
	
	private final Hashtable<String, Vector<Symbol>> symbolTable;
	private final Vector<Instruction> instructions;
	private final Stack<Symbol> register_zero;
	private final InterpreterUI gui;
	private boolean exit = false;
	private int pc;
	
	public void init(String text) {
		symbolTable.clear();
		instructions.clear();
		register_zero.clear();
		exit = false;
		// loading
		StringTokenizer st = new StringTokenizer(text, "\n");
		gui.writeConsole("* Reading file from the editor panel...");
		// read symbol table
		String line = st.nextToken();
		while (line != null && !line.trim().equals("@")) {
			if (line.charAt(0) == '#') {
				gui.writeConsole("* Getting labels... ");
				insert_symbol_table_label(line);
			} else {
				gui.writeConsole("* Getting variables... ");
				insert_symbol_table_var(line);
			}
			line = st.nextToken();
		}
		// read instructions
		gui.writeConsole("* Loading instructions... ");
		while (st.hasMoreElements()) {
			line = st.nextToken();
			if (!line.trim().equals("")) {
				if (!insert_instruction(line)) {
					gui.writeConsole("- Error in instruction line... ");
				}
			}
		}
		gui.writeConsole("* Intermediate code loaded.");
		// show
		show();
		if (symbolTable.get("#pc") != null) {
			pc = Integer.parseInt(symbolTable.get("#pc").get(0).getValue().toString()) - 1;
		} else {
			pc = 0;
		}
		gui.writePC(pc);
	}
	
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
				gui.writeConsole("ERROR: type <" + type + "> not recognized.");
				break;
		}
		return null;
	}
	
	private void insert_symbol_table_var(String line) {
		try {
			int firstComma = line.indexOf(",");
			int secondComma = line.indexOf(",", firstComma + 1);
			String symbolName = line.substring(0, firstComma).trim();
			String symbolType = line.substring(firstComma + 1, secondComma).trim();
			Vector<Symbol> value = new Vector<Symbol>();
			value.add(newSymbolForType(symbolType));
			symbolTable.put(symbolName, value);
		} catch (Exception e) {
			gui.writeConsole("ERROR: trying to insert a variable into the symbol table.\n" + e);
		}
	}
	
	private void insert_symbol_table_label(String line) {
		try {
			int firstComma = line.indexOf(",");
			int secondComma = line.indexOf(",", firstComma + 1);
			String symbolName = line.substring(0, firstComma).trim();
			String symbolType = line.substring(firstComma + 1, secondComma).trim();
			String symbolValue = line.substring(secondComma + 1).trim();
			Vector<Symbol> item = new Vector<Symbol>();
			item.add(newSymbolForTypeAndValue("int", symbolValue));
			symbolTable.put(symbolName, item);
		} catch (Exception e) {
			gui.writeConsole("ERROR: trying to insert a label into the symbol table.\n" + e);
		}
	}
	
	private boolean insert_instruction(String line) {
		// BUG: si el primer parametro es string y tiene comas falla :(
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
			gui.writeConsole("ERROR: trying to insert a label into the symbol table.\n" + e);
		}
		return true;
	}
	
	private void show() {
		// symbol table
		Enumeration items = symbolTable.keys();
		while (items.hasMoreElements()) {
			String key = (String) items.nextElement();
			Symbol symbol = symbolTable.get(key).get(0);
			gui.writeRam(key, symbol.getType(), symbol.getValue().toString());
		}
		// instructions
		for (int i = 0; i < instructions.size(); i++) {
			String cmd = instructions.get(i).getName();
			String p1 = instructions.get(i).getParameter1();
			String p2 = instructions.get(i).getParameter2();
			gui.writeCode(String.format("%04d", i + 1), cmd, p1, p2);
		}
		
	}
	
	private void execution_loop() {
		try {
			String cmd = instructions.get(pc).getName().trim();
			String p1 = instructions.get(pc).getParameter1().trim();
			String p2 = instructions.get(pc).getParameter2().trim();
			
			if (cmd.toUpperCase().equals("LIT")) {
				// verificar type
				if (p1.equals("true"))
					register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
				else if (p1.equals("false"))
					register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
				else if (p1.matches("\\d+")) {
					int i = Integer.parseInt(p1);
					register_zero.push(newSymbolForTypeAndValue("int", p1));
				} else {
					try {
						float f = Float.parseFloat(p1);
						register_zero.push(newSymbolForTypeAndValue("float", p1));
					} catch (Exception ef) {
						register_zero.push(newSymbolForTypeAndValue("string", p1));
					}
				}
				gui.writeRegistry(pc + 1, p1);
			} else if (cmd.toUpperCase().equals("LOD")) {
				Symbol value = symbolTable.get(p1).get(0);
				register_zero.push(value);
				gui.writeRegistry(pc + 1, value.getValue().toString());
				
			} else if (cmd.toUpperCase().equals("STO")) {
				Symbol value = register_zero.pop();
				gui.deleteRegistry();
				Symbol s = symbolTable.get(p1).get(0);
				s.setValue(value.getValue());
				gui.updateRam(p1, value.getValue().toString());
				
			} else if (cmd.toUpperCase().equals("JMP")) {
				if (p1.matches("\\d+")) {
					pc = Integer.parseInt(p1) - 2;
				} else {
					//its a label
					if (!p1.trim().equals(" ")) {
						String v1 = symbolTable.get(p1).get(0).getValue().toString();
						pc = Integer.parseInt(v1) - 2;
					} else {
						gui.writeConsole("\tUndefined label: " + p1);
					}
				}
				
			} else if (cmd.toUpperCase().equals("JMC")) {
				
				String value = register_zero.pop().getValue().toString().trim();
				
				gui.deleteRegistry();
				String param = p2;
				//System.out.println(">> " + value + "::" +p2 +"-");
				if (value.equals(p2)) {
					//System.out.println("A");
					if (p1.matches("\\d+")) {
						System.out.println("B");
						pc = Integer.parseInt(p1) - 2;
					} else {
						System.out.println("C");
						//its a label
						if (!p1.trim().equals(" ")) {
							System.out.println("D");
							String v1 = symbolTable.get(p1).get(0).getValue().toString();
							pc = Integer.parseInt(v1) - 2;
						} else {
							gui.writeConsole("\tUndefined label: " + p1);
						}
					}
				}
				
			} else if (cmd.toUpperCase().equals("OPR")) {
				Symbol value1, value2, result;
				switch (Integer.parseInt(p1)) {
					case 0:
						exit = true;
						break;
					case 1:
						break;
					case 2:
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						result = calculate(value1.getValue(), value2.getValue(), "+");
						register_zero.push(result);
						gui.deleteRegistry();
						gui.deleteRegistry();
						gui.writeRegistry(pc + 1, "" + result.getValue());
						break;
					case 3:
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						result = calculate(value1.getValue(), value2.getValue(), "-");
						register_zero.push(result);
						gui.deleteRegistry();
						gui.deleteRegistry();
						gui.writeRegistry(pc + 1, "" + result.getValue());
						break;
					case 4:
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						result = calculate(value1.getValue(), value2.getValue(), "*");
						register_zero.push(result);
						gui.deleteRegistry();
						gui.deleteRegistry();
						gui.writeRegistry(pc + 1, "" + result.getValue());
						break;
					case 5:
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						result = calculate(value1.getValue(), value2.getValue(), "/");
						register_zero.push(result);
						gui.deleteRegistry();
						gui.deleteRegistry();
						gui.writeRegistry(pc + 1, "" + result.getValue());
						break;
					
					case 8: // OR
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						Boolean v1or = Boolean.parseBoolean(value1.getValue().toString());
						Boolean v2or = Boolean.parseBoolean(value2.getValue().toString());
						gui.deleteRegistry();
						gui.deleteRegistry();
						if (v1or || v2or) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc + 1, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc + 1, "false");
						}
						break;
					
					case 9: // AND
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						Boolean v1and = Boolean.parseBoolean(value1.getValue().toString());
						Boolean v2and = Boolean.parseBoolean(value2.getValue().toString());
						gui.deleteRegistry();
						gui.deleteRegistry();
						if (v1and && v2and) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc + 1, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc + 1, "false");
						}
						break;
					
					case 10: // NOT
						
						value1 = register_zero.pop();
						Boolean v1not = Boolean.parseBoolean(value1.getValue().toString());
						gui.deleteRegistry();
						if (!v1not) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc + 1, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc + 1, "false");
						}
						break;
					
					case 11: // >
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						Float v1 = Float.parseFloat(value1.getValue().toString());
						Float v2 = Float.parseFloat(value2.getValue().toString());
						gui.deleteRegistry();
						gui.deleteRegistry();
						if (v1 > v2) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc + 1, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc + 1, "false");
						}
						break;
					
					case 12:  // <
						value2 = register_zero.pop();
						value1 = register_zero.pop();
						Float v1a = Float.parseFloat(value1.getValue().toString());
						Float v2a = Float.parseFloat(value2.getValue().toString());
						gui.deleteRegistry();
						gui.deleteRegistry();
						if (v1a < v2a) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc + 1, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc + 1, "false");
						}
						break;
					
					case 15:
						String s2 = register_zero.pop().getValue().toString();
						String s1 = register_zero.pop().getValue().toString();
						gui.deleteRegistry();
						gui.deleteRegistry();
						if (s1.equals(s2)) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc, "false");
						}
						break;
					
					case 16:
						String s2a = register_zero.pop().getValue().toString();
						String s1a = register_zero.pop().getValue().toString();
						gui.deleteRegistry();
						gui.deleteRegistry();
						if (!s1a.equals(s2a)) {
							register_zero.push(newSymbolForTypeAndValue("boolean", "true"));
							gui.writeRegistry(pc, "true");
						} else {
							register_zero.push(newSymbolForTypeAndValue("boolean", "false"));
							gui.writeRegistry(pc, "false");
						}
						break;
					case 20:
						gui.writeScreen(" " + register_zero.pop().getValue());
						gui.deleteRegistry();
						break;
					case 21:
						gui.writeScreen(" " + register_zero.pop().getValue() + "\n");
						gui.deleteRegistry();
						break;
					default:
						gui.writeScreen("\tUndefined operator: " + p1);
				}
			}
			if (!exit == true && pc + 1 < instructions.size()) {
				pc++;
			} else {
				exit = true;
			}
		} catch (Exception e) {
			gui.writeConsole("ERROR:\n" + e);
		}
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
	
	public void go(String s) {
		if (s.equals("step")) {
			if (!exit) {
				execution_loop();
			}
			if (exit) {
				gui.stop();
			}
			gui.writePC(pc);
		} else if (s.equals("all")) {
			
			while (!exit) {
				execution_loop();
				if (exit) {
					gui.stop();
				}
				gui.writePC(pc);
			}
			
		}
	}
	
	public Interpreter() {
		symbolTable = new Hashtable<String, Vector<Symbol>>();
		instructions = new Vector<Instruction>();
		register_zero = new Stack<>();
		gui = new InterpreterUI("CSE340 - Virtual Machine", this);
		gui.setVisible(true);
	}
	
	public static void main(String[] args) throws IOException {
		Interpreter m = new Interpreter();
	}
	
}
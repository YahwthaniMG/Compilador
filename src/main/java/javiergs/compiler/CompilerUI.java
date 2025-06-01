package javiergs.compiler;

import javiergs.vm.Interpreter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * User Interface for the Compiler including five tabs: Lexer, Parser, Semantic Analyzer, Intermediate Code, and Screen & Console
 * Now integrated with the Virtual Machine for automatic execution
 *
 * @author javiergs
 * @version 1.3
 */
public class CompilerUI extends JFrame implements ActionListener {

	private JTextArea console, codeArea;
	private JTextArea editor;
	private JTable tokensTable;
	private JTable semanticTable;
	private JMenuItem menuOpen = new JMenuItem("Open ...");
	private JMenuItem menuCompiler = new JMenuItem("Compile & Run");
	private JTree tree;
	private JPanel treePanel = new JPanel(new GridLayout(1, 1));
	private JTextArea parseTreeArea;

	// Screen y Console para la pestaña adicional
	private JTextArea screenArea;
	private JTextArea consoleArea;

	private JTextArea inputArea;
	private String[] inputLines = null;
	private int currentInputLine = 0;

	// Interpreter programático (sin UI) para ejecución automática
	private ProgrammaticInterpreter progInterpreter;

	public CompilerUI() {
		createMenu();
		createGUI();
		progInterpreter = new ProgrammaticInterpreter(this);
	}

	public static void main(String[] args) {
		// look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
				 UnsupportedLookAndFeelException e) {
		}
		// run
		CompilerUI gui = new CompilerUI();
		gui.setTitle("Our Compiler");
		Dimension dim = gui.getToolkit().getScreenSize();
		gui.setSize(3 * dim.width / 4, 3 * dim.height / 4);
		gui.setLocation((dim.width - gui.getSize().width) / 2, (dim.height - gui.getSize().height) / 2);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setVisible(true);
	}

	public void writeCode(String msg) {
		codeArea.append(msg + "\n");
	}

	public void writeConsole(String msg) {
		console.append(msg + "\n");
		// También escribir en la nueva consola
		writeConsoleArea(msg);
	}

	// Método para escribir en la nueva área de consola
	public void writeConsoleArea(String msg) {
		if (consoleArea != null) {
			consoleArea.append(msg + "\n");
			consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
		}
	}

	// Método para escribir en la nueva área de pantalla
	public void writeScreenArea(String msg) {
		if (screenArea != null) {
			screenArea.append(msg);
			screenArea.setCaretPosition(screenArea.getDocument().getLength());
		}
	}

	// Método para limpiar la pantalla
	public void clearScreen() {
		if (screenArea != null) {
			screenArea.setText("");
		}
	}

	private void writeEditor(String msg) {
		editor.append(msg + "\n");
	}

	private void writeTokenTable(Vector<TheToken> tokens) {
		for (TheToken token : tokens) {
			int line = token.getLineNumber();
			String tokenType = token.getType();
			String tokenValue = token.getValue();
			((DefaultTableModel) tokensTable.getModel()).addRow(new Object[]{String.format("%04d", line), tokenType, tokenValue});
		}
	}

	public void writeSymbolTable(Hashtable<String, Vector<SymbolTableItem>> symbolTable) {
		if (symbolTable == null) return;

		Enumeration<String> items = symbolTable.keys();
		if (items != null) {
			while (items.hasMoreElements()) {
				String name = items.nextElement();
				String type = symbolTable.get(name).get(0).getType();
				String scope = symbolTable.get(name).get(0).getScope();
				String value = symbolTable.get(name).get(0).getValue();
				((DefaultTableModel) semanticTable.getModel()).addRow(new Object[]{name, type, scope, value});
			}
		}
	}

	public void writeParseTree(Vector<String> parseTree) {
		StringBuilder treeText = new StringBuilder();
		treeText.append("Parse Tree:\n");
		treeText.append("===========\n\n");
		for (String rule : parseTree) {
			treeText.append(rule).append("\n");
		}
		parseTreeArea.setText(treeText.toString());
	}

	private void writeIntermediateCode(Vector<String> code) {
		StringBuilder codeText = new StringBuilder();
		for (String line : code) {
			codeText.append(line).append("\n");
		}
		codeArea.setText(codeText.toString());

		// EJECUTAR AUTOMÁTICAMENTE EL CÓDIGO
		if (!codeText.toString().trim().isEmpty()) {
			writeConsole("Executing program...");
			writeConsoleArea("=== PROGRAM EXECUTION ===");

			// Ejecutar el código intermedio generado
			progInterpreter.executeCode(codeText.toString());

			writeConsole("Program execution completed.");
			writeConsoleArea("=== EXECUTION COMPLETED ===");
		}
	}

	private void clearTokenTable() {
		int ta = ((DefaultTableModel) tokensTable.getModel()).getRowCount();
		for (int i = 0; i < ta; i++)
			((DefaultTableModel) tokensTable.getModel()).removeRow(0);
	}

	private void clearSemanticTable() {
		int ta = ((DefaultTableModel) semanticTable.getModel()).getRowCount();
		for (int i = 0; i < ta; i++)
			((DefaultTableModel) semanticTable.getModel()).removeRow(0);
	}

	private void clearParseTree() {
		if (parseTreeArea != null) {
			parseTreeArea.setText("After compilation, the Parse Tree will be showed here");
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (menuOpen.equals(e.getSource())) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "text");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				console.setText("");
				codeArea.setText("");
				editor.setText("");
				clearTokenTable();
				clearSemanticTable();
				clearScreenAndConsole();
				try {
					loadFile(file.getAbsolutePath());
				} catch (IOException ex) {
					writeConsole(ex.toString());
				}
			}
		} else if (menuCompiler.equals(e.getSource())) {
			// Compile & Run
			clearTokenTable();
			clearSemanticTable();
			clearParseTree();
			clearScreenAndConsole();
			console.setText("");
			codeArea.setText("");

			// lexical analysis
			if (editor.getText().equals("")) {
				writeConsole("The file is empty");
				return;
			}

			try {
				// Cargar datos de input antes de ejecutar
				if (inputArea != null) {
					setInputData(inputArea.getText());
				}
				// Crear el lexer con el texto del editor
				TheLexer lex = new TheLexer(editor.getText());
				lex.run();
				Vector<TheToken> tokens = lex.getTokens();

				// show token in a table
				writeTokenTable(tokens);

				// counting errors
				int errors = 0;
				for (TheToken token : tokens) {
					if (token.getType().equals("ERROR")) {
						errors++;
					}
				}

				// show stats on console
				writeConsole(tokens.size() + " strings found in " + tokens.get(tokens.size() - 1).getLineNumber() + " lines,");
				writeConsole(errors + " strings do not match any rule");

				if (errors > 0) {
					writeConsole("Compilation failed due to lexical errors.");
					return;
				}

				// Análisis sintáctico
				TheParser parser = new TheParser(tokens);
				parser.run();

				//Mostrar parse tree
				writeParseTree(parser.getParseTreeLog());

				// Análisis semántico
				TheSemantic semantic = parser.getSemantic();
				if (semantic != null) {
					writeSymbolTable(semantic.getSymbolTable());

					// NO FALLAR POR ERRORES SEMÁNTICOS - solo mostrarlos
					if (semantic.hasErrors()) {
						writeConsole("Semantic warnings found:");
						for (String error : semantic.getSemanticErrors()) {
							writeConsole("WARNING: " + error);
						}
						// Continuar con la generación de código
					} else {
						writeConsole("No semantic errors found.");
					}
				}

				//Generación de código intermedio (SIEMPRE se ejecuta)
				Vector<String> intermediateCode = parser.getIntermediateCode();
				writeIntermediateCode(intermediateCode); // Esto ejecuta automáticamente
				writeConsole("Compilation and execution completed!");

			} catch (IOException ex) {
				writeConsole("Error during compilation: " + ex.getMessage());
			}
		}
	}

	private boolean loadFile(String file) throws IOException {
		String line;
		BufferedReader br = new BufferedReader(new FileReader(file));
		writeConsole("Reading " + file + "");
		line = br.readLine();
		while (line != null) {
			writeEditor(line);
			line = br.readLine();
		}
		writeConsole("File loaded.");
		br.close();
		return true;
	}

	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenu menuRun = new JMenu("Run");

		menuOpen.addActionListener(this);
		menuCompiler.addActionListener(this);

		menuFile.add(menuOpen);
		menuRun.add(menuCompiler);

		menuBar.add(menuFile);
		menuBar.add(menuRun);
		setJMenuBar(menuBar);
	}
	public void setInputData(String input) {
		if (input != null && !input.trim().isEmpty()) {
			inputLines = input.split("\n");
			currentInputLine = 0;
			writeConsoleArea("Input data loaded: " + inputLines.length + " lines");
		} else {
			inputLines = null;
			currentInputLine = 0;
			writeConsoleArea("No input data provided");
		}
	}

	public String readInputLine() {
		if (inputLines != null && currentInputLine < inputLines.length) {
			String line = inputLines[currentInputLine].trim();
			currentInputLine++;
			writeConsoleArea("INPUT READ: " + line);
			return line;
		}
		writeConsoleArea("INPUT READ: (no more input available, returning 0)");
		return "0"; // Valor por defecto
	}

	// Método para limpiar el input también
	private void clearScreenAndConsole() {
		if (screenArea != null) {
			screenArea.setText("");
		}
		if (consoleArea != null) {
			consoleArea.setText("");
		}
		// Resetear input
		currentInputLine = 0;
		inputLines = null;
	}


	private void createGUI() {
		TitledBorder panelTitle;
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		JPanel downPanel = new JPanel(new GridLayout(1, 1));
		JPanel tokenPanel = new JPanel(new GridLayout(1, 1));
		JPanel semanticPanel = new JPanel(new GridLayout(1, 1));
		JPanel screenPanel = new JPanel(new GridLayout(1, 1));
		JPanel consolePanel = new JPanel(new GridLayout(1, 1));
		JPanel codePanel = new JPanel(new GridLayout(1, 1));

		// Panel para Screen & Console
		JPanel screenConsolePanel = new JPanel(new GridLayout(3, 1));
		JPanel screenSubPanel = new JPanel(new GridLayout(1, 1));
		JPanel inputSubPanel = new JPanel(new GridLayout(1, 1));
		JPanel consoleSubPanel = new JPanel(new GridLayout(1, 1));

		// screen
		panelTitle = BorderFactory.createTitledBorder("Source Code");
		screenPanel.setBorder(panelTitle);
		editor = new JTextArea();
		editor.setEditable(true);
		JScrollPane scrollScreen = new JScrollPane(editor);
		screenPanel.add(scrollScreen);

		// Input area
		panelTitle = BorderFactory.createTitledBorder("Program Input (one value per line)");
		inputSubPanel.setBorder(panelTitle);
		inputArea = new JTextArea();
		inputArea.setEditable(true);
		inputArea.setBackground(Color.lightGray);
		inputArea.setForeground(Color.black);
		inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		inputArea.setText("5\n10\n15\n"); // Valores de ejemplo
		JScrollPane scrollInputArea = new JScrollPane(inputArea);
		inputSubPanel.add(scrollInputArea);

		// tokens
		panelTitle = BorderFactory.createTitledBorder("Lexical Analysis");
		tokenPanel.setBorder(panelTitle);
		DefaultTableModel modelRegistry = new DefaultTableModel();
		tokensTable = new JTable(modelRegistry);
		tokensTable.setShowGrid(true);
		tokensTable.setGridColor(Color.LIGHT_GRAY);
		tokensTable.setAutoCreateRowSorter(true);
		modelRegistry.addColumn("line");
		modelRegistry.addColumn("token");
		modelRegistry.addColumn("string or word");
		JScrollPane scrollRegistry = new JScrollPane(tokensTable);
		tokensTable.setFillsViewportHeight(true);
		tokenPanel.add(scrollRegistry);
		tokensTable.setEnabled(false);

		// console
		panelTitle = BorderFactory.createTitledBorder("Console");
		consolePanel.setBorder(panelTitle);
		console = new JTextArea();
		console.setEditable(false);
		console.setBackground(Color.black);
		console.setForeground(Color.white);
		JScrollPane scrollConsole = new JScrollPane(console);
		consolePanel.add(scrollConsole);

		// tree
		panelTitle = BorderFactory.createTitledBorder("Syntactical Analysis");
		treePanel.setBorder(panelTitle);
		parseTreeArea = new JTextArea();
		parseTreeArea.setEditable(false);
		parseTreeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane treeView = new JScrollPane(parseTreeArea);
		treePanel.add(treeView);

		// semantic
		panelTitle = BorderFactory.createTitledBorder("Symbol Table");
		semanticPanel.setBorder(panelTitle);
		DefaultTableModel modelSemantic = new DefaultTableModel();
		semanticTable = new JTable(modelSemantic);
		semanticTable.setShowGrid(true);
		semanticTable.setGridColor(Color.LIGHT_GRAY);
		semanticTable.setAutoCreateRowSorter(true);
		modelSemantic.addColumn("name");
		modelSemantic.addColumn("type");
		modelSemantic.addColumn("scope");
		modelSemantic.addColumn("value");
		JScrollPane scrollSemantic = new JScrollPane(semanticTable);
		semanticTable.setFillsViewportHeight(true);
		semanticPanel.add(scrollSemantic);
		semanticTable.setEnabled(false);

		// code
		panelTitle = BorderFactory.createTitledBorder("Intermediate Code");
		codePanel.setBorder(panelTitle);
		codeArea = new JTextArea();
		codeArea.setEditable(false);
		JScrollPane scrollCode = new JScrollPane(codeArea);
		codePanel.add(scrollCode);

		// Screen & Console combinados
		// Screen area
		panelTitle = BorderFactory.createTitledBorder("Program Output");
		screenSubPanel.setBorder(panelTitle);
		screenArea = new JTextArea();
		screenArea.setEditable(false);
		screenArea.setBackground(Color.black);
		screenArea.setForeground(Color.white);
		screenArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane scrollScreenArea = new JScrollPane(screenArea);
		screenSubPanel.add(scrollScreenArea);

		// Console area
		panelTitle = BorderFactory.createTitledBorder("Execution Log");
		consoleSubPanel.setBorder(panelTitle);
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setBackground(Color.darkGray);
		consoleArea.setForeground(Color.white);
		consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		JScrollPane scrollConsoleArea = new JScrollPane(consoleArea);
		consoleSubPanel.add(scrollConsoleArea);

		// Combinar screen y console
		screenConsolePanel.add(screenSubPanel);
		screenConsolePanel.add(inputSubPanel);
		screenConsolePanel.add(consoleSubPanel);

		// tabs
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Lexer", tokenPanel);
		tabbedPane.addTab("Parser", treePanel);
		tabbedPane.addTab("Semantic Analyzer", semanticPanel);
		tabbedPane.addTab("Intermediate Code", codePanel);
		tabbedPane.addTab("Execution", screenConsolePanel);
		tabbedPane.setSelectedIndex(4); // Seleccionar la pestaña de ejecución por defecto

		// main frame
		topPanel.add(screenPanel);
		topPanel.add(tabbedPane);
		downPanel.add(consolePanel);
		downPanel.setPreferredSize(new Dimension(getWidth(), getHeight() / 4));
		add(topPanel, BorderLayout.CENTER);
		add(downPanel, BorderLayout.SOUTH);

		// editor hotkey
		menuCompiler.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
	}


}
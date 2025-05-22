package javiergs.vm;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * User Interface for the Virtual Machine
 * It shows the code, the console, the screen, the registry and the RAM
 *
 * @author javiergs
 * @version 1.0
 */
public class InterpreterUI extends JFrame implements ActionListener {
	
	private final Interpreter vm;
	private JTextArea console;
	private JTextArea screen;
	public JTextArea editor;
	private JTable ram;
	private JTable code;
	private JTable registry;
	private JTextField pc = new JTextField("", 5);
	private JMenuItem menuOpen = new JMenuItem("Open ...");
	private JMenuItem menuRun = new JMenuItem("Load");
	private JButton playStep = new JButton("run one step");
	private JButton playAll = new JButton("run all");
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	public InterpreterUI(String title, Interpreter vm) {
		super(title);
		this.vm = vm;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
						 UnsupportedLookAndFeelException e) {
		}
		createMenu();
		createGUI();
		Dimension dim = getToolkit().getScreenSize();
		setSize(dim.width / 2, 3 * dim.height / 4);
		setLocation((dim.width - getSize().width) / 2, (dim.height - getSize().height) / 2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenu menuRun = new JMenu("Debug");
		menuOpen.addActionListener(this);
		this.menuRun.addActionListener(this);
		menuFile.add(menuOpen);
		menuRun.add(this.menuRun);
		menuBar.add(menuFile);
		menuBar.add(menuRun);
		setJMenuBar(menuBar);
	}
	
	private void createGUI() {
		TitledBorder panelTitle;
		JPanel rightPanel = new JPanel(new GridLayout(3, 1));
		JPanel leftPanel = new JPanel(new GridLayout(3, 1));
		JPanel ramPanel = new JPanel(new GridLayout(1, 1));
		JPanel codePanel = new JPanel(new GridLayout(1, 1));
		JPanel registryPanel = new JPanel(new GridLayout(1, 1));
		JPanel screenPanel = new JPanel(new GridLayout(1, 1));
		JPanel consolePanel = new JPanel(new GridLayout(1, 1));
		JPanel controlPanel = new JPanel(new BorderLayout());
		JPanel editorPanel = new JPanel(new GridLayout(1, 1));
		// editor
		editor = new JTextArea();
		editor.setText("x, int, global, 0\n" + "y, float, global, 0\n" + "z, boolean, global, 0\n" + "@\n" + "lit 1, 0\n" + "sto x, 0\n" + "lod x, 0\n" + "lit 2, 0\n" + "opr 2, 0\n" + "sto y, 0\n" + "lod y, 0\n" + "opr 21, 0\n" + "lit 10, 0\n" + "lit 20, 0\n" + "opr 15, 0\n" + "opr 21, 0\n" + "lit \"this is an example\", 0\n" + "opr 21, 0\n" + "opr 1, 0\n" + "opr 0, 0\n");
		//ram
		panelTitle = BorderFactory.createTitledBorder("Symbol Table");
		ramPanel.setBorder(panelTitle);
		DefaultTableModel modelRam = new DefaultTableModel();
		ram = new JTable(modelRam);
		ram.setShowGrid(true);
		ram.setGridColor(Color.LIGHT_GRAY);
		ram.setAutoCreateRowSorter(true);
		modelRam.addColumn("variable");
		modelRam.addColumn("type");
		modelRam.addColumn("value");
		DefaultRowSorter sorter = ((DefaultRowSorter) ram.getRowSorter());
		ArrayList list = new ArrayList();
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.sort();
		JScrollPane scrollRam = new JScrollPane(ram);
		ram.setFillsViewportHeight(true);
		ramPanel.add(scrollRam);
		ram.setEnabled(false);
		sorter.setSortable(0, false);
		sorter.setSortable(1, false);
		sorter.setSortable(2, false);
		//code
		panelTitle = BorderFactory.createTitledBorder("Code");
		codePanel.setBorder(panelTitle);
		DefaultTableModel modelCode = new DefaultTableModel();
		code = new JTable(modelCode);
		code.setShowGrid(true);
		code.setGridColor(Color.LIGHT_GRAY);
		code.setAutoCreateRowSorter(true);
		modelCode.addColumn("#");
		modelCode.addColumn("instruction");
		modelCode.addColumn("parameter 1");
		modelCode.addColumn("parameter 2");
		DefaultRowSorter sorterCode = ((DefaultRowSorter) code.getRowSorter());
		ArrayList listCode = new ArrayList();
		listCode.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorterCode.setSortKeys(listCode);
		sorterCode.sort();
		JScrollPane scrollCode = new JScrollPane(code);
		code.setFillsViewportHeight(true);
		codePanel.add(scrollCode);
		code.setEnabled(false);
		sorterCode.setSortable(0, false);
		sorterCode.setSortable(1, false);
		sorterCode.setSortable(2, false);
		sorterCode.setSortable(3, false);
		//screen
		panelTitle = BorderFactory.createTitledBorder("Screen");
		screenPanel.setBorder(panelTitle);
		//screenPanel.setBackground(Color.black);
		screen = new JTextArea();
		screen.setBackground(Color.black);
		screen.setForeground(Color.white);
		screen.setEditable(false);
		JScrollPane scrollScreen = new JScrollPane(screen);
		screenPanel.add(scrollScreen);
		//registry
		panelTitle = BorderFactory.createTitledBorder("CPU registry");
		registryPanel.setBorder(panelTitle);
		DefaultTableModel modelRegistry = new DefaultTableModel();
		registry = new JTable(modelRegistry);
		registry.setShowGrid(true);
		registry.setGridColor(Color.LIGHT_GRAY);
		registry.setAutoCreateRowSorter(true);
		modelRegistry.addColumn("#");
		modelRegistry.addColumn("value");
		DefaultRowSorter sorterRegistry = ((DefaultRowSorter) registry.getRowSorter());
		ArrayList listRegistry = new ArrayList();
		listRegistry.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
		sorterRegistry.setSortKeys(listRegistry);
		sorterRegistry.sort();
		JScrollPane scrollRegistry = new JScrollPane(registry);
		registry.setFillsViewportHeight(true);
		registryPanel.add(scrollRegistry);
		registry.setEnabled(false);
		sorterRegistry.setSortable(0, false);
		sorterRegistry.setSortable(1, false);
		//console
		panelTitle = BorderFactory.createTitledBorder("Console");
		consolePanel.setBorder(panelTitle);
		console = new JTextArea();
		console.setEditable(false);
		JScrollPane scrollConsole = new JScrollPane(console);
		consolePanel.add(scrollConsole);
		// control
		panelTitle = BorderFactory.createTitledBorder("Control Panel");
		controlPanel.setBorder(panelTitle);
		pc.setEditable(false);
		playStep.addActionListener(this);
		playAll.addActionListener(this);
		JPanel aPanel = new JPanel(new GridLayout(1, 2));
		aPanel.add(new JLabel("Program Counter:"));
		aPanel.add(pc);
		JPanel bPanel = new JPanel(new GridLayout(3, 1));
		bPanel.add(aPanel);
		bPanel.add(playStep);
		bPanel.add(playAll);
		controlPanel.add(bPanel, BorderLayout.SOUTH);
		rightPanel.add(screenPanel);
		rightPanel.add(registryPanel);
		rightPanel.add(controlPanel);
		leftPanel.add(consolePanel);
		leftPanel.add(ramPanel);
		leftPanel.add(codePanel);
		// main
		JPanel vmPanel = new JPanel(new GridLayout(1, 2));
		vmPanel.add(leftPanel);
		vmPanel.add(rightPanel);
		// editor
		panelTitle = BorderFactory.createTitledBorder("Code");
		editorPanel.setBorder(panelTitle);
		JScrollPane scrollEditor = new JScrollPane(editor);
		editorPanel.add(scrollEditor);
		// tabs
		tabbedPane.addTab("Editor", editorPanel);
		tabbedPane.addTab("Dashboard", vmPanel);
		tabbedPane.setSelectedIndex(0);
		//main
		setLayout(new GridLayout(1, 1));
		add(tabbedPane);
	}
	
	private boolean loadFile(String file) throws FileNotFoundException, IOException {
		String line;
		BufferedReader br = new BufferedReader(new FileReader(file));
		writeConsole("Reading " + file);
		line = br.readLine();
		while (line != null) {
			writeEditor(line);
			line = br.readLine();
		}
		writeConsole("File loaded.");
		br.close();
		return true;
	}
	
	public void updateRam(String p1, String value) {
		for (int i = 0; i < ram.getModel().getRowCount(); i++) {
			if (p1.equals(ram.getModel().getValueAt(i, 0))) {
				ram.getModel().setValueAt(value, i, 2);
				int pos = ram.getRowSorter().convertRowIndexToView(i);
				ram.setRowSelectionInterval(pos, pos);
			}
		}
		
	}
	
	public void writePC(int msg) {
		pc.setText(String.format("%04d", msg + 1));
		code.setRowSelectionInterval(msg, msg);
	}
	
	public void writeConsole(String msg) {
		console.append(msg + "\n");
	}
	
	public void writeScreen(String msg) {
		screen.append(msg);
	}
	
	public void writeRam(String a, String b, String c) {
		((DefaultTableModel) ram.getModel()).addRow(new Object[]{a, b, c});
	}
	
	public void writeCode(String n, String a, String b, String c) {
		((DefaultTableModel) code.getModel()).addRow(new Object[]{n, a, b, c});
	}
	
	public void writeRegistry(int n, String a) {
		((DefaultTableModel) registry.getModel()).addRow(new Object[]{String.format("%04d", n), a});
	}
	
	private void writeEditor(String msg) {
		editor.append(msg + "\n");
	}
	
	public void deleteRegistry() {
		int size = ((DefaultTableModel) registry.getModel()).getRowCount();
		((DefaultTableModel) registry.getModel()).removeRow(size - 1);
	}
	
	public void stop() {
		playStep.setEnabled(false);
		playAll.setEnabled(false);
		code.getSelectionModel().clearSelection();
	}
	
	public void clearDashboard() {
		console.setText("");
		screen.setText("");
		playStep.setEnabled(true);
		playAll.setEnabled(true);
		int ta = registry.getModel().getRowCount();
		for (int i = 0; i < ta; i++) {
			((DefaultTableModel) registry.getModel()).removeRow(0);
		}
		int tb = ram.getModel().getRowCount();
		for (int i = 0; i < tb; i++) {
			((DefaultTableModel) ram.getModel()).removeRow(0);
		}
		int tc = code.getModel().getRowCount();
		for (int i = 0; i < tc; i++) {
			((DefaultTableModel) code.getModel()).removeRow(0);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		File file;
		if (menuOpen.equals(e.getSource())) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "text");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				clearDashboard();
				editor.setText("");
				// file
				try {
					loadFile(file.getAbsolutePath());
				} catch (IOException ex) {
					writeConsole(ex.toString());
				}
			}
		} else if (menuRun.equals(e.getSource())) {
			clearDashboard();
			vm.init(editor.getText());
			tabbedPane.setSelectedIndex(1);
		} else if (playStep.equals(e.getSource())) {
			vm.go("step");
		} else if (playAll.equals(e.getSource())) {
			vm.go("all");
		}
	}
	
}
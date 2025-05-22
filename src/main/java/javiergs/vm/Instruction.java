package javiergs.vm;

/**
 * Instruction class used to store the instruction name and its parameters
 *
 * @author javiergs
 * @version 1.0
 */
public class Instruction {
	
	private String name;
	private String parameter1;
	private String parameter2;
	
	public Instruction(String name, String parameter1, String parameter2) {
		this.name = name;
		this.parameter1 = parameter1;
		this.parameter2 = parameter2;
	}
	
	public Instruction() {
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getParameter1() {
		return parameter1;
	}
	
	public void setParameter1(String parameter1) {
		this.parameter1 = parameter1;
	}
	
	public String getParameter2() {
		return parameter2;
	}
	
	public void setParameter2(String parameter2) {
		this.parameter2 = parameter2;
	}
	
}
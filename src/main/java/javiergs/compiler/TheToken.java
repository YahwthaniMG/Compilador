package javiergs.compiler;
/**
 * Token.java
 * ---------------
 * Represents a lexical token with its value, type and line number in the source code.
 * This class is fundamental for the lexical analysis process, storing the essential
 * information about each token identified in the source code.
 *
 * @author javiergs
 * @author eduardomv
 * @author santiarr
 * @author yawham
 * @version 2.0
 */
public class TheToken {

	private String value;
	private String type;
	private int lineNumber;

	/**
	 * Constructs a new Token with the specified value, type, and line number.
	 *
	 * @param value      The string value of the token
	 * @param type       The type/category of the token
	 * @param lineNumber The line number where the token appears
	 */
	public TheToken(String value, String type, int lineNumber) {
		this.value = value;
		this.type = type;
		this.lineNumber = lineNumber;
	}

	public String getValue() {
		return value;
	}

	public String getType() {
		return type;
	}

	public int getLineNumber() {
		return lineNumber;
	}
}


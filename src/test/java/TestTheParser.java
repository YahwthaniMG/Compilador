import java.io.File;
import java.io.IOException;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

public class TestTheParser {
	
	private TheParser parser;
	private Vector<TheToken> tokens;
	
	@Test
	public void testParserInputThree() throws IOException {
		System.out.println("testing statements ==========================================================================");
		File file = new File("src/main/resources/inputMethods.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
	@Test
	public void testParserInputTwo() throws IOException {
		System.out.println("testing assignments =========================================================================");
		File file = new File("src/test/resources/ConteoPares.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
	@Test
	public void testParserInputOne() throws IOException {
		System.out.println("testing method declaration ==================================================================");
		File file = new File("src/test/resources/Suma.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
	@Test
	public void testParserInputZero() throws IOException {
		System.out.println("testing class declaration ===================================================================");
		File file = new File("src/test/resources/Temperatura.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
}
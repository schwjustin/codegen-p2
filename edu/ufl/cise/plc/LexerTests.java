package edu.ufl.cise.plc;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plc.IToken.Kind;


public class LexerTests {

	ILexer getLexer(String input){
		 return CompilerComponentFactory.getLexer(input);
	}
	
	static final boolean VERBOSE = true;
	void show(Object obj) {
		if(VERBOSE) {
			System.out.println(obj);
		}
	}
	
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}
		
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn){
		assertEquals(expectedKind, t.getKind());
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}
	
	void checkIdent(IToken t, String expectedName){
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, t.getText());
	}
	
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn){
		checkIdent(t,expectedName);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}
	
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.INT_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());	
	}
	
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t,expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());		
	}
	
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}
	
	
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		checkEOF(lexer.next());
	}
	
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+ 
				- 	 
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 0,0);
		checkToken(lexer.next(), Kind.MINUS, 1,0);
		checkEOF(lexer.next());
	}
	
	@Test
	void testComment0() throws LexicalException {
		String input = """
				"This is a string"
				#this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 0,0);
		checkToken(lexer.next(), Kind.TIMES, 2,0);
		checkEOF(lexer.next());
	}
	
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc");
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}
	
	@Test
	public void testIdent0() throws LexicalException {
		String input = """
				abc
				  def
				     ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 0,0);
		checkIdent(lexer.next(), "def", 1,2);
		checkIdent(lexer.next(), "ghi", 2,5);
		checkEOF(lexer.next());
	}
	
	
	@Test
	public void testEquals0() throws LexicalException {
		String input = """
				= == ===
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(),Kind.ASSIGN,0,0);
		checkToken(lexer.next(),Kind.EQUALS,0,2);
		checkToken(lexer.next(),Kind.EQUALS,0,5);
		checkToken(lexer.next(),Kind.ASSIGN,0,7);
		checkEOF(lexer.next());
	}



	
	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 0,0);
		checkInt(lexer.next(), 456, 0,5);
		checkIdent(lexer.next(), "b",0,8);
		checkEOF(lexer.next());
		}
	
	
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(),42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();			
		});
	}



		void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn, String expectedText){
		assertEquals(expectedKind, t.getKind());
		assertEquals(expectedText, t.getText());
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	void checkFloat(IToken t, float expectedValue) {
		assertEquals(Kind.FLOAT_LIT, t.getKind());
		assertEquals(expectedValue, t.getFloatValue());
	}

	void checkFloat(IToken t, float expectedValue, int expectedLine, int expectedColumn) {
		checkFloat(t,expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine,expectedColumn), t.getSourceLocation());
	}

	@Test
	void testIntFloatError() throws LexicalException {
		String input = """
			0.32
			00.15
			10.030.32
			""";
		show(input);
		ILexer lexer = getLexer(input);
		checkFloat(lexer.next(), (float) 0.32,	0, 0);
		checkInt(lexer.next(), 0, 			1, 0);
		checkFloat(lexer.next(), (float) 0.15,	1, 1);
		checkFloat(lexer.next(), (float) 10.030,	2, 0);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});

	}

	@Test
	public void testStringErrorEOF() throws LexicalException {
		String input = """
           \"good\"
           "test
   
            """;
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 0);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	void testErrorOnDotOperator() throws LexicalException {
		String input = "myObject.myProperty";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});

	}

	@Test
	void testWhatsThisOwO() throws LexicalException {
		String input = """
			OwO
			>.<
			Oh noes did I ...  thwow an ewwow
			X.x


			""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkToken(lexer.next(), Kind.GT, 1, 0);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	String getASCII(String s) {
		int[] ascii = new int[s.length()];
		for (int i = 0; i != s.length(); i++) {
			ascii[i] = s.charAt(i);
		}
		return Arrays.toString(ascii);
	}

	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "REDYELLOWGREEN";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkEOF(lexer.next());
	}

	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		show("input chars= " + getASCII(input));
		ILexer lexer = getLexer(input);

		IToken t = lexer.next();

		String val = t.getStringValue();
		show("getStringValueChars= 	" + getASCII(val));
		String expectedStringValue = " ...  \"  \'  \\  ";
		show("expectedStringValueChars=" + getASCII(expectedStringValue));
		assertEquals(expectedStringValue, val);
		String text = t.getText();
		show("getTextChars= 	" +getASCII(text));
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \"";
		show("expectedTextChars="+getASCII(expectedText));
		assertEquals(expectedText,text);
	}

	@Test
	public void testCommentEOF2() throws LexicalException {
		String input = "#";
		show(input);
		ILexer lexer = getLexer(input);
		checkEOF(lexer.next());
	}

	@Test
	void testSingleDigitNonzero() throws LexicalException
	{
		String input = "8";
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 8);
	}

	@Test
	void testPointerDereference() throws LexicalException
	{
		String input = "myObject->myProperty";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkToken(lexer.next(), Kind.RARROW, 0, 8);
		checkToken(lexer.next(), Kind.IDENT, 0, 10);
	}

	@Test
	void testManyIdentsWithWhitespace() throws LexicalException
	{
		String input = "Did you ever hear the tragedy of Darth Plagueis \"the wise\"?";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		checkToken(lexer.next(), Kind.IDENT, 0, 4);
		checkToken(lexer.next(), Kind.IDENT, 0, 8);
		checkToken(lexer.next(), Kind.IDENT, 0, 13);
		checkToken(lexer.next(), Kind.IDENT, 0, 18);
		checkToken(lexer.next(), Kind.IDENT, 0, 22);
		checkToken(lexer.next(), Kind.IDENT, 0, 30);
		checkToken(lexer.next(), Kind.IDENT, 0, 33);
		checkToken(lexer.next(), Kind.IDENT, 0, 39);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 48, "\"the wise\"");
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	void testIllegalIdent3() throws LexicalException
	{
		String input = "t\u01E2est";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.IDENT, 0, 0);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	public void testIllegalEscape3() throws LexicalException
	{
		String input = """
        "\\uFEFF"
        """;
		show(input);
		ILexer lexer = getLexer(input);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}




	@Test
	public void testStringErrorEscape() throws LexicalException {
		String input = """
                "good"
                "test \\n nesting \\h"
   
                """;
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 0, 0);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	public void testPeek() throws LexicalException {
		String input = """
			abc
			  def
			     ghi
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.peek(), "abc", 0,0);
		checkIdent(lexer.next(), "abc", 0,0);

		checkIdent(lexer.peek(), "def", 1,2);
		checkIdent(lexer.peek(), "def", 1,2);
		checkIdent(lexer.next(), "def", 1,2);

		checkIdent(lexer.peek(), "ghi", 2,5);
		checkIdent(lexer.peek(), "ghi", 2,5);
		checkIdent(lexer.peek(), "ghi", 2,5);
		checkIdent(lexer.next(), "ghi", 2,5);

		checkEOF(lexer.peek());
		checkEOF(lexer.next());
	}


}

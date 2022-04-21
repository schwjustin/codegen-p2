package edu.ufl.cise.plc;

public interface IToken {
	
    public record SourceLocation(int line, int column) {}  

	public static enum Kind {
		IDENT, 
		INT_LIT, 
		FLOAT_LIT, 
		STRING_LIT, 
		BOOLEAN_LIT,
		LPAREN,
		RPAREN,
		LSQUARE,
		RSQUARE,
		LANGLE,
		RANGLE,
		PLUS,
		MINUS,
		TIMES,
		DIV,
		MOD,
		COLOR_CONST,
					KW_IF,
		KW_FI,
		KW_ELSE,
		KW_WRITE,
		KW_CONSOLE,
		AND,
		OR,
		BANG,
		LT,
		GT,
		EQUALS,
		NOT_EQUALS,
		LE,
		GE,
		TYPE,
		COLOR_OP,
		IMAGE_OP,
		SEMI,
		COMMA,
		ASSIGN,
		RARROW,
		LARROW,
		KW_VOID,
		RETURN,
		EOF,
		ERROR,
	}
	
	public Kind getKind();

	public String getText();
	
	public SourceLocation getSourceLocation();

	public int getIntValue();

	public float getFloatValue();

	public boolean getBooleanValue();
	
	public String getStringValue();

	
}

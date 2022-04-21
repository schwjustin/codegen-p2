package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTVisitor;

public class CompilerComponentFactory {
	public static ILexer getLexer(String input) {
		return new Lexer(input);
	}

	public static IParser getParser(String input) {
		return new Parser(input);
	}
	
	public static TypeCheckVisitor getTypeChecker() {
		return new TypeCheckVisitor();
	}

	public static ASTVisitor getCodeGenerator(String packageName) {

		return new CodeGenVisitor(packageName);

	}
}

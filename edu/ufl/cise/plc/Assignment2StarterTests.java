package edu.ufl.cise.plc;

import static edu.ufl.cise.plc.IToken.Kind.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import edu.ufl.cise.plc.CompilerComponentFactory;
import edu.ufl.cise.plc.IParser;
import edu.ufl.cise.plc.LexicalException;
import edu.ufl.cise.plc.SyntaxException;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;

class Assignment2StarterTests {

	private ASTNode getAST(String input) throws Exception {
		IParser parser = CompilerComponentFactory.getParser(input);
		return parser.parse();
	}

	static final boolean VERBOSE = true;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	@DisplayName("test0")
	@Test
	public void test0(TestInfo testInfo) throws Exception {
		String input = """
				true
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BooleanLitExpr.class));
		assertTrue(((BooleanLitExpr) ast).getValue());
	}

	@DisplayName("test1")
	@Test
	public void test1(TestInfo testInfo) throws Exception {
		String input = """
				"this is a string"
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(StringLitExpr.class));
		assertEquals("this is a string", ((StringLitExpr) ast).getValue());
	}

	@DisplayName("test2")
	@Test
	public void test2(TestInfo testInfo) throws Exception {
		String input = """
				12.4
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(FloatLitExpr.class));
		assertEquals(12.4f, ((FloatLitExpr) ast).getValue());
	}

	@DisplayName("test3")
	@Test
	public void test3(TestInfo testInfo) throws Exception {
		String input = """
				var
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(IdentExpr.class));
		assertEquals("var", ast.getText());
	}

	@DisplayName("test4")
	@Test
	public void test4(TestInfo testInfo) throws Exception {
		String input = """
				!var
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(UnaryExpr.class));
		assertEquals(BANG, ((UnaryExpr) ast).getOp().getKind());
		Expr var0 = ((UnaryExpr) ast).getExpr();
		assertThat("", var0, instanceOf(IdentExpr.class));
		assertEquals("var", var0.getText());
	}

	@DisplayName("test5")
	@Test
	public void test5(TestInfo testInfo) throws Exception {
		String input = """
				-30
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(UnaryExpr.class));
		assertEquals(MINUS, ((UnaryExpr) ast).getOp().getKind());
		Expr var1 = ((UnaryExpr) ast).getExpr();
		assertThat("", var1, instanceOf(IntLitExpr.class));
		assertEquals(30, ((IntLitExpr) var1).getValue());
	}

	@DisplayName("test6")
	@Test
	public void test6(TestInfo testInfo) throws Exception {
		String input = """
				a + true
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(PLUS, ((BinaryExpr) ast).getOp().getKind());
		Expr var2 = ((BinaryExpr) ast).getLeft();
		assertThat("", var2, instanceOf(IdentExpr.class));
		assertEquals("a", var2.getText());
		Expr var3 = ((BinaryExpr) ast).getRight();
		assertThat("", var3, instanceOf(BooleanLitExpr.class));
		assertTrue(((BooleanLitExpr) var3).getValue());
	}

	@DisplayName("test7")
	@Test
	public void test7(TestInfo testInfo) throws Exception {
		String input = """
				b[a,200]
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(UnaryExprPostfix.class));
		Expr var4 = ((UnaryExprPostfix) ast).getExpr();
		assertThat("", var4, instanceOf(IdentExpr.class));
		assertEquals("b", var4.getText());
		PixelSelector var5 = ((UnaryExprPostfix) ast).getSelector();
		assertThat("", var5, instanceOf(PixelSelector.class));
		Expr var6 = ((PixelSelector) var5).getX();
		assertThat("", var6, instanceOf(IdentExpr.class));
		assertEquals("a", var6.getText());
		Expr var7 = ((PixelSelector) var5).getY();
		assertThat("", var7, instanceOf(IntLitExpr.class));
		assertEquals(200, ((IntLitExpr) var7).getValue());
	}

	@DisplayName("test8")
	@Test
	public void test8(TestInfo testInfo) throws Exception {
		String input = """
				a[x,y]*z
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(TIMES, ((BinaryExpr) ast).getOp().getKind());
		Expr var8 = ((BinaryExpr) ast).getLeft();
		assertThat("", var8, instanceOf(UnaryExprPostfix.class));
		Expr var9 = ((UnaryExprPostfix) var8).getExpr();
		assertThat("", var9, instanceOf(IdentExpr.class));
		assertEquals("a", var9.getText());
		PixelSelector var10 = ((UnaryExprPostfix) var8).getSelector();
		assertThat("", var10, instanceOf(PixelSelector.class));
		Expr var11 = ((PixelSelector) var10).getX();
		assertThat("", var11, instanceOf(IdentExpr.class));
		assertEquals("x", var11.getText());
		Expr var12 = ((PixelSelector) var10).getY();
		assertThat("", var12, instanceOf(IdentExpr.class));
		assertEquals("y", var12.getText());
		Expr var13 = ((BinaryExpr) ast).getRight();
		assertThat("", var13, instanceOf(IdentExpr.class));
		assertEquals("z", var13.getText());
	}

	@DisplayName("test9")
	@Test
	public void test9(TestInfo testInfo) throws Exception {
		String input = """
				if (a) b else c fi
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(ConditionalExpr.class));
		Expr var14 = ((ConditionalExpr) ast).getCondition();
		assertThat("", var14, instanceOf(IdentExpr.class));
		assertEquals("a", var14.getText());
		Expr var15 = ((ConditionalExpr) ast).getTrueCase();
		assertThat("", var15, instanceOf(IdentExpr.class));
		assertEquals("b", var15.getText());
		Expr var16 = ((ConditionalExpr) ast).getFalseCase();
		assertThat("", var16, instanceOf(IdentExpr.class));
		assertEquals("c", var16.getText());
	}

	@DisplayName("test10")
	@Test
	public void test10(TestInfo testInfo) throws Exception {
		String input = """
				if (a & b) if (x) y else z fi else c fi
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(ConditionalExpr.class));
		Expr var17 = ((ConditionalExpr) ast).getCondition();
		assertThat("", var17, instanceOf(BinaryExpr.class));
		assertEquals(AND, ((BinaryExpr) var17).getOp().getKind());
		Expr var18 = ((BinaryExpr) var17).getLeft();
		assertThat("", var18, instanceOf(IdentExpr.class));
		assertEquals("a", var18.getText());
		Expr var19 = ((BinaryExpr) var17).getRight();
		assertThat("", var19, instanceOf(IdentExpr.class));
		assertEquals("b", var19.getText());
		Expr var20 = ((ConditionalExpr) ast).getTrueCase();
		assertThat("", var20, instanceOf(ConditionalExpr.class));
		Expr var21 = ((ConditionalExpr) var20).getCondition();
		assertThat("", var21, instanceOf(IdentExpr.class));
		assertEquals("x", var21.getText());
		Expr var22 = ((ConditionalExpr) var20).getTrueCase();
		assertThat("", var22, instanceOf(IdentExpr.class));
		assertEquals("y", var22.getText());
		Expr var23 = ((ConditionalExpr) var20).getFalseCase();
		assertThat("", var23, instanceOf(IdentExpr.class));
		assertEquals("z", var23.getText());
		Expr var24 = ((ConditionalExpr) ast).getFalseCase();
		assertThat("", var24, instanceOf(IdentExpr.class));
		assertEquals("c", var24.getText());
	}

	@DisplayName("test11")
	@Test
	public void test11(TestInfo testInfo) throws Exception {
		String input = """
				getRed x
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(UnaryExpr.class));
		assertEquals(COLOR_OP, ((UnaryExpr) ast).getOp().getKind());
		Expr var25 = ((UnaryExpr) ast).getExpr();
		assertThat("", var25, instanceOf(IdentExpr.class));
		assertEquals("x", var25.getText());
	}

	@DisplayName("test12")
	@Test
	public void test12(TestInfo testInfo) throws Exception {
		String input = """
				getGreen getRed x
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(UnaryExpr.class));
		assertEquals(COLOR_OP, ((UnaryExpr) ast).getOp().getKind());
		Expr var26 = ((UnaryExpr) ast).getExpr();
		assertThat("", var26, instanceOf(UnaryExpr.class));
		assertEquals(COLOR_OP, ((UnaryExpr) var26).getOp().getKind());
		Expr var27 = ((UnaryExpr) var26).getExpr();
		assertThat("", var27, instanceOf(IdentExpr.class));
		assertEquals("x", var27.getText());
	}

	@DisplayName("test13")
	@Test
	public void test13(TestInfo testInfo) throws Exception {
		String input = """
						x + if
				""";
		show("-------------");
		show(input);
		Exception e = assertThrows(SyntaxException.class, () -> {
			getAST(input);
		});
		show(e);
	}

	@DisplayName("test14")
	@Test
	public void test14(TestInfo testInfo) throws Exception {
		String input = """
				x + @
				""";
		show("-------------");
		show(input);
		Exception e = assertThrows(LexicalException.class, () -> {
			getAST(input);
		});
		show(e);
	}

	@DisplayName("testEOF")
	@Test
	public void testEOF(TestInfo testinfo) throws Exception{
		String input = """
        x +
        """;
		show("-------------");
		show(input);
		Exception e =assertThrows(SyntaxException.class,() ->{
			getAST(input);
		});
		show(e);
	}


	@DisplayName("testPixelError")
	@Test
	public void testPixelError(TestInfo testinfo) throws Exception{
		String input = """
      a[,
      """;
		show("-------------");
		show(input);
		Exception e = assertThrows(SyntaxException.class,() ->{
			getAST(input);
		});
		show(e);
	}

	@DisplayName("testPEMDAS0")
	@Test
	public void testPEMDAS0(TestInfo testInfo) throws Exception {
		String input = """
				1 + 2 * 3 / 4 - 5
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(MINUS, ((BinaryExpr) ast).getOp().getKind());

		Expr addition = ((BinaryExpr) ast).getLeft();
		assertThat("", addition, instanceOf(BinaryExpr.class));
		assertEquals(PLUS, ((BinaryExpr) addition).getOp().getKind());
		Expr five = ((BinaryExpr) ast).getRight();
		assertThat("", five, instanceOf(IntLitExpr.class));
		assertEquals(5, ((IntLitExpr) five).getValue());

		Expr one = ((BinaryExpr) addition).getLeft();
		assertThat("", one, instanceOf(IntLitExpr.class));
		assertEquals(1, ((IntLitExpr) one).getValue());
		Expr division = ((BinaryExpr) addition).getRight();
		assertThat("", division, instanceOf(BinaryExpr.class));
		assertEquals(DIV, ((BinaryExpr) division).getOp().getKind());

		Expr multiplication = ((BinaryExpr) division).getLeft();
		assertThat("", multiplication, instanceOf(BinaryExpr.class));
		assertEquals(TIMES, ((BinaryExpr) multiplication).getOp().getKind());
		Expr four = ((BinaryExpr) division).getRight();
		assertThat("", four, instanceOf(IntLitExpr.class));
		assertEquals(4, ((IntLitExpr) four).getValue());

		Expr two = ((BinaryExpr) multiplication).getLeft();
		assertThat("", two, instanceOf(IntLitExpr.class));
		assertEquals(2, ((IntLitExpr) two).getValue());
		Expr three = ((BinaryExpr) multiplication).getRight();
		assertThat("", three, instanceOf(IntLitExpr.class));
		assertEquals(3, ((IntLitExpr) three).getValue());
	}

	@DisplayName("testPEMDAS1")
	@Test
	public void testPEMDAS1(TestInfo testInfo) throws Exception {
		String input = """
				3 * (4 + 5)
				""";
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(TIMES, ((BinaryExpr) ast).getOp().getKind());

		Expr three = ((BinaryExpr) ast).getLeft();
		assertThat("", three, instanceOf(IntLitExpr.class));
		assertEquals(3, ((IntLitExpr) three).getValue());
		Expr addition = ((BinaryExpr) ast).getRight();
		assertThat("", addition, instanceOf(BinaryExpr.class));
		assertEquals(PLUS, ((BinaryExpr) addition).getOp().getKind());

		Expr four = ((BinaryExpr) addition).getLeft();
		assertThat("", four, instanceOf(IntLitExpr.class));
		assertEquals(4, ((IntLitExpr) four).getValue());
		Expr five = ((BinaryExpr) addition).getRight();
		assertThat("", five, instanceOf(IntLitExpr.class));
		assertEquals(5, ((IntLitExpr) five).getValue());

	}

	@DisplayName("testPEMDAS2")
	@Test
	public void testPEMDAS2(TestInfo testInfo) throws Exception {
		String input = """
                1 | 2 & 3 & 4 | 5
                """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(OR, ((BinaryExpr) ast).getOp().getKind());

		Expr five = ((BinaryExpr) ast).getRight();
		assertThat("", five, instanceOf(IntLitExpr.class));
		assertEquals(5, ((IntLitExpr) five).getValue());

		Expr var = ((BinaryExpr) ast).getLeft();
		assertThat("", var, instanceOf(BinaryExpr.class));
		assertEquals(OR, ((BinaryExpr) var).getOp().getKind());

		Expr one = ((BinaryExpr) var).getLeft();
		assertThat("", one, instanceOf(IntLitExpr.class));
		assertEquals(1, ((IntLitExpr) one).getValue());

		Expr var2 = ((BinaryExpr) var).getRight();
		assertThat("", var2, instanceOf(BinaryExpr.class));
		assertEquals(AND, ((BinaryExpr) var2).getOp().getKind());

		Expr four = ((BinaryExpr) var2).getRight();
		assertThat("", four, instanceOf(IntLitExpr.class));
		assertEquals(4, ((IntLitExpr) four).getValue());

		Expr var3 = ((BinaryExpr) var2).getLeft();
		assertThat("", var3, instanceOf(BinaryExpr.class));
		assertEquals(AND, ((BinaryExpr) var3).getOp().getKind());

		Expr two = ((BinaryExpr) var3).getLeft();
		assertThat("", two, instanceOf(IntLitExpr.class));
		assertEquals(2, ((IntLitExpr) two).getValue());

		Expr three = ((BinaryExpr) var3).getLeft();
		assertThat("", three, instanceOf(IntLitExpr.class));
		assertEquals(2, ((IntLitExpr) three).getValue());
	}

	@DisplayName("testPEMDAS3")
	@Test
	public void testPEMDAS3(TestInfo testInfo) throws Exception {
		String input = """
                3 & (4 | 5)
                """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(AND, ((BinaryExpr) ast).getOp().getKind());

		Expr three = ((BinaryExpr) ast).getLeft();
		assertThat("", three, instanceOf(IntLitExpr.class));
		assertEquals(3, ((IntLitExpr) three).getValue());
		Expr or = ((BinaryExpr) ast).getRight();
		assertThat("", or, instanceOf(BinaryExpr.class));
		assertEquals(OR, ((BinaryExpr) or).getOp().getKind());

		Expr four = ((BinaryExpr) or).getLeft();
		assertThat("", four, instanceOf(IntLitExpr.class));
		assertEquals(4, ((IntLitExpr) four).getValue());
		Expr five = ((BinaryExpr) or).getRight();
		assertThat("", five, instanceOf(IntLitExpr.class));
		assertEquals(5, ((IntLitExpr) five).getValue());
	}

	@DisplayName("triple_if")
	@Test
	public void triple_if(TestInfo testInfo) throws Exception {
		String input = """
        if (a < b)
           if (l > s)
              if (le == us)
                 v
              else
                 o
              fi
           else
              z
           fi
        else
           c
        fi
        """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(ConditionalExpr.class));

		Expr var17 = ((ConditionalExpr) ast).getCondition();
		assertThat("", var17, instanceOf(BinaryExpr.class));
		assertEquals(LT, ((BinaryExpr) var17).getOp().getKind());

		Expr var18 = ((BinaryExpr) var17).getLeft();
		assertThat("", var18, instanceOf(IdentExpr.class));
		assertEquals("a", var18.getText());

		Expr var19 = ((BinaryExpr) var17).getRight();
		assertThat("", var19, instanceOf(IdentExpr.class));
		assertEquals("b", var19.getText());

		Expr var20 = ((ConditionalExpr) ast).getTrueCase();
		assertThat("", var20, instanceOf(ConditionalExpr.class));

		Expr var25 = ((ConditionalExpr) var20).getCondition();
		assertThat("", var25, instanceOf(BinaryExpr.class));
		assertEquals(GT, ((BinaryExpr) var25).getOp().getKind());

		Expr var26 = ((BinaryExpr) var25).getLeft();
		assertThat("", var26, instanceOf(IdentExpr.class));
		assertEquals("l", var26.getText());

		Expr var27 = ((BinaryExpr) var25).getRight();
		assertThat("", var27, instanceOf(IdentExpr.class));
		assertEquals("s", var27.getText());

		Expr var28 = ((ConditionalExpr) var20).getTrueCase();
		assertThat("", var28, instanceOf(ConditionalExpr.class));

		Expr var29 = ((ConditionalExpr) var28).getCondition();
		assertThat("", var29, instanceOf(BinaryExpr.class));

		assertEquals(EQUALS, ((BinaryExpr) var29).getOp().getKind());

		Expr var30 = ((BinaryExpr) var29).getLeft();
		assertThat("", var30, instanceOf(IdentExpr.class));
		assertEquals("le", var30.getText());

		Expr var31 = ((BinaryExpr) var29).getRight();
		assertThat("", var31, instanceOf(IdentExpr.class));
		assertEquals("us", var31.getText());

		Expr var22 = ((ConditionalExpr) var20).getTrueCase();
		assertThat("", var22, instanceOf(ConditionalExpr.class));

		Expr var23 = ((ConditionalExpr) var20).getFalseCase();
		assertThat("", var23, instanceOf(IdentExpr.class));
		assertEquals("z", var23.getText());



		Expr var24 = ((ConditionalExpr) ast).getFalseCase();
		assertThat("", var24, instanceOf(IdentExpr.class));
		assertEquals("c", var24.getText());
	}

	@DisplayName("Test parentheses")
	@Test
	public void testParentheses(TestInfo testInfo) throws Exception {
		String input = """
        a + ((b + c) * 4) + e
        """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);

		assertThat("", ast, instanceOf(BinaryExpr.class));
		assertEquals(PLUS, ((BinaryExpr) ast).getOp().getKind());

		Expr addition = ((BinaryExpr) ast).getLeft();
		assertThat("", addition, instanceOf(BinaryExpr.class));
		assertEquals(PLUS, ((BinaryExpr) addition).getOp().getKind());

		Expr identE = ((BinaryExpr) ast).getRight();
		assertThat("", identE, instanceOf(IdentExpr.class));
		assertEquals("e", identE.getText());

		Expr identA = ((BinaryExpr) addition).getLeft();
		assertThat("", identA, instanceOf(IdentExpr.class));
		assertEquals("a", identA.getText());

		Expr multExpr = ((BinaryExpr) addition).getRight();
		assertThat("", multExpr, instanceOf(BinaryExpr.class));
		assertEquals(TIMES, ((BinaryExpr) multExpr).getOp().getKind());

		Expr bPlusC = ((BinaryExpr) multExpr).getLeft();
		assertThat("", bPlusC, instanceOf(BinaryExpr.class));
		assertEquals(PLUS, ((BinaryExpr) bPlusC).getOp().getKind());

		Expr identB = ((BinaryExpr) bPlusC).getLeft();
		assertThat("", identB, instanceOf(IdentExpr.class));
		assertEquals("b", identB.getText());

		Expr identC = ((BinaryExpr) bPlusC).getRight();
		assertThat("", identC, instanceOf(IdentExpr.class));
		assertEquals("c", identC.getText());

		Expr int_lit4 = ((BinaryExpr) multExpr).getRight();
		assertThat("", int_lit4, instanceOf(IntLitExpr.class));
		assertEquals(4,  ((IntLitExpr) int_lit4).getValue());
	}

	@DisplayName("bangBool")
	@Test
	public void bangBool(TestInfo testInfo) throws Exception {
		String input = """
                !true
                """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(UnaryExpr.class));
		assertEquals(BANG, ((UnaryExpr) ast).getOp().getKind());
		Expr var1 = ((UnaryExpr) ast).getExpr();
		assertThat("", var1, instanceOf(BooleanLitExpr.class));
		assertEquals(true, ((BooleanLitExpr) var1).getValue());
	}

	@DisplayName("nested_if")
	@Test
	public void nested_if(TestInfo testInfo) throws Exception{
		String input = """
                if(if(z) a else b fi)
                    x
                else
                    y fi
                """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(ConditionalExpr.class));

		Expr var = ((ConditionalExpr) ast).getCondition();
		assertThat("", var, instanceOf(ConditionalExpr.class));

		Expr var2 = ((ConditionalExpr) var).getCondition();
		assertThat("", var2, instanceOf(IdentExpr.class));
		assertEquals("z", var2.getText());

		Expr var3 = ((ConditionalExpr) var).getTrueCase();
		assertThat("", var3, instanceOf(IdentExpr.class));
		assertEquals("a", var3.getText());

		Expr var4 = ((ConditionalExpr) var).getFalseCase();
		assertThat("", var4, instanceOf(IdentExpr.class));
		assertEquals("b", var4.getText());

		Expr var5 = ((ConditionalExpr) ast).getTrueCase();
		assertThat("", var5, instanceOf(IdentExpr.class));
		assertEquals("x", var5.getText());

		Expr var6 = ((ConditionalExpr) ast).getFalseCase();
		assertThat("", var6, instanceOf(IdentExpr.class));
		assertEquals("y", var6.getText());
	}

	@DisplayName("empty_paren")
	@Test
	public void empty_paren(TestInfo testInfo) throws Exception{
		String input = """
                ()
                """;
		show("-------------");
		show(input);
		Exception e = assertThrows(SyntaxException.class, () -> {
			getAST(input);
		});
		show(e);
	}

	@DisplayName("simple_paren")
	@Test
	public void simple_paren(TestInfo testInfo) throws Exception{
		String input = """
                ("hello")
                """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(StringLitExpr.class));
		assertEquals("hello", ((StringLitExpr) ast).getValue());
	}

	@DisplayName("extra_paren")
	@Test
	public void extra_paren(TestInfo testInfo) throws Exception{
		String input = """
                (("world"))
                """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		show(ast);
		assertThat("", ast, instanceOf(StringLitExpr.class));
		assertEquals("world", ((StringLitExpr) ast).getValue());
	}


	@DisplayName("test_eof")
	@Test
	public void test_eof(TestInfo testInfo) throws Exception{
		String input = """
              1 1
              """;
		show("-------------");
		show(input);
		Expr ast = (Expr) getAST(input);
		assertThat("", ast, instanceOf(IntLitExpr.class));
	}












@DisplayName("complex_unary")
@Test
public void complex_unary(TestInfo testInfo) throws Exception{
String input = """
		getWidth true[3,getWidth true]  < 9 | x != e & i
		""";
//[(8 < 9)] | [(x != e) & i]
show("-------------");
show(input);
Expr ast = (Expr) getAST(input);
show(ast);
assertThat("", ast, instanceOf(BinaryExpr.class));
assertEquals(OR, ((BinaryExpr) ast).getOp().getKind());

Expr left1 = ((BinaryExpr) ast).getLeft();
assertEquals(LT, ((BinaryExpr) left1).getOp().getKind());

Expr right1 = ((BinaryExpr) ast).getRight();
assertEquals(AND, ((BinaryExpr) right1).getOp().getKind());

Expr final_ = ((BinaryExpr) right1).getLeft();
assertEquals(NOT_EQUALS, ((BinaryExpr) final_).getOp().getKind());

}





}

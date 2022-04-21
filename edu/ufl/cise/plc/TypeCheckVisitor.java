package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;

import java.util.List;
import java.util.Map;


import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}
	
	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(BOOLEAN);
		return BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		stringLitExpr.setType(STRING);
		return STRING;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		intLitExpr.setType(INT);
		return INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(FLOAT);
		return FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		//TODO:  implement this method
		colorConstExpr.setType(COLOR);
		return COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(CONSOLE);
		return CONSOLE;
	}

	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == INT || redType == FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == INT) ? COLOR : COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}	

	
	
	//Maps forms a lookup table that maps an operator expression pair into result   
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);
	
	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression  
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result 
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}


	private Type plusMinus(Type leftType, Type rightType, BinaryExpr binaryExpr) {
		Type resultType = null;

		if (leftType == INT && rightType == INT) resultType = INT;
		else if (leftType == FLOAT && rightType == FLOAT) resultType = FLOAT;
		else if (leftType == COLOR && rightType == COLOR) resultType = COLOR;
		else if (leftType == COLORFLOAT && rightType == COLORFLOAT) resultType = COLORFLOAT;
		else if (leftType == IMAGE && rightType == IMAGE) resultType = IMAGE;
		else if (leftType == INT && rightType == FLOAT) {
			binaryExpr.getLeft().setCoerceTo(FLOAT);
			resultType = FLOAT;
		}
		else if (leftType == FLOAT && rightType == INT) {
			binaryExpr.getRight().setCoerceTo(FLOAT);
			resultType = FLOAT;
		}
		else if (leftType == COLORFLOAT && rightType == COLOR) {
			binaryExpr.getRight().setCoerceTo(COLORFLOAT);
			resultType = COLORFLOAT;
		}
		else if (leftType == COLOR && rightType == COLORFLOAT) {
			binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
			resultType = COLORFLOAT;
		}

		return resultType;
	}
	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;

		switch(op) {
			case AND, OR -> {
				if (leftType == BOOLEAN && rightType == BOOLEAN) resultType = BOOLEAN;
				else check(false, binaryExpr, "incompatible types for operator");
			}
			case EQUALS, NOT_EQUALS -> {
				check(leftType == rightType, binaryExpr, "incompatible types for comparison");
				resultType = BOOLEAN;
			}
			case PLUS, MINUS -> {
				resultType = plusMinus(leftType, rightType, binaryExpr);
				if (resultType == null) check(false, binaryExpr, "incompatible types for operator");
			}
			case TIMES, DIV, MOD -> {
				resultType = plusMinus(leftType, rightType, binaryExpr);
				if (resultType == null) {
					if (leftType == IMAGE && rightType == INT) resultType = IMAGE;
					else if (leftType == IMAGE && rightType == FLOAT) resultType = IMAGE;
					else if (leftType == INT && rightType == COLOR) {
						binaryExpr.getLeft().setCoerceTo(COLOR);
						resultType = COLOR;
					} else if (leftType == COLOR && rightType == INT) {
						binaryExpr.getRight().setCoerceTo(COLOR);
						resultType = COLOR;
					} else if (leftType == FLOAT && rightType == COLOR) {
						binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
						binaryExpr.getRight().setCoerceTo(COLORFLOAT);
						resultType = COLORFLOAT;
					} else if (leftType == COLOR && rightType == FLOAT) {
						binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
						binaryExpr.getRight().setCoerceTo(COLORFLOAT);
						resultType = COLORFLOAT;
					} else check(false, binaryExpr, "incompatible types for operator");
				}
			}
			case LT, LE, GT, GE -> {
				if (leftType == INT && rightType == INT) resultType = BOOLEAN;
				else if (leftType == FLOAT && rightType == FLOAT) resultType = BOOLEAN;
				else if (leftType == INT && rightType == FLOAT) {
					binaryExpr.getLeft().setCoerceTo(FLOAT);
					resultType = BOOLEAN;
				}
				else if (leftType == FLOAT && rightType == INT) {
					binaryExpr.getRight().setCoerceTo(FLOAT);
					resultType = BOOLEAN;
				}
				else check(false, binaryExpr, "incompatible types for operator");
			}
			default -> {
				throw new Exception("compiler error");
			}
		}
		binaryExpr.setType(resultType);
		return resultType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		//TODO:  implement this method
		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name);
		check(dec != null, identExpr, "undefined identifier " + name);
		check(dec.isInitialized(), identExpr, "using uninitialized variable");
		identExpr.setDec(dec);  //save declaration--will be useful later.
		Type type = dec.getType();
		identExpr.setType(type);
		return type;
	}


	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
		Type conditionType = (Type) conditionalExpr.getCondition().visit(this, arg);
		check(conditionType == BOOLEAN, conditionalExpr, "the condition of a conditional " +
				"expression must be a boolean");
		Type trueCaseType =  (Type) conditionalExpr.getTrueCase().visit(this, arg);
		Type falseCaseType = (Type) conditionalExpr.getFalseCase().visit(this,arg);
		check(trueCaseType == falseCaseType, conditionalExpr, "true case and false case must have same type in a " +
				"conditional expression");
		conditionalExpr.setType(trueCaseType);

		return trueCaseType;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		//TODO  implement this method
		Type widthType =  (Type) dimension.getWidth().visit(this, arg);
		check(widthType == INT, dimension, "width of dimension must be INT");
		Type heightType =  (Type) dimension.getHeight().visit(this, arg);
		check(heightType == INT, dimension, "height of dimension must be INT");
		return null;
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	private boolean assignmentCompatible(Type targetType, Type rhsType) {
		return (targetType == rhsType
				|| targetType==INT && rhsType==FLOAT
				|| targetType==FLOAT && rhsType==INT
				|| targetType==INT && rhsType==COLOR
				|| targetType==COLOR && rhsType==INT
				|| targetType==IMAGE && rhsType==INT
				|| targetType==IMAGE && rhsType==FLOAT
				|| targetType==IMAGE && rhsType==COLOR
				|| targetType==IMAGE && rhsType==COLORFLOAT
		);
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		//TODO:  implement this method
		String name = assignmentStatement.getName();
		Declaration declaration = symbolTable.lookup(name);
		assignmentStatement.setTargetDec(declaration);
		check(declaration != null, assignmentStatement,
				"name has not been declared");
		Type decType = declaration.getType();
		boolean hasSelector = assignmentStatement.getSelector() != null;

		if (decType != IMAGE) {
			assignmentStatement.getExpr().visit(this, arg);
			Type exprType = assignmentStatement.getExpr().getType();
			check(!hasSelector, assignmentStatement, "invalid pixel selector");
			check(assignmentCompatible(decType, exprType), assignmentStatement,
					"incompatible types in assignment");
			assignmentStatement.getExpr().setCoerceTo(decType);
		} else if (!hasSelector) {
			assignmentStatement.getExpr().visit(this, arg);
			Type exprType = assignmentStatement.getExpr().getType();
			check(assignmentCompatible(decType, exprType), assignmentStatement,
					"incompatible types in assignment");

			if (exprType == INT) {
				assignmentStatement.getExpr().setCoerceTo(COLOR);
			} else if (exprType == FLOAT) {
				assignmentStatement.getExpr().setCoerceTo(COLORFLOAT);
			}
		} else {
			Expr x = assignmentStatement.getSelector().getX();
			Expr y = assignmentStatement.getSelector().getY();

			NameDef nameDefX = new NameDef(x.getFirstToken(), "int", x.getText());
			nameDefX.visit(this, arg);
			symbolTable.init(nameDefX.getName());

			NameDef nameDefY = new NameDef(y.getFirstToken(), "int", y.getText());
			nameDefY.visit(this, arg);
			symbolTable.init(nameDefY.getName());

			assignmentStatement.getSelector().visit(this, arg);
			assignmentStatement.getExpr().visit(this, arg);
			Type exprType = assignmentStatement.getExpr().getType();
			check(assignmentCompatible(decType, exprType), assignmentStatement,
					"incompatible types in assignment");

			symbolTable.remove(x.getText());
			symbolTable.remove(y.getText());

			assignmentStatement.getExpr().setCoerceTo(COLOR);
		}

		symbolTable.init(name);
		return null;
	}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == STRING || destType == CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	private boolean readAssignmentCompatible(Type type) {
		return (type == CONSOLE || type == STRING);
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		//TODO:  implement this method
		Declaration target = symbolTable.lookup(readStatement.getName());
		Type targetType = target.getType();
		check(readStatement.getSelector() == null, readStatement,
				"read statement must not have a pixel selector");
		readStatement.setTargetDec(target);
		Type sourceType = (Type) readStatement.getSource().visit(this, arg);
		check(sourceType == STRING || sourceType == CONSOLE, readStatement,
				"illegal source type for read");
		if (sourceType == CONSOLE) { readStatement.getSource().setCoerceTo(targetType); }
		symbolTable.init(readStatement.getName());
		return null;

	}




	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		//TODO:  implement this method
		Type decType = declaration.getType();
		declaration.getNameDef().visit(this, arg);

		if (decType == IMAGE) { // var type is IMAGE
			if (declaration.getDim() != null) {
				declaration.getDim().visit(this, arg);
			} else {
				check(declaration.getExpr() != null, declaration,
						"an image without a dimension must have an initializer expression");
				declaration.getExpr().visit(this, arg);
				Type exprType = declaration.getExpr().getType();
				check(exprType == IMAGE, declaration,
						"type of expression and declared type do not match");
			}
		} else if (declaration.getOp() != null) { // has initializer expression
			Kind opKind = declaration.getOp().getKind();
			declaration.getExpr().visit(this, arg);
			Type exprType = declaration.getExpr().getType();


			if (opKind == Kind.ASSIGN) { // has assignment initializer
				check(assignmentCompatible(decType, exprType), declaration,
						"incompatible types in assignment");
				NameDefWithDim nameDef = new NameDefWithDim(declaration.getFirstToken(),
						declaration.getExpr().getType().toString().toLowerCase(), declaration.getName(), declaration.getDim());
				declaration = new VarDeclaration(declaration.getFirstToken(), nameDef, declaration.getOp(),
						declaration.getExpr());
				declaration.getExpr().setCoerceTo(decType);

			} else if (opKind == Kind.LARROW) { // has read initializer
				check(readAssignmentCompatible(exprType), declaration,
						"type of expression and declared type do not match");
				ReadStatement read = new ReadStatement(declaration.getNameDef().getFirstToken(),
						declaration.getNameDef().getName(), null, declaration.getExpr());
				read.visit(this, arg);
			}

			symbolTable.init(declaration.getName());
		}
		return null;
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//TODO:  this method is incomplete, finish it.  
		
		//Save root of AST so return type can be accessed in return statements
		root = program;
		symbolTable.programName = program.getName();

		//Check params
		List<NameDef> params = program.getParams();
		for (NameDef node : params) {
			node.visit(this, arg);
			symbolTable.init(node.getName());
		}

		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDef.getName();
		boolean inserted = symbolTable.insert(name, nameDef);
		check(inserted, nameDef, "variable " + name + "already declared");
		return null;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDefWithDim.getName();
		Dimension dim = nameDefWithDim.getDim();
		dim.visit(this, arg);
		Type heightType = dim.getHeight().getType();
		Type widthType = dim.getHeight().getType();
		check(heightType == widthType, nameDefWithDim, "dimension components must have the same type");
		check(heightType == INT, nameDefWithDim, "dimension component type must be int");

		boolean inserted = symbolTable.insert(name, nameDefWithDim);
		check(inserted, nameDefWithDim, "variable " + name + "already declared");
		return null;
	}
 
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return COLOR;
	}

}

package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import static edu.ufl.cise.plc.ast.Types.Type;
import static edu.ufl.cise.plc.ast.Types.Type.*;

import edu.ufl.cise.plc.runtime.*;

import javax.management.StringValueExp;

public class CodeGenVisitor implements ASTVisitor {

    private String packageName;

    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;
    }

    class StringBuilderDelegate {
        StringBuilder str;

        StringBuilderDelegate(Object arg) {
            str = (StringBuilder) arg;
        }

        void add(Object obj) {
            str.append(obj);
        }

        StringBuilder getString() {
            return str;
        }

        void print(Object obj, Boolean multi) {
            str.append("ConsoleIO.console.println(");
            if (multi) {
                str.append("\"\"");
            }
            str.append(obj);
            if (multi) {
                str.append("\"\"");
            }
            str.append(")");
        }

        void readName(Object name, Object targetType) {
            str.append(name);
            str.append(" =");
            str.append(" (");
            str.append(targetType);
            str.append(") ");
        }

        void readConsole() {
            str.append("ConsoleIO.readValueFromConsole(\"");
        }

        void readConsoleExpr(Type type) {
            str.append(type.toString()).append("\",");
            str.append("\"Enter ");
            str.append(capitalizedType(type).toLowerCase());
            str.append(":\")");
        }

        // other
        void multiline(Boolean start) {
            if (start) {
                str.append("\"");
            } else {
                str.append("\"");
            }
        }

        void coerceType(Object type) {
            str.append("(");
            str.append(type);
            str.append(")");
        }

        void setAssignment(Object obj) {
            str.append(obj);
            str.append("=");
        }

        void ternaryCondition() {
            str.append("?");
        }

        void ternaryRes() {
            str.append(":");
        }

        void returnStatement() {
            str.append("return ");
        }
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        res.add(booleanLitExpr.getValue());

        return res.str;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        res.multiline(true);
        res.add(stringLitExpr.getValue());
        res.multiline(false);

        return res.str;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type;

        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Type.INT) {
            type = intLitExpr.getCoerceTo();
        } else {
            type = intLitExpr.getType();
        }

        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Type.INT) {
            res.coerceType(typeToString(type));
        }

        res.add(intLitExpr.getValue());

        return res.str;
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Type type;

        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Type.FLOAT) {
            type = floatLitExpr.getCoerceTo();
        } else {
            type = floatLitExpr.getType();
        }

        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Type.FLOAT) {
            res.coerceType(typeToString(type));
        }

        float floatVal = floatLitExpr.getValue();
        res.add(floatVal);
        res.add("f");

        return res.str;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        res.add("ColorTuple.unpack(Color.");
        res.add(colorConstExpr.getText());
        res.add(".getRGB())");

        return res.getString();
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        res.readConsole();

        return res.str;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        if (colorExpr.getRed().getType() == FLOAT || colorExpr.getRed().getCoerceTo() == FLOAT) {
            res.add("new ColorTupleFloat(");
        } else {
            res.add("new ColorTuple(");
        }


        StringBuilder red = (StringBuilder) colorExpr.getRed().visit(this, new StringBuilder(""));
        StringBuilder green = (StringBuilder) colorExpr.getGreen().visit(this, new StringBuilder(""));
        StringBuilder blue = (StringBuilder) colorExpr.getBlue().visit(this, new StringBuilder(""));
        res.add(red.toString() + ",");
        res.add(green.toString() + ",");
        res.add(blue.toString() + ")");

        return res.getString();
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        IToken op = unaryExpression.getOp();
        IToken.Kind opKind = unaryExpression.getOp().getKind();
        Type exprType = unaryExpression.getExpr().getType();
        StringBuilder exprStr = (StringBuilder) unaryExpression.getExpr().visit(this, new StringBuilder(""));


        if (opKind == IToken.Kind.MINUS) {
            res.add("(");
        }

        if (opKind == IToken.Kind.BANG || opKind == IToken.Kind.MINUS) {
            res.add(unaryExpression.getOp().getText());
            unaryExpression.getExpr().visit(this, res.str);
        } else if (opKind == IToken.Kind.COLOR_OP) {
            if (exprType == INT || exprType == COLOR) {
                res.add(unaryExpression.getExpr().getText() + ".get");
                if (Objects.equals(op.getText(), "getRed")) {
                    res.add("Red(");
                } else if (Objects.equals(op.getText(), "getGreen")) {
                    res.add("Green(");
                } else if (Objects.equals(op.getText(), "getBlue")) {
                    res.add("Blue(");
                }

                res.add(exprStr + ")");
            } else if (exprType == IMAGE) {
                res.add(" ImageOps.extract");
                if (Objects.equals(op.getText(), "getRed")) {
                    res.add("Red(");
                } else if (Objects.equals(op.getText(), "getGreen")) {
                    res.add("Green(");
                } else if (Objects.equals(op.getText(), "getBlue")) {
                    res.add("Blue(");
                }
                res.add(exprStr + ")");
            }
        } else if (opKind == IToken.Kind.IMAGE_OP) {
            unaryExpression.getExpr().visit(this, res.str);
        }

        if (unaryExpression.getOp().getKind() == IToken.Kind.MINUS) {
            res.add(")");
        }
        return res.str;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Type type = identExpr.getType();
        Type coercedType = identExpr.getCoerceTo();

        if (coercedType != null && coercedType != type) {
            res.coerceType(typeToString(coercedType));
        }

        res.add(identExpr.getText());

        return res.str;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        StringBuilder resString = res.getString();

        res.add("(");
        conditionalExpr.getCondition().visit(this, resString);
        res.ternaryCondition();
        conditionalExpr.getTrueCase().visit(this, resString);
        res.ternaryRes();
        conditionalExpr.getFalseCase().visit(this, resString);
        res.add(")");

        return res.getString();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        dimension.getWidth().visit(this, null);
        dimension.getHeight().visit(this, null);
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        StringBuilder x = (StringBuilder) pixelSelector.getX().visit(this, new StringBuilder(""));
        StringBuilder y = (StringBuilder) pixelSelector.getY().visit(this, new StringBuilder(""));

        res.add(x + "," + y);
        return res.getString();
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        String equals = assignmentStatement.getName() + "=";
        StringBuilder exprStr = (StringBuilder) assignmentStatement.getExpr().visit(this, new StringBuilder(""));
        Expr expr = assignmentStatement.getExpr();
        Type targetType = assignmentStatement.getTargetDec().getType();
        Type exprType = assignmentStatement.getExpr().getType();

        String name = assignmentStatement.getName();


        if (targetType == IMAGE && exprType == IMAGE) {
            res.add(equals);
            if (assignmentStatement.getSelector() != null) {
                PixelSelector selector = assignmentStatement.getSelector();
                res.add(" ImageOps.resize(");
                res.add(name + ",");
                res.add((StringBuilder) selector.visit(this, new StringBuilder("")));
                res.add(")");
            } else {
                if (expr instanceof IdentExpr) {
                    res.add(" ImageOps.clone(");
                    res.add(expr.getText() + ")");
                } else {
                    System.out.println("rhs is not identExpr");
                }
            }
        } else if (targetType == IMAGE && expr.getCoerceTo() == COLOR) {


            String xVar;
            String yVar;

            if (assignmentStatement.getSelector() != null) {
                PixelSelector selector = assignmentStatement.getSelector();
                xVar = selector.getX().getText();
                yVar = selector.getY().getText();
            } else {
                xVar = "x";
                yVar = "y";
            }



            res.add("for(int " + xVar + "=0; " + xVar + "<" + name +
                    ".getWidth(); " + xVar + "++)\n");
            res.add("for(int " + yVar + "=0; " + yVar + "<" + name +
                    ".getHeight(); " + yVar + "++)\n");


            res.add("\t\t\tImageOps.setColor(");
            res.add(name + "," + xVar + "," + yVar + ",");

            if (expr.getType() == INT) {
                res.add("new ColorTuple(" + expr.getText() + "," + expr.getText() + "," + expr.getText() + ")");
            } else {
                if (exprStr.toString().contains("ColorTupleFloat")) {
                    res.add("new ColorTuple(");
                }
                res.add(exprStr);
                if (exprStr.toString().contains("ColorTupleFloat")) {
                    res.add(")");
                }
            }

            res.add(")");
        } else if (targetType == IMAGE && (expr.getType() == INT || expr.getCoerceTo() == INT)) {
            PixelSelector selector = assignmentStatement.getSelector();
            String xVar = selector.getX().getText();
            String yVar = selector.getY().getText();

            int val = Integer.parseInt(expr.getText());
            val = val > 255 ? 255 : Math.max(val, 0);

            res.add("for(int " + xVar + "=0; " + xVar + "<" + name +
                    ".getWidth(); " + xVar + "++)\n");
            res.add("for(int " + yVar + "=0; " + yVar + "<" + name +
                    ".getHeight(); " + yVar + "++)\n");
            res.add("\t\tImageOps.setColor(");
            res.add(name + ",x,y,new ColorTuple(");
            res.add(val + ",");
            res.add(val + ",");
            res.add(val + "))");
        } else {
            res.add(equals);
            if (targetType != exprType) {
                res.coerceType(typeToString(targetType));
            }
            res.add(exprStr);
        }


        return res.getString();
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        String sourceText = writeStatement.getSource().getText();
        res.print(writeStatement.getSource().getText(), sourceText.contains("\n"));
        return res.getString();
    }

    public String capitalizedType(Type type) {
        return switch (type) {
            case INT -> "Integer";
            case FLOAT -> "Float";
            case BOOLEAN -> "Boolean";
            case STRING -> "String";
            default -> null;
        };
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Type readTargetType = readStatement.getTargetDec().getType();

        res.readName(readStatement.getName(), capitalizedType(readTargetType));
        readStatement.getSource().visit(this, res.getString());
        res.readConsoleExpr(readTargetType);

        return res.getString();
    }

    String typeToString(Type type) {
        if (type == Type.STRING) return "String";
        else if (type == IMAGE) return "BufferedImage";
        else if (type == COLOR) return "ColorTuple";
        else return type.toString().toLowerCase();
    }

    @Override
    public Object visitNameDef(NameDef nameDefinition, Object arg) throws Exception {
        StringBuilder res = (StringBuilder) arg;

        Type nameType = nameDefinition.getType();
        String typeLowerCase = typeToString(nameType);
        res.append(typeLowerCase).append(" ").append(nameDefinition.getName());

        return res;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        StringBuilder res = (StringBuilder) arg;

        Type nameType = nameDefWithDim.getType();
        String typeLowerCase = typeToString(nameType);
        res.append(typeLowerCase).append(" ").append(nameDefWithDim.getName());

        return res;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        Expr expr = returnStatement.getExpr();
        res.returnStatement();
        expr.visit(this, res.getString());

        return res.getString();
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Type type = binaryExpr.getType();


        res.add("(");

        StringBuilder leftStr = (StringBuilder) binaryExpr.getLeft().visit(this, new StringBuilder(""));
        Expr left = binaryExpr.getLeft();
        Type leftType = left.getType();

        String op = binaryExpr.getOp().getText();

        StringBuilder rightStr = (StringBuilder) binaryExpr.getRight().visit(this, new StringBuilder(""));
        Expr right = binaryExpr.getLeft();
        Type rightType = right.getType();

        if (leftType == IMAGE && rightType == IMAGE) {
            res.add(" ImageOps.binaryImageImageOp(");
            res.add(binaryExpr.getOp().getKind().toString() + ",");
            res.add(left.getText() + ",");
            res.add(right.getText() + ")");
        } else if (leftType == COLOR && rightType == COLOR) {
            res.add(" ImageOps.binaryTupleOp(");
            res.add(binaryExpr.getOp().getKind().toString() + ",");
            res.add(leftStr + ",");
            res.add(rightStr + ")");
        } else if (leftType == IMAGE && rightType == INT) {
            res.add(" ImageOps.binaryImageScalarOp(");
            res.add(binaryExpr.getOp().getKind().toString() + ",");
            res.add(leftStr + ",");
            res.add(rightStr + ")");
        } else if (leftType == STRING && rightType == STRING) {
            if (binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) res.add("!");
            res.add(leftStr);
            res.add(".equals(");
            res.add(rightStr);
            res.add(")");
        } else {
            res.add(leftStr);
            res.add(op);
            res.add(rightStr);
        }
        res.add(")");


        return res.str;
    }

    public Object bufferedImage(String name, Expr expr, Dimension dim, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        if (expr != null) {
            StringBuilder exprStr = (StringBuilder) expr.visit(this, new StringBuilder(""));
            expr.visit(this, new StringBuilder(""));
            System.out.println(expr.getText());

            if (dim != null) {
                if (expr.getType() == STRING) {
                    res.add(" FileURLIO.readImage(");
                    res.add(expr.getText());
                    res.add("," + dim.getWidth().getText());
                    res.add("," + dim.getHeight().getText());
                    res.add(")");
                } else if (expr.getType() == COLOR) {
                    String xVar = dim.getWidth().getText();
                    String yVar = dim.getHeight().getText();
                    res.add("new BufferedImage(" + xVar + "," + yVar + ",BufferedImage.TYPE_INT_RGB);");

                    res.add("for(int x=0; x<" + name +
                            ".getWidth(); x++)\n");
                    res.add("for(int y=0; y<" + name +
                            ".getHeight(); y++)\n");

                    Expr xType = dim.getWidth();
                    Expr yType = dim.getHeight();

                    res.add("\t\t\tImageOps.setColor(");
                    res.add(name + ",x,y,");
                    if (exprStr.toString().contains("ColorTupleFloat")) {
                        res.add("new ColorTuple(");
                    }
                    res.add(exprStr);
                    if (exprStr.toString().contains("ColorTupleFloat")) {
                        res.add(")");
                    }

                    res.add(")");
                }

            } else {
                if (expr instanceof IdentExpr) {
//                    if (expr.getType() == STRING) {
                    res.add(" FileURLIO.readImage(");
                    res.add(expr.getText());
                    res.add(")");
//                    } else if (expr.getType() == COLOR) {
//
//
//                    }
                } else if (expr instanceof BinaryExpr) {
                    String op = ((BinaryExpr) expr).getOp().getKind().toString();
                    StringBuilder leftStr = (StringBuilder) ((BinaryExpr) expr).getLeft().visit(this,
                            new StringBuilder(""));
                    StringBuilder rightStr = (StringBuilder) ((BinaryExpr) expr).getRight().visit(this,
                            new StringBuilder(""));

                    res.add(" ImageOps.binaryImageScalarOp(");
                    res.add(op + ",");
                    res.add(leftStr + ",");
                    res.add(rightStr + ")");
                }
            }
        } else if (dim != null) {
            res.add(" new BufferedImage(");
            res.add(dim.getWidth().getText());
            res.add("," + dim.getHeight().getText());
            res.add(",BufferedImage.TYPE_INT_RGB)");
        }

        return res.getString();
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        StringBuilder resString = res.getString();

        declaration.getNameDef().visit(this, resString);
        Type decType = declaration.getNameDef().getType();

        if (decType == IMAGE) {
            res.add("=");
            StringBuilder buffImage = (StringBuilder) bufferedImage(declaration.getName(), declaration.getExpr(),
                    declaration.getDim(),
                    new StringBuilder(""));
            resString.append(buffImage);
        } else if (declaration.getExpr() != null) {
            res.add("=");
            StringBuilder expr = (StringBuilder) declaration.getExpr().visit(this, new StringBuilder(""));
            Type exprType = declaration.getExpr().getType();

            if (exprType != null && exprType != declaration.getType()) {
                res.coerceType(typeToString(declaration.getType()));
                resString.append("(");
            }
            resString.append(expr);
            if (exprType == Type.CONSOLE) {
                res.readConsoleExpr(decType);
            }
            if (exprType != null && exprType != declaration.getType()) {
                resString.append(")");
            }
        }

        return resString;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        StringBuilder exprStr = (StringBuilder) unaryExprPostfix.getExpr().visit(this, new StringBuilder(""));

        //
        if (unaryExprPostfix.getCoerceTo() != unaryExprPostfix.getType()) {
            res.add("(");
            res.add(unaryExprPostfix.getCoerceTo().toString());
            res.add(")");
        }


        res.add(exprStr + ".getRGB(");
        StringBuilder postfix = (StringBuilder) unaryExprPostfix.getSelector().visit(this, new StringBuilder(""));

        res.add(postfix + ")");

        return res.getString();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        StringBuilder str = new StringBuilder();
        Type returnType = program.getReturnType();

        str.append(String.format("package %s;\n", packageName));
        str.append("import edu.ufl.cise.plc.runtime.*;\n");
        str.append("import java.awt.image.BufferedImage;\n");
        str.append("import static edu.ufl.cise.plc.runtime.ImageOps.OP.*;\n");
        str.append("import java.awt.Color;\n");

        str.append(String.format("public class %s {\n", program.getName()));
        str.append(String.format("\tpublic static %s apply(", typeToString(returnType)));

        List<NameDef> params = program.getParams();
        for (int i = 0; i < params.size(); i++) {
            params.get(i).visit(this, str);
            if (i != params.size() - 1) str.append(", ");
        }

        str.append("){\n");

        List<ASTNode> decsAndStatements = program.getDecsAndStatements();
        for (int i = 0; i < decsAndStatements.size(); i++) {
            str.append("\t");
            decsAndStatements.get(i).visit(this, str);
            str.append(";");
            if (i != decsAndStatements.size() - 1) str.append("\n");
        }

        str.append("\n\t}\n}");

        return str.toString();
    }
}
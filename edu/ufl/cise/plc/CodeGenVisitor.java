package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.awt.image.BufferedImage;
import java.util.List;
import static edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.runtime.*;

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

        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Type.INT)  {
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        res.readConsole();

        return res.str;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);


        if (unaryExpression.getOp().getKind() == IToken.Kind.MINUS) {
            res.add("(");
        }
        res.add(unaryExpression.getOp().getText());
        unaryExpression.getExpr().visit(this, res.str);
        if (unaryExpression.getOp().getKind() == IToken.Kind.MINUS) {
            res.add(")");
        }
        return res.str;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Type type = binaryExpr.getType();

        if (type == Type.IMAGE) {
            throw new UnsupportedOperationException("N/A");
        } else {
//            if ()
//            res.coerceType(typeToString(type));
            res.add("(");
            StringBuilder left = (StringBuilder) binaryExpr.getLeft().visit(this, new StringBuilder(""));
            String op = binaryExpr.getOp().getText();
            StringBuilder right = (StringBuilder) binaryExpr.getRight().visit(this, new StringBuilder(""));
            if (binaryExpr.getLeft().getType() == Type.STRING && binaryExpr.getRight().getType() == Type.STRING) {
                if (binaryExpr.getOp().getKind() == IToken.Kind.NOT_EQUALS) res.add("!");
                res.add(left);
                res.add(".equals(");
                res.add(right);
                res.add(")");
            } else {
                res.add(left);
                res.add(op);
                res.add(right);
            }
            res.add(")");
        }

        return res.str;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Type type;

        if (identExpr.getCoerceTo() != null) {
            type = identExpr.getCoerceTo();
        } else {
            type = identExpr.getType();
        }

        Type identCoerced = identExpr.getCoerceTo();

        if (identCoerced != null && identCoerced != type) {
            res.coerceType(typeToString(identCoerced));
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        res.setAssignment(assignmentStatement.getName());
        StringBuilder expr = (StringBuilder) assignmentStatement.getExpr().visit(this, new StringBuilder(""));
        Type targetType = assignmentStatement.getTargetDec().getType();
        if (targetType != assignmentStatement.getExpr().getType()) {
            res.coerceType(typeToString(targetType));
        }
        res.add(expr);
        return res.getString();
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        String sourceText = writeStatement.getSource().getText();
        res.print(writeStatement.getSource().getText(), sourceText.contains("\n"));
        return res.getString();
    }

    public String capitalizedType(Type type){
        switch(type) {
            case INT:
                return "Integer";
            case FLOAT:
                return "Float";
            case BOOLEAN:
                return "Boolean";
            case STRING:
                return "String";
            default:
                return null;
        }
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
        else return type.toString().toLowerCase();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        StringBuilder str = new StringBuilder();
        Type returnType = program.getReturnType();

        str.append(String.format("package %s;\n", packageName));
        str.append("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
        str.append(String.format("public class %s {\n", program.getName()));
        str.append(String.format("\tpublic static %s apply(", typeToString(returnType)));

        List<NameDef> params = program.getParams();
        for (int i = 0; i < params.size(); i++){
            params.get(i).visit(this, str);
            if (i != params.size()-1) str.append(", ");
        }

        str.append("){\n");

        List<ASTNode> decsAndStatements = program.getDecsAndStatements();
        for (int i = 0; i < decsAndStatements.size(); i++){
            str.append("\t");
            decsAndStatements.get(i).visit(this, str);
            str.append(";");
            if (i != decsAndStatements.size()-1) str.append("\n");
        }

        str.append("\n\t}\n}");

        return str.toString();
    }


    @Override
    public Object visitNameDef(NameDef nameDefintion, Object arg) throws Exception {
        StringBuilder res = (StringBuilder) arg;

        Type nameType = nameDefintion.getType();
        String typeLowerCase = typeToString(nameType);
        res.append(typeLowerCase).append(" ").append(nameDefintion.getName());

        return res;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
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
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
         StringBuilder resString = res.getString();

        declaration.getNameDef().visit(this, resString);
        Type decType = declaration.getNameDef().getType();

        declaration.getNameDef().getDim().visit(this, null);
        Dimension dim = declaration.getNameDef().getDim();
        BufferedImage buffImage;

        if (declaration.getExpr() != null) {
            res.add("=");
            StringBuilder expr = (StringBuilder) declaration.getExpr().visit(this, new StringBuilder(""));
            Type exprType = declaration.getExpr().getType();

            if (decType == Type.IMAGE) {
                if (dim != null) {
                    buffImage = FileURLIO.readImage(declaration.getExpr().getText(),
                            Integer.parseInt(dim.getWidth().getText()),
                            Integer.parseInt(dim.getHeight().getText()));
                } else {
                    buffImage = FileURLIO.readImage(declaration.getExpr().getText());
                }
            }

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
        } else if (dim != null) {
            buffImage = new BufferedImage(Integer.parseInt(dim.getWidth().getText()),
                    Integer.parseInt(dim.getHeight().getText()), BufferedImage.TYPE_INT_ARGB);
        }

        return resString;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.ArrayList;

import static edu.ufl.cise.plc.IToken.Kind;
import static edu.ufl.cise.plc.IToken.Kind.*;
import static edu.ufl.cise.plc.ast.Types.Type;

public class Parser implements IParser {

    Lexer lexer;
    ASTNode root = null;
    Token currToken;

    public Parser(String input) {
        lexer = new Lexer(input);
    }

    public ASTNode booleanLit(Token token) {
        return new BooleanLitExpr(token);
    }

    public ASTNode stringLit(Token token) {
        return new StringLitExpr(token);
    }

    public ASTNode intLit(Token token) {
        return new IntLitExpr(token);
    }

    public ASTNode floatLit(Token token) {
        return new FloatLitExpr(token);
    }

    public ASTNode ident(Token token) {
        return new IdentExpr(token);
    }

    public ASTNode colorConst(Token token) {
        return new ColorConstExpr(token);
    }

    public ASTNode primary(Token token) throws LexicalException, SyntaxException {
        switch(token.kind) {
            case BOOLEAN_LIT:
                next();
                return booleanLit(token);
            case STRING_LIT:
                next();
                return stringLit(token);
            case INT_LIT:
                next();
                return intLit(token);
            case FLOAT_LIT:
                next();
                return floatLit(token);
            case IDENT:
                next();
                return ident(token);
            case COLOR_CONST:
                next();
                return colorConst(token);
            case LPAREN:
                next();
                ASTNode expr = expr(currToken);
                matchCurr(RPAREN);
                next();
                return expr;
            case LANGLE:
                next();
                ASTNode red = expr(currToken);
                matchCurr(COMMA);
                next();
                ASTNode green = expr(currToken);
                matchCurr(COMMA);
                next();
                ASTNode blue = expr(currToken);
                matchCurr(RANGLE);
                next();
                return new ColorExpr((IToken) token, (Expr) red, (Expr) green, (Expr) blue);
            case KW_CONSOLE:
                next();
                return new ConsoleExpr(token);
            case ERROR:
                throw new LexicalException("");

            default:
                throw new SyntaxException("");
        }
    }
    
    public ASTNode dimension(Token token) throws SyntaxException, LexicalException {
        matchCurr(LSQUARE);
        next();
        Expr width = (Expr) expr(currToken);
        matchCurr(COMMA);
        next();
        Expr height = (Expr) expr(currToken);
        matchCurr(RSQUARE);
        next();
        return new Dimension(token, width, height);
    }

    public ASTNode statement(Token token) throws SyntaxException, LexicalException {
        if (isKind(IDENT)) {
            String name = token.getStringValue();
            next();
            PixelSelector selector = null;
            if (isKind(LSQUARE)) {
                next();
                selector = (PixelSelector) pixel(currToken);
            }
            if (isKind(ASSIGN) || isKind(LARROW)) {
                Kind op = currToken.getKind();
                next();
                Expr expr = (Expr) expr(currToken);
                return op == ASSIGN ? new AssignmentStatement(token, name, selector, expr) : new ReadStatement(token,
                        name, selector, expr);
            } else {
                throw new SyntaxException("");
            }
        } else if (isKind(KW_WRITE)) {
            next();
            Expr source = (Expr) expr(currToken);
            matchCurr(RARROW);
            next();
            Expr dest = (Expr) expr(currToken);
            return new WriteStatement(token, source, dest);
        } else if (isKind(RETURN)) {
            next();
            Expr expr = (Expr) expr(currToken);
            return new ReturnStatement(token, expr);
        } else {
            throw new SyntaxException("");
        }
    }

    public ASTNode pixel(Token token) throws SyntaxException, LexicalException {
        ASTNode x = expr(token);
        matchCurr(COMMA);
        next();
        ASTNode y = expr(currToken);
        matchCurr(RSQUARE);
        next();
        return new PixelSelector((IToken) token, (Expr) x, (Expr) y);
    }

    public ASTNode unaryPostfix(Token token) throws SyntaxException, LexicalException {
        ASTNode expr = primary(token);
        if (isKind(LSQUARE)) {
            next();
            return new UnaryExprPostfix((IToken) token, (Expr) expr, (PixelSelector) pixel(currToken));
        }
        else return expr;
    }

    public ASTNode unary(Token token) throws SyntaxException, LexicalException {
        if (isKind(BANG) || isKind(MINUS) || isKind(COLOR_OP) || isKind(IMAGE_OP)) {
            next();
            return new UnaryExpr((IToken) token, (IToken) token, (Expr) unary(currToken));
        } else {
            return unaryPostfix(token);
        }
    }

    public ASTNode multiplicative(Token token) throws SyntaxException, LexicalException {
        ASTNode left = unary(token);
        while (isKind(TIMES) || isKind(DIV) || isKind(MOD)) {
            Token op = currToken;
            next();
            ASTNode right = unary(currToken);
            left = new BinaryExpr((IToken) token, (Expr) left, (IToken) op, (Expr) right);
        }
        return left;
    }

    public ASTNode additive(Token token) throws SyntaxException, LexicalException {
        ASTNode left = multiplicative(token);
        while (isKind(PLUS) || isKind(MINUS)) {
            Token op = currToken;
            next();
            ASTNode right = multiplicative(currToken);
            left = new BinaryExpr((IToken) token, (Expr) left, (IToken) op, (Expr) right);
        }
        return left;
    }

    public ASTNode comparison(Token token) throws SyntaxException, LexicalException {
        ASTNode left = additive(token);
        while (isKind(LT) || isKind(GT) ||
            isKind(LE) || isKind(GE) ||
            isKind(EQUALS) || isKind(NOT_EQUALS)) {
            Token op = currToken;
            next();
            ASTNode right = additive(currToken);
            left = new BinaryExpr((IToken) token, (Expr) left, (IToken) op, (Expr) right);
        }
        return left;
    }

    public ASTNode logicalAnd(Token token) throws SyntaxException, LexicalException {
        ASTNode left = comparison(token);
        while (isKind(AND)) {
            Token op = currToken;
            next();
            ASTNode right = comparison(currToken);
            left = new BinaryExpr((IToken) token, (Expr) left, (IToken) op, (Expr) right);
        }
        return left;
    }

    public ASTNode logicalOr(Token token) throws SyntaxException, LexicalException {
        ASTNode left = logicalAnd(token);
        while (isKind(OR)) {
            Token op = currToken;
            next();
            ASTNode right = logicalAnd(currToken);
            left = new BinaryExpr((IToken) token, (Expr) left, (IToken) op, (Expr) right);
        }
        return left;
    }

    public ASTNode conditional(Token token) throws SyntaxException, LexicalException {
        if (isKind(LPAREN)) {
            next();
            Expr condition = (Expr) expr(currToken);
            matchCurr(RPAREN);
            next();
            Expr trueCase = (Expr) expr(currToken);
            matchCurr(KW_ELSE);
            next();
            Expr falseCase = (Expr) expr(currToken);
            matchCurr(KW_FI);
            next();
            return new ConditionalExpr(token, condition, trueCase, falseCase);
        } else {
            throw new SyntaxException("");
        }
    }

    public ASTNode expr(Token token) throws SyntaxException, LexicalException {
        if (isKind(KW_IF)) {
            next();
            return conditional(currToken);
        } else {
            return logicalOr(token);
        }
    }

    public ASTNode declaration(Token token) throws SyntaxException, LexicalException {
        NameDef nameDef = (NameDef) nameDef(token);
        IToken op = null;
        Expr expr = null;
        if (isKind(ASSIGN) || isKind(LARROW)) {
            op = currToken;
            next();
            expr = (Expr) expr(currToken);
        }
        return new VarDeclaration(token, nameDef, op, expr);
    }

    public ASTNode nameDef(Token token) throws SyntaxException, LexicalException {
        matchCurr(TYPE);
        String type = currToken.getStringValue();
        next();

        Dimension dim = null;
        if (isKind(LSQUARE)) {
            dim = (Dimension) dimension(currToken);
        }

        matchCurr(IDENT);
        String name = currToken.getStringValue();
        next();

        return dim != null ? new NameDefWithDim(token, type, name, dim) : new NameDef(token, type, name);
    }

    public ASTNode program(Token token) throws SyntaxException, LexicalException {
        if (isKind(KW_VOID) || isKind(TYPE)) {
            Type returnType = Type.toType(currToken.getStringValue());
            next();
            matchCurr(IDENT);
            String name = currToken.getStringValue();
            next();
            matchCurr(LPAREN);
            next();
            ArrayList<NameDef> params = new ArrayList<>();
            if (!isKind(RPAREN)) {
                params.add((NameDef) nameDef(currToken));

                while (isKind(COMMA)) {
                    next();
                    params.add((NameDef) nameDef(currToken));
                }
            }
            matchCurr(RPAREN);
            next();
            ArrayList<ASTNode> decsAndStatements = new ArrayList<>();
            while (isKind(TYPE) || isKind(IDENT) || isKind(KW_WRITE) || isKind(RETURN)) {
                if (isKind(TYPE)) decsAndStatements.add(declaration(currToken));
                else decsAndStatements.add(statement(currToken));
                matchCurr(SEMI);
                next();
            }
            if (!isKind(EOF)) throw new SyntaxException("");
            return new Program(token, returnType, name, params, decsAndStatements);
        } else {
            throw new SyntaxException("");
        }
    }

    public void matchCurr(Kind kind) throws SyntaxException, LexicalException {
        if (currToken.kind != kind) throw new SyntaxException("");
    }

    public void next() throws LexicalException {
        currToken = (Token) lexer.next();
    }

    public Token peek() throws LexicalException {
        return (Token) lexer.peek();
    }

    public boolean isKind(Kind kind) {
        return currToken.getKind() == kind;
    }

    public ASTNode check() throws LexicalException, SyntaxException {
        ASTNode currNode = null;
        currToken = (Token) lexer.next();
        return program(currToken);
    }

    @Override
    public ASTNode parse() throws PLCException {
        root = check();
        return root;
    }
}
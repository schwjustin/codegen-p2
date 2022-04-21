package edu.ufl.cise.plc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import static java.util.Map.entry;
import static edu.ufl.cise.plc.IToken.Kind.*;
import static edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    int startPos = 0, currPos = 0, line = 0, col = 0, index = 0;
    String input;
    char[] chars;
    char c;
    ArrayList<Token> tokens = new ArrayList<>();

    Map<String, Kind> reserved = Map.ofEntries(
            entry("true", BOOLEAN_LIT),
            entry("false", BOOLEAN_LIT),
            entry("BLACK", COLOR_CONST),
            entry("BLUE", COLOR_CONST),
            entry("CYAN", COLOR_CONST),
            entry("DARK_GRAY", COLOR_CONST),
            entry("GRAY", COLOR_CONST),
            entry("GREEN", COLOR_CONST),
            entry("LIGHT_GRAY", COLOR_CONST),
            entry("MAGENTA", COLOR_CONST),
            entry("ORANGE", COLOR_CONST),
            entry("PINK", COLOR_CONST),
            entry("RED", COLOR_CONST),
            entry("WHITE", COLOR_CONST),
            entry("YELLOW", COLOR_CONST),
            entry("if", KW_IF),
            entry("fi", KW_FI),
            entry("else", KW_ELSE),
            entry("write", KW_WRITE),
            entry("console", KW_CONSOLE),
            entry("int", TYPE),
            entry("float", TYPE),
            entry("string", TYPE),
            entry("boolean", TYPE),
            entry("color", TYPE),
            entry("image", TYPE),
            entry("void", KW_VOID),
            entry("getRed", COLOR_OP),
            entry("getGreen", COLOR_OP),
            entry("getBlue", COLOR_OP),
            entry("getWidth", IMAGE_OP),
            entry("getHeight", IMAGE_OP)
    );

    public Lexer(String input) {
        this.input = input;
        this.chars = input.toCharArray();
        loop();
    }

    private void loop() {

        while (!isAtEnd()) {

            startPos = currPos;
            c = chars[currPos];
            currPos++;

            switch (c) {

                // symbols
                case '(': addToken(LPAREN); break;
                case ')': addToken(RPAREN); break;
                case '[': addToken(LSQUARE); break;
                case ']': addToken(RSQUARE); break;
                case '+': addToken(PLUS); break;
                case '*': addToken(TIMES); break;
                case '/': addToken(DIV); break;
                case '%': addToken(MOD); break;
                case '&': addToken(AND); break;
                case '|': addToken(OR); break;
                case ';': addToken(SEMI); break;
                case ',': addToken(COMMA); break;
                case '^': addToken(RETURN); break;

                case '!':
                    addToken(match('=') ? NOT_EQUALS : BANG); // !=, !
                    break;
                case '<':
                    addToken(match('=') ? LE : match('<') ? LANGLE : match('-') ?
                            LARROW : LT); // <=, <<, <-, <
                    break;
                case '>':
                    addToken(match('=') ? GE : match('>') ? RANGLE : GT); // >=, >>, >
                    break;
                case '=':
                    addToken(match('=') ? EQUALS : ASSIGN); // ==, =
                    break;
                case '-':
                    addToken(match('>') ? RARROW : MINUS); // -, ->
                    break;

                // comment
                case '#': comment(); break;

                // whitespace
                case ' ', '\r', '\t': col++; break;

                // newline
                case '\n':
                    line++;
                    col = 0;
                    break;

                // string
                case '"': string(); break;

                default:
                    if (isDigit(c)) number();
                    else if (isAlpha(c)) identifier();
                    else addErrorToken("Unrecognized Character");

            }
        }

        tokens.add(new Token(EOF, "", line, 0, 0));
    }

    private void addToken(Kind kind) {
        String text = input.substring(startPos, currPos);
        Token token = new Token(kind, text, line, col, text.length());
        col += text.length();
        tokens.add(token);
    }

    private void addToken(Kind kind, String text) {
        Token token = new Token(kind, text, line, col, text.length());
        col += text.length();
        tokens.add(token);
    }

    private boolean match(char c) {
        if (isAtEnd()) return false;
        if (input.charAt(currPos) != c) return false;
        currPos++;

        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_') ||
                (c == '$');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return (currPos >= input.length());
    }

    private void comment() {
        while (charPeek() != '\n' && !isAtEnd()) currPos++;
    }

    private void string() {
        StringBuilder str = new StringBuilder("\"");
        while (charPeek() != '"' && !isAtEnd()) {
            if (charPeek() == '\n') {
                line++;
                col = 0;
            }
            if (charPeek() == '\\') {
                switch (charPeekNext()) {
                    case 'b', 't', 'n', 'f', 'r', '\"', '\'':
                        str.append(charPeek());
                        currPos++;
                        break;
                    case '\\':
                        str.append(charPeekNext());
                        currPos++;
                        break;
                    default:
                        addErrorToken("");
                        break;
                }
            } else {
                str.append(charPeek());
            }


            currPos++;
        }

        // unterminated string
        if (isAtEnd()) {
            addErrorToken("");
            return;
        }

        currPos++; // closing '"'

        // trim quotes
        addToken(STRING_LIT);
//        addToken(STRING_LIT, input.substring(startPos+1, currPos-1));
    }

    private void number() {

        if (c == '0' && charPeek() == '0') {
            addToken(INT_LIT);
            return;
        }

        while (isDigit(charPeek())) {

            currPos++;
        }

        if (charPeek() == '.' && isDigit(charPeekNext())) {
            currPos++; // skip '.'
            while (isDigit(charPeek())) {
                currPos++;
            }
            addToken(FLOAT_LIT);
        } else {
            BigInteger maxInt = BigInteger.valueOf(Integer.MAX_VALUE);
            BigInteger minInt = BigInteger.valueOf(Integer.MIN_VALUE);
            BigInteger value = new BigInteger(input.substring(startPos, currPos));

            if (value.compareTo(maxInt) > 0) addToken(ERROR);
            else if (value.compareTo(minInt) < 0) addToken(ERROR);
            else addToken(INT_LIT);
        }
    }

    private void identifier() {
        while (isAlphaNumeric(charPeek())) currPos++;
        String text = input.substring(startPos, currPos);

        Kind kind = reserved.get(text);
        if (kind == null) kind = IDENT;
        addToken(kind);
    }

    private void addErrorToken(String error_msg) {
        String rawText = input.substring(startPos, currPos);
        Token token = new Token(Kind.ERROR, rawText, line, col, rawText.length(), error_msg);
        tokens.add(token);
    }

    private char charPeek() {
        if (isAtEnd()) return '\0';
        return input.charAt(currPos);
    }

    private char charPeekNext() {
        if (currPos+1 >= input.length()) return '\0';
        return input.charAt(currPos+1);
    }

    @Override
    public IToken next() throws LexicalException {

        if (index < tokens.size()) {
            if (tokens.get(index).kind == ERROR) throw new LexicalException("");
            else return tokens.get(index++);
        }
        else throw new LexicalException("");
    }

    @Override
    public IToken peek() throws LexicalException {
        if (index < tokens.size()) {
            if (tokens.get(index).kind == ERROR) throw new LexicalException("");
            else return tokens.get(index);
        }
        else throw new LexicalException("");
    }
}

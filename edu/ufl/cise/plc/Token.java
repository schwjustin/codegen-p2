package edu.ufl.cise.plc;

import static edu.ufl.cise.plc.IToken.Kind.STRING_LIT;

public class Token implements IToken {

    final Kind kind; // enum KIND that identifies token type (ALWAYS SET)
    final String rawText; // Text with all characters and escape sequences stored in token
    final int line; // Line # of the first character in the token
    final int col; // Column # of the first character in the token

    final int length; // Overall length of token in characters
    final String error_msg;

    public Token() {
        this.kind = null;
        this.rawText = null;
        this.line = 0;
        this.col = 0;
        this.length = 0;
        this.error_msg = null;
    }

    public Token(Kind kind, String rawText, int line, int col, int length) {
        this.kind = kind;
        this.rawText = rawText;
        this.line = line;
        this.col = col;
        this.length = length;
        this.error_msg = " ";
    }

    public Token(Kind kind, String rawText, int line, int col, int length, String error_msg) {
        this.kind = kind;
        this.rawText = rawText;
        this.line = line;
        this.col = col;
        this.length = length;
        this.error_msg = error_msg;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getText() {
        // Would this return with the \n, \t, and \r characters in the text??
        return rawText;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return new SourceLocation(line, col);
    }

    @Override
    public int getIntValue() {
        if (kind == Kind.INT_LIT) {
            return Integer.parseInt(rawText);
        }
        // What do we return if it's not an INT_LIT????
        return -1;
    }

    @Override
    public float getFloatValue() {
        if (kind == Kind.FLOAT_LIT) {
            return Float.parseFloat(rawText);
        }
        return -1;
    }

    @Override
    public boolean getBooleanValue() {
        if (kind == Kind.BOOLEAN_LIT) {
            return Boolean.parseBoolean(rawText);
        }

        return false;
    }

    @Override
    public String getStringValue() {
        if (kind == Kind.STRING_LIT) {
            int currPos = 0;
            StringBuilder str = new StringBuilder("");

            while (currPos < rawText.length()) {
                if (rawText.charAt(currPos) == '\n') {
//                    str.append("\n");
                }
                if (rawText.charAt(currPos) == '\\') {
                    switch (rawText.charAt(currPos+1)) {
                        case 'b':
                            str.append("\b");
                            currPos++;
                            break;
                        case 't':
                            str.append("\t");
                            currPos++;
                            break;
                        case 'n':
                            str.append("\n");
                            currPos++;
                            break;
                        case 'f':
                            str.append("\f");
                            currPos++;
                            break;
                        case 'r':
                            str.append("\r");
                            currPos++;
                            break;
                        case '"':
                            str.append("\"");
                            currPos++;
                            break;
                        case '\'':
                            str.append("\'");
                            currPos++;
                            break;
                        case '\\':
                            str.append("\\");
                            currPos++;
                            break;
                        default:
                            break;
                    }
                } else if (rawText.charAt(currPos) != '"') {
                    str.append(rawText.charAt(currPos));
                }

                currPos++;
            }

            return String.valueOf(str);

        } else {
            return rawText;
        }
    }
}
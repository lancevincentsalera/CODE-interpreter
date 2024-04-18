package Main.Token.Lexer;

import Main.Token.Token;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
    private int currentPos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        while (currentPos < input.length()) {
            int tokenStartPos = currentPos;
            char lookahead = input.charAt(currentPos);
            if (Character.isWhitespace(lookahead)) {
                currentPos++;
                continue;
            }
            switch (lookahead) {
                case '#':
                    skipComment();
                    break;
                case ':':
                case '+':
                case '-':
                case '(':
                case ')':
                case '[':
                case ']':
                case '\'':
                case '\"':
                case '&':
                case ',':
                case '%':
                case '*':
                    addSingleCharacterToken(tokens, lookahead, tokenStartPos);
                    currentPos++;
                    break;
                case '>':
                    handleGreaterToken(tokens, tokenStartPos);
                    break;
                case '<':
                    handleLessToken(tokens, tokenStartPos);
                    break;
                case '/':
                    tokens.add(new Token(Token.Type.Divide, Character.toString(lookahead), tokenStartPos));
                    currentPos++;
                    break;
                case '$':
                    tokens.add(new Token(Token.Type.NewLine, Character.toString(lookahead), tokenStartPos));
                    currentPos++;
                    break;
                case '=':
                    handleEqualsToken(tokens, tokenStartPos);
                    break;
                default:
                    if (Character.isDigit(lookahead)) {
                        handleNumberToken(tokens, tokenStartPos);
                    } else if (Character.isLetter(lookahead) || lookahead == '_') {
                        handleIdentifierOrKeyword(tokens, tokenStartPos);
                    } else {
                        throw new RuntimeException("ERROR: Unknown character '" + lookahead + "' at position " + currentPos);
                    }
            }
        }
//            printTokens(tokens);
        return tokens;
    }

    private void skipComment() {
        while (currentPos < input.length() && input.charAt(currentPos) != '\n') {
            currentPos++;
        }
    }

    private boolean isValidEscapeChar(char c) {
        return c == '$' || c == '&' || c == '[' || c == ']' || c == '\'' || c == '"' || c == '#';
    }

    private void addSingleCharacterToken(List<Token> tokens, char character, int startPos) {
        Token.Type type;

        if(character == '_') handleIdentifierOrKeyword(tokens, startPos);

        if (character == '[') {
            StringBuilder charLiteralBuilder = new StringBuilder();
            char nextChar = input.charAt(currentPos + 1);

            if (currentPos + 1 < input.length() && isValidEscapeChar(nextChar)) {
                charLiteralBuilder.append(nextChar);
                currentPos++;

                if (currentPos + 1 < input.length() && input.charAt(currentPos + 1) == ']') {
                    String charLiteral = charLiteralBuilder.toString();
                    tokens.add(new Token(Token.Type.Escape, charLiteral, startPos));
                    currentPos += 2;
                    return;
                } else {
                    throw new RuntimeException("ERROR: Unclosed or invalid character literal starting at position " + startPos);
                }
            } else {
                throw new RuntimeException("ERROR: Invalid character following '[' at position " + (startPos + 1));
            }
        }

        if(character == '\'') {
            StringBuilder charLiteralBuilder = new StringBuilder();

            while (currentPos + 1 < input.length() && input.charAt(currentPos + 1) != '\'') {
                char nextChar = input.charAt(currentPos + 1);
                charLiteralBuilder.append(nextChar);
                currentPos++;
            }

            if (currentPos + 1 < input.length() && input.charAt(currentPos + 1) == '\'') {
                String charLiteral = charLiteralBuilder.toString();
                tokens.add(new Token(Token.Type.CharLiteral, charLiteral, startPos));
                currentPos += 2;
                return;
            } else {
                throw new RuntimeException("ERROR: Unclosed character literal starting at position " + startPos);
            }
        }

        if(character == '\"') {
            StringBuilder charLiteralBuilder = new StringBuilder();

            while (currentPos + 1 < input.length() && input.charAt(currentPos + 1) != '\"') {
                char nextChar = input.charAt(currentPos + 1);
                charLiteralBuilder.append(nextChar);
                currentPos++;
            }

            if (currentPos + 1 < input.length() && input.charAt(currentPos + 1) == '\"') {
                String charLiteral = charLiteralBuilder.toString();
                if(charLiteral.equals("TRUE") || charLiteral.equals("FALSE")) {
                    tokens.add(new Token(Token.Type.BooleanLiteral, charLiteral, startPos));
                } else {
                    tokens.add(new Token(Token.Type.StringLiteral, charLiteral, startPos));
                }
                currentPos += 2;
                return;
            } else {
                throw new RuntimeException("ERROR: Unclosed character literal starting at position " + startPos);
            }
        }

        switch (character) {
            case ':':
                type = Token.Type.Colon;
                break;
            case '+':
                type = Token.Type.Plus;
                break;
            case '-':
                if(currentPos < input.length() && tokens.getLast().getType() == Token.Type.Num ||
                        tokens.getLast().getType() == Token.Type.NumFloat ||
                        tokens.getLast().getType() == Token.Type.Identifier ||
                        (tokens.getLast().getType() == Token.Type.Parentheses && tokens.getLast().getText().equals(")")) ) {
                    type = Token.Type.Minus;
                }
                else {
                    type = Token.Type.Negation;
                }
                break;
            case '(':
            case ')':
                type = Token.Type.Parentheses;
                break;
            case '[':
                type = Token.Type.SquareBOpen;
                break;
            case ']':
                type = Token.Type.SquareBClose;
                break;
            case '&':
                type = Token.Type.Concat;
                break;
            case ',':
                type = Token.Type.Comma;
                break;
            case '%':
                type = Token.Type.Modulo;
                break;
            case '*':
                type = Token.Type.Times;
                break;
            default:
                throw new RuntimeException("ERROR: Unknown character '" + character + "' at position " + currentPos);
        }
        tokens.add(new Token(type, Character.toString(character), startPos));
    }

    private void handleGreaterToken(List<Token> tokens, int tokenStartPos) {
        if (currentPos + 1 < input.length()) {
            int temp = currentPos;
            if (input.charAt(++temp) == '=') {
                tokens.add(new Token(Token.Type.GreaterEqual, ">=", tokenStartPos));
                currentPos++;
            } else {
                tokens.add(new Token(Token.Type.Greater, ">", tokenStartPos));
            }
            currentPos++;
        } else {
            tokens.add(new Token(Token.Type.Greater, ">", tokenStartPos));
            currentPos++;
        }
    }

    private void handleLessToken(List<Token> tokens, int tokenStartPos) {
        if (currentPos + 1 < input.length()) {
            int temp = currentPos;
            if (input.charAt(++temp) == '=') {
                tokens.add(new Token(Token.Type.LessEqual, "<=", tokenStartPos));
                currentPos++;
            } else if (input.charAt(temp) == '>') {
                tokens.add(new Token(Token.Type.NotEqual, "<>", tokenStartPos));
                currentPos++;
            } else {
                tokens.add(new Token(Token.Type.Less, "<", tokenStartPos));
            }
            currentPos++;
        } else {
            tokens.add(new Token(Token.Type.Less, "<", tokenStartPos));
            currentPos++;
        }
    }

    private void handleEqualsToken(List<Token> tokens, int tokenStartPos) {
        if (currentPos + 1 < input.length()) {
            int temp = currentPos;
            if (input.charAt(++temp) == '=') {
                tokens.add(new Token(Token.Type.Equals, "==", tokenStartPos));
                currentPos++;
            } else {
                tokens.add(new Token(Token.Type.Assign, "=", tokenStartPos));
            }
            currentPos++;
        } else {
            tokens.add(new Token(Token.Type.Assign, "=", tokenStartPos));
            currentPos++;
        }
    }

    private void handleNumberToken(List<Token> tokens, int tokenStartPos) {
        StringBuilder text = new StringBuilder();
        boolean hasDecimal = false;

        while (currentPos < input.length() && (Character.isDigit(input.charAt(currentPos)) || input.charAt(currentPos) == '.')) {
            char currentChar = input.charAt(currentPos);
            if (currentChar == '.') {
                if (hasDecimal) {
                    throw new RuntimeException("ERROR: Invalid floating-point number format at position " + currentPos);
                }
                hasDecimal = true;
            }
            text.append(currentChar);
            currentPos++;
        }

        tokens.add(new Token(hasDecimal ? Token.Type.NumFloat : Token.Type.Num, text.toString(), tokenStartPos));
    }

    private void handleIdentifierOrKeyword(List<Token> tokens, int tokenStartPos) {
        StringBuilder text = new StringBuilder();
        while (currentPos < input.length() && (Character.isLetterOrDigit(input.charAt(currentPos)) || input.charAt(currentPos) == '_')) {
            text.append(input.charAt(currentPos));
            currentPos++;
        }
        String identifier = text.toString();
        Token.Type type;

        switch (identifier) {
            case "AND":
                type = Token.Type.And;
                break;
            case "DISPLAY":
                type = Token.Type.Print;
                break;
            case "SCAN":
                type = Token.Type.Scan;
                break;
            case "OR":
                type = Token.Type.Or;
                break;
            case "NOT":
                type = Token.Type.Not;
                break;
            case "INT":
                type = Token.Type.Int;
                break;
            case "CHAR":
                type = Token.Type.Char;
                break;
            case "BOOL":
                type = Token.Type.Bool;
                break;
            case "FLOAT":
                type = Token.Type.Float;
                break;
            case "BEGIN":
                if (currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 4 < input.length() && input.substring(currentPos + 1, currentPos + 5).equals("CODE")) {
                    tokens.add(new Token(Token.Type.BeginContainer, "BEGIN CODE", tokenStartPos));
                    currentPos += 5;
                    return;
                }
                type = Token.Type.BeginContainer;
                break;
            case "END":
                if (currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 2 < input.length() && input.substring(currentPos + 1, currentPos + 5).equals("CODE")) {
                    tokens.add(new Token(Token.Type.EndContainer, "END CODE", tokenStartPos));
                    currentPos += 5;
                    return;
                }
                type = Token.Type.EndContainer;
                break;
            case "IF":
                type = Token.Type.If;
                break;
            case "ELSE":
                if (currentPos < input.length() && input.charAt(currentPos) == ' ' && currentPos + 2 < input.length() && input.substring(currentPos + 1, currentPos + 3).equals("IF")) {
                    tokens.add(new Token(Token.Type.IfElse, "ELSE IF", tokenStartPos));
                    currentPos += 3;
                    return;
                }
                type = Token.Type.Else;
                break;
            default:
                if (Character.isLetter(identifier.charAt(0)) || identifier.charAt(0) == '_') {
                    type = Token.Type.Identifier;
                } else {
                    throw new RuntimeException("ERROR: Invalid identifier '" + identifier + "' at position " + tokenStartPos);
                }
        }
        tokens.add(new Token(type, identifier, tokenStartPos));
    }

    private void printTokens(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.println("ERROR: Token " + i + ": Type = " + token.getType() + ", Text = '" + token.getText() + "', StartPos = " + token.getStartPos());
        }
    }
}
class Token {
    TokenType type;
    String data;

    public static final Token DOT = new Token(TokenType.DOT);
    public static final Token EQUALS = new Token(TokenType.EQUALS);
    public static final Token LT = new Token(TokenType.LT);
    public static final Token GT = new Token(TokenType.GT);
    public static final Token NOT = new Token(TokenType.NOT);
    public static final Token NEWLINE = new Token(TokenType.NEWLINE);
    public static final Token INDENT = new Token(TokenType.INDENT);
    public static final Token DEDENT = new Token(TokenType.DEDENT);
    public static final Token COLON = new Token(TokenType.COLON);

    public Token(TokenType t){
        super();
        type = t;
    }

    public String toString(){
        String typeString;
        switch(type){
            case IDENTIFIER: typeString = "Identifier"; break;
            case DOT: typeString = "."; break;
            case EQUALS: typeString = "="; break;
            case LT: typeString = "<"; break;
            case GT: typeString = ">"; break;
            case NOT: typeString = "!"; break;
            case NEWLINE: typeString = "\\n"; break;
            case INDENT: typeString = "--->"; break;
            case DEDENT: typeString = "<---"; break;
            case COLON: typeString = ":"; break;
            case NUMBER: typeString = "Number"; break;
            default: typeString = "???";
        }

        return typeString + ((type == TokenType.IDENTIFIER || type == TokenType.NUMBER) ? (": " + data)  : "");
    }
}

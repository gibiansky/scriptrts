import java.io.*;
import java.util.*;

public class Parser {

    /* Parse a string */
    public static void parse(String text){
        try {
            Token[] tokens = lex(text);
            for(Token t : tokens)
                System.out.println(t);
        } catch (SyntaxException se){
            se.printStackTrace();
        }
    }

    /* Parse contents of a file */
    public static void parse(File file) throws IOException { 
        /* Open the file and create a string builder to read into */
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder fileContents = new StringBuilder();

        /* Read into the string buffer until we get an EOF signal */
        String nextLine = reader.readLine();
        while(nextLine != null){
            fileContents.append(nextLine);
            nextLine = reader.readLine();
        }

        /* Close the file */
        reader.close();

        /* Continue parsing the text */
        parse(fileContents.toString());
    }


    private static Token[] lex(String text) throws SyntaxException {
        /* Store the tokens while lexing */
        ArrayList<Token> tokens = new ArrayList<Token>();

        /* Record whether lines have tabs in front. If they do, don't let them use spaces */
        boolean hasTabs = false, hasSpaces = false;

        /* Keep track of current indent level and past indent levels */
        Stack<Integer> indentLevels = new Stack<Integer>();
        indentLevels.push(0);

        /* Parse by lines, count indent level of each line */
        String[] lines = text.split("\n");
        int currentLine = 1;
        for(String line : lines){

            /* Count indent */
            int i = 0, indentCount = 0; 
            while(line.charAt(i) == ' ' || line.charAt(i) == '\t'){
                indentCount++;
                if(line.charAt(i) == '\t')
                    hasTabs = true;
                if(line.charAt(i) == ' ')
                    hasSpaces = true;

                i++;
            }

            /* Throw an error if they're mixing tabs and spaces */
            if(hasTabs && hasSpaces)
                throw new SyntaxException("mixing spaces and tabs is not permitted.", currentLine);

            /* Emit indent or dedent token if indent level changed */
            if(indentCount < indentLevels.peek()){
                indentLevels.pop();

                /* Make sure we returned to the old indent level, not something in between */
                if(indentCount != indentLevels.peek())
                    throw new SyntaxException("returned to intermediate indent level.", currentLine);

                /* Emit dedent token */
                tokens.add(Token.DEDENT);
            }
            else if(indentCount > indentLevels.peek()){
                /* Push new indent level onto the stack as the current one */
                indentLevels.push(indentCount);

                /* Emit indent token */
                tokens.add(Token.INDENT);
            }

            /* Finish lexing the rest of the line */
            lexLine(tokens, line.substring(i, line.length()));

            /* Go to the next line */
            currentLine++;
        }

        Token[] toks = new Token[tokens.size()];
        toks = tokens.toArray(toks);
        return toks;
    }

    /* Convert a line into tokens, ignoring indentation */
    public static void lexLine(ArrayList<Token> tokens, String text){
        int i = 0;
        while(i < text.length()){
            char next = text.charAt(i);

            /* Parse a number */
            if(Character.isDigit(next)){
                int len = 0;
                char numNext = next;
                while(i + len != text.length() && (numNext == '.' || Character.isDigit(numNext))){
                    numNext = text.charAt(i+len);
                    len++;
                }

                Token number = new Token(TokenType.NUMBER);
                number.data = text.substring(i, i + len - 1);
                tokens.add(number);
                i += len - 2;
            }
            else if(Character.isJavaIdentifierStart(next)){
                int len = 0;
                char idNext = next;
                while(i + len != text.length() && Character.isJavaIdentifierPart(idNext)){
                    idNext = text.charAt(i+len);
                    len++;
                }

                Token ident = new Token(TokenType.IDENTIFIER);
                ident.data = text.substring(i, i + len - 1);
                tokens.add(ident);
                i += len - 2;
            }
            else switch(next){
                case '.': 
                    tokens.add(Token.DOT); break;
                case '=': 
                    tokens.add(Token.EQUALS); break;
                case '<': 
                    tokens.add(Token.LT); break;
                case '>': 
                    tokens.add(Token.GT); break;
                case '!': 
                    tokens.add(Token.NOT); break;
                case '\n': 
                    tokens.add(Token.NEWLINE); break;
                case ':': 
                    tokens.add(Token.COLON); break;
            }
            i++;
        }
    }

}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

// File:   MH_Lexer.java

// Java template file for lexer component of Informatics 2A Assignment 1.
// Concerns lexical classes and lexer for the language MH (`Micro-Haskell').

class MH_Lexer extends GenLexer implements LEX_TOKEN_STREAM {

    static class VarAcceptor extends Acceptor implements DFA {
        public String lexClass() {
            return "VAR";
        };

        public int numberOfStates() {
            return 3;
        };

        int next(int state, char c) {
            switch (state) {
            case 0:
                if (CharTypes.isSmall(c))
                    return 1;
                else
                    return 2;
            case 1:
                if (CharTypes.isSmall(c) || CharTypes.isLarge(c) || CharTypes.isDigit(c) || c == '\'')
                    return 1;
                else
                    return 2;
            default:
                return 2;
            }
        }

        boolean accepting(int state) {
            return (state == 1);
        }

        int dead() {
            return 2;
        }
    }

    static class NumAcceptor extends Acceptor implements DFA {

        // these are my implementation of my convertion of the regular expressions into finite state automata
        public String lexClass() {
            return "NUM";
        };

        public int numberOfStates() {
            return 4;
        };

        int next(int state, char c) {
            switch (state) {
            case 0:
                if (c == '0')
                    return 1;
                else if (CharTypes.isDigit(c))
                    return 2;
            case 1:
                return 3;
            case 2:
                if (CharTypes.isDigit(c) && c != 0)
                    return 2;
                else
                    return 3;
            default:
                return 3;
            }
        }

        boolean accepting(int state) {
            return (state == 1 || state == 2);
        }

        int dead() {
            return 3;
        }
    }

    static class BooleanAcceptor extends Acceptor implements DFA {
        public String lexClass() {
            return "BOOLEAN";
        };

        public int numberOfStates() {
            return 10;
        };

        int next(int state, char c) {
            switch (state) {
            case 0:
                if (c == 'T')
                    return 1;
                else if (c == 'F')
                    return 5;
                else
                    return 9;
            case 1:
                if (c == 'r')
                    return 2;
                else
                    return 9;
            case 2:
                if (c == 'u')
                    return 3;
                else
                    return 9;
            case 3:
                if (c == 'e')
                    return 4;
                else
                    return 9;
            case 4:
                return 9;
            case 5:
                if (c == 'a')
                    return 6;
                else
                    return 9;
            case 6:
                if (c == 'l')
                    return 7;
                else
                    return 9;
            case 7:
                if (c == 's')
                    return 8;
                else
                    return 9;
            case 8:
                if (c == 'e')
                    return 4;
                else
                    return 9;
            default:
                return 9;
            }
        }

        boolean accepting(int state) {
            return (state == 4);
        }

        int dead() {
            return 9;
        }
    }

    static class SymAcceptor extends Acceptor implements DFA {
        public String lexClass() {
            return "SYM";
        };

        public int numberOfStates() {
            return 3;
        };

        int next(int state, char c) {
            switch (state) {
            case 0:
                if (CharTypes.isSymbolic(c))
                    return 1;
                else
                    return 2;
            case 1:
                if (CharTypes.isSymbolic(c))
                    return 1;
                else
                    return 2;
            default:
                return 2;
            }
        }

        boolean accepting(int state) {
            return (state == 1);
        }

        int dead() {
            return 2;
        }
    }

    static class WhitespaceAcceptor extends Acceptor implements DFA {
        public String lexClass() {
            return "";
        };

        public int numberOfStates() {
            return 3;
        };

        int next(int state, char c) {
            switch (state) {
            case 0:
                if (CharTypes.isWhitespace(c))
                    return 1;
                else
                    return 2;
            case 1:
                if (CharTypes.isWhitespace(c))
                    return 1;
                else
                    return 2;
            default:
                return 2;
            }
        }

        boolean accepting(int state) {
            return (state == 1);
        }

        int dead() {
            return 2;
        }
    }

    static class CommentAcceptor extends Acceptor implements DFA {
        public String lexClass() {
            return "";
        };

        public int numberOfStates() {
            return 5;
        };

        int next(int state, char c) {
            switch (state) {
            case 0:
                if (c == '-')
                    return 1;
                else
                    return 4;
            case 1:
                if (c == '-')
                    return 2;
                else
                    return 4;
            case 2:
                if (c == '-')
                    return 2;
                else if (CharTypes.isSymbolic(c) || CharTypes.isNewline(c))
                    return 4;
                else
                    return 3;
            case 3:
                if (CharTypes.isNewline(c))
                    return 4;
                else
                    return 3;
            default:
                return 4;
            }
        }

        boolean accepting(int state) {
            return (state == 2 || state == 3);
        }

        int dead() {
            return 4;
        }
    }

    static class TokAcceptor extends Acceptor implements DFA {

        String tok;
        int tokLen;

        TokAcceptor(String tok) {
            this.tok = tok;
            tokLen = tok.length();
        }

        public String lexClass() {
            return this.tok;
        };

        public int numberOfStates() {
            return tokLen + 2;
        };

        int next(int state, char c) {
            if (state >= tokLen)
                return tokLen + 1;
            if (this.tok.charAt(state) != c)
                return tokLen + 1;
            else {
                state += 1;
                return state;
            }
        }

        boolean accepting(int state) {
            return (tokLen == state);
        }

        int dead() {
            return tokLen + 1;
        }
    }

    // add definitions of MH_acceptors here
    static DFA intAcc = new TokAcceptor("Integer");
    static DFA boolAcc = new TokAcceptor("Bool");
    static DFA ifAcc = new TokAcceptor("if");
    static DFA thenAcc = new TokAcceptor("then");
    static DFA elseAcc = new TokAcceptor("else");
    static DFA leftBracketAcc = new TokAcceptor(")");
    static DFA rightBracketAcc = new TokAcceptor("(");
    static DFA semicolonAcc = new TokAcceptor(";");
    static DFA varAcc = new VarAcceptor();
    static DFA numAcc = new NumAcceptor();
    static DFA spaceAcc = new SpaceAcceptor();
    static DFA booleanAcc = new BooleanAcceptor();
    static DFA symAcc = new SymAcceptor();
    static DFA whiteAcc = new WhitespaceAcceptor();
    static DFA commentAcc = new CommentAcceptor();
    static DFA[] MH_acceptors = new DFA[] { intAcc, ifAcc, boolAcc, thenAcc, elseAcc, leftBracketAcc, rightBracketAcc,
            semicolonAcc, varAcc, numAcc, spaceAcc, booleanAcc, commentAcc, symAcc, whiteAcc };

    MH_Lexer(Reader reader) {
        super(reader, MH_acceptors);
    }

}

class myLexer {

    public static void main(String[] args) throws StateOutOfRange, IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        while (0 == 0) {
            System.out.print("myLexer> ");
            String inputLine = consoleReader.readLine();
            Reader lineReader = new BufferedReader(new StringReader(inputLine));
            GenLexer demoLexer = new MH_Lexer(lineReader);
            try {
                LexToken currTok = demoLexer.pullProperToken();
                while (currTok != null) {
                    System.out.println(currTok.value() + " \t" + currTok.lexClass());
                    currTok = demoLexer.pullProperToken();
                }
            } catch (LexError x) {
                System.out.println("Error: " + x.getMessage());
            }
        }
    }
}

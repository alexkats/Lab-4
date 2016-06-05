import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import utils.GrammarFields;
import utils.GrammarUtils;
import utils.Pair;
import utils.Type;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

public class ClassGenerator {
    private static Set<String> computeFirstForProduction(String rule, List<Pair<String, String>> production, GrammarFields g) {
        Set<String> first = new HashSet<>();
        boolean isEps = true;

        for (Pair<String, String> stringStringPair : production) {
            if (GrammarUtils.isToken(stringStringPair.getFirst())) {
                first.add(stringStringPair.getFirst());
                isEps = false;
                break;
            }

            if (GrammarUtils.isRule(stringStringPair.getFirst())) {
                first.addAll(g.first.get(stringStringPair.getFirst()));

                if (!g.isEpsRule(stringStringPair.getFirst())) {
                    isEps = false;
                    break;
                }
            }
        }

        if (isEps) {
            first.addAll(g.follow.get(rule));
        }

        return first;
    }

    private static String parse(String block, String rule, List<Pair<String, String>> production, GrammarFields g) {
        StringBuilder sb = new StringBuilder();
        boolean isSingleQuotes = false;
        boolean isDoubleQuotes = false;
        boolean isEscape = false;

        for (int i = 1; i < block.length() - 1;) {
            boolean entered = false;

            if (block.charAt(i) != '$' || isSingleQuotes || isDoubleQuotes) {
                sb.append(block.charAt(i));
                entered = true;
            }

            if (!isEscape) {
                switch (block.charAt(i)) {
                    case '\'':
                        isSingleQuotes = !isSingleQuotes;
                        break;
                    case '"':
                        isDoubleQuotes = !isDoubleQuotes;
                        break;
                    case '\\':
                        isEscape = true;
                        break;
                }
            } else {
                isEscape = false;
            }

            if (entered) {
                i++;
                continue;
            }

            i++;
            StringBuilder locals = new StringBuilder();

            while (GrammarUtils.isValidId(block.charAt(i))) {
                locals.append(block.charAt(i++));
            }

            String id = locals.toString();

            for (Pair<String, Type> parameter : g.parameters.get(rule)) {
                if (parameter.getFirst().equals(id)) {
                    sb.append(id);
                    entered = true;
                    break;
                }
            }

            if (entered) {
                continue;
            }

            for (Pair<String, Type> ret : g.returns.get(rule)) {
                if (ret.getFirst().equals(id)) {
                    sb.append("cntx.").append(id);
                    entered = true;
                    break;
                }
            }

            if (entered) {
                continue;
            }

            for (Pair<String, String> stringStringPair : production) {
                if (GrammarUtils.isRule(stringStringPair.getFirst())) {
                    if (id.equals(stringStringPair.getFirst())) {
                        sb.append("cntx.").append(id);
                        break;
                    }
                }

                if (GrammarUtils.isToken(stringStringPair.getFirst())) {
                    if (id.equals(stringStringPair.getFirst())) {
                        sb.append("cntx.").append(id).append(".getText()");
                        break;
                    }
                }
            }
        }

        return sb.toString();
    }

    private static void genRule(String rule, GrammarFields g, PrintWriter out) {
        List<List<Pair<String, String>>> productions = g.grammar.get(rule);
        out.println("    ");
        String cntx = GrammarUtils.firstToUpperCase(rule) + "Context";
        out.println("    public class " + cntx + " {");
        List<Pair<String, Type>> returns = g.returns.get(rule);

        for (Pair<String, Type> decl : returns) {
            out.println("        public " + decl.getSecond() + " " + decl.getFirst() + ";");
        }

        Set<Pair<String, String>> subCntxs = new TreeSet<>();

        for (List<Pair<String, String>> production : productions) {
            Map<String, Integer> manyCntxs = new HashMap<>();

            for (Pair<String, String> stringStringPair : production) {
                if (GrammarUtils.isRule(stringStringPair.getFirst())) {
                    if (!manyCntxs.containsKey(stringStringPair.getFirst())) {
                        manyCntxs.put(stringStringPair.getFirst(), 0);
                    } else {
                        manyCntxs.compute(stringStringPair.getFirst(), (s, a) -> a + 1);
                    }

                    String suff = "";

                    if (manyCntxs.get(stringStringPair.getFirst()) != 0) {
                        suff = String.valueOf(manyCntxs.get(stringStringPair.getFirst()));
                    }

                    subCntxs.add(new Pair<>(stringStringPair.getFirst() + suff, GrammarUtils.firstToUpperCase(stringStringPair.getFirst()) + "Context"));
                }

                if (GrammarUtils.isToken(stringStringPair.getFirst())) {
                    if (!manyCntxs.containsKey(stringStringPair.getFirst())) {
                        manyCntxs.put(stringStringPair.getFirst(), 0);
                    } else {
                        manyCntxs.compute(stringStringPair.getFirst(), (s, a) -> a + 1);
                    }

                    String suff = "";

                    if (manyCntxs.get(stringStringPair.getFirst()) != 0) {
                        suff = String.valueOf(manyCntxs.get(stringStringPair.getFirst()));
                    }

                    subCntxs.add(new Pair<>(stringStringPair.getFirst() + suff, "Token"));
                }
            }
        }

        for (Pair<String, String> subCntx : subCntxs) {
            out.println("        public " + subCntx.getSecond() + " " + subCntx.getFirst() + ";");
        }

        out.println("    }");
        out.println("    ");
        out.print("    public " + cntx + " parse" + GrammarUtils.firstToUpperCase(rule) + "(");
        List<Pair<String, Type>> parameters = g.parameters.get(rule);
        out.print(parameters.stream().map(s -> s.getSecond() + " " + s.getFirst()).collect(Collectors.joining(", ")));
        out.println(") {");
        out.println("        " + cntx + " cntx = new " + cntx + "();");
        Set<String> allFirsts = new HashSet<>();

        for (List<Pair<String, String>> production : productions) {
            out.print("        if (GrammarUtils.isInParams(tokens.curToken().getType()");
            Set<String> first = computeFirstForProduction(rule, production, g);

            if (!Collections.disjoint(allFirsts, first)) {
                System.err.println("Not LL(1)");
            }

            allFirsts.addAll(first);
            first.forEach(s -> out.print(", " + s));
            out.println(")) {");
            Map<String, Integer> manyCntxs = new HashMap<>();

            for (Pair<String, String> stringStringPair : production) {
                if (GrammarUtils.isToken(stringStringPair.getFirst())) {
                    if (!manyCntxs.containsKey(stringStringPair.getFirst())) {
                        manyCntxs.put(stringStringPair.getFirst(), 0);
                    } else {
                        manyCntxs.compute(stringStringPair.getFirst(), (s, a) -> a + 1);
                    }

                    String suff = "";

                    if (manyCntxs.get(stringStringPair.getFirst()) != 0) {
                        suff = String.valueOf(manyCntxs.get(stringStringPair.getFirst()));
                    }

                    out.println("            cntx." + stringStringPair.getFirst() + suff + " = tokens.curToken();");
                    out.println("            tokens.nextToken();");
                }

                if (GrammarUtils.isRule(stringStringPair.getFirst())) {
                    if (!manyCntxs.containsKey(stringStringPair.getFirst())) {
                        manyCntxs.put(stringStringPair.getFirst(), 0);
                    } else {
                        manyCntxs.compute(stringStringPair.getFirst(), (s, a) -> a + 1);
                    }

                    String suff = "";

                    if (manyCntxs.get(stringStringPair.getFirst()) != 0) {
                        suff = String.valueOf(manyCntxs.get(stringStringPair.getFirst()));
                    }

                    String parsed = stringStringPair.getSecond();

                    if (parsed.length() > 1) {
                        parsed = parse(stringStringPair.getSecond(), rule, production, g);
                    }

                    out.println("            cntx." + stringStringPair.getFirst() + suff + " = parse" + GrammarUtils.firstToUpperCase(stringStringPair.getFirst()) + "(" + parsed + ");");
                }

                if (GrammarUtils.isCodeBlock(stringStringPair.getFirst())) {
                    String parsed = parse(stringStringPair.getFirst(), rule, production, g);
                    out.println("            {" + parsed + "}");
                }
            }

            out.println("            return cntx;");
            out.println("        }");
        }

        out.println("        assert true == false;");
        out.println("        return null;");
        out.println("    }");
    }

    private static void genLexer(List<Pair<String, Pair<String, Boolean>>> tokens, String className, PrintWriter out, PrintWriter tokenOut) {
        out.print("import java.io.*;\nimport java.util.*;\nimport java.util.regex.*;\n\nimport utils.*;\n\n" +
                "public class " + className + " implements Tokens {\n" +
                "    public static final int EOF = -1;\n" +
                "    public static final boolean[] skip = new boolean[] {");
        out.print(tokens.stream().map(token -> token.getSecond().getSecond() + "").collect(Collectors.joining(", ")));
        out.print("};\n\n" +
                "    private String s;\n" +
                "    private Token token;\n" +
                "    private int pos;\n" +
                "    private List<Matcher> matchers;\n\n" +
                "    public " + className + "(String s) {\n" +
                "        this.s = s;\n" +
                "        pos = 0;\n" +
                "        matchers = new ArrayList<>();\n");
        tokens.forEach(token -> out.println("        matchers.add(Pattern.compile(\"" + token.getSecond().getFirst().replace("\\", "\\\\") + "\").matcher(s));"));
        out.print("    }\n\n" +
                "    public void nextToken() {\n" +
                "        if (pos < s.length()) {\n" +
                "            int tokenId = -1;\n" +
                "            int end = -1;\n" +
                "            for (int i = 0; i < matchers.size(); i++) {\n" +
                "                Matcher matcher = matchers.get(i);\n" +
                "                if (matcher.find(pos)) {\n" +
                "                    if (matcher.start() == pos && matcher.end() > end) {\n" +
                "                        tokenId = i;\n" +
                "                        end = matcher.end();\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "            if (tokenId >= 0) {\n" +
                "                if (skip[tokenId]) {\n" +
                "                    pos = end;\n" +
                "                    nextToken();\n" +
                "                } else {\n" +
                "                    token = new Token(s.substring(pos, end), tokenId);\n" +
                "                    pos = end;\n" +
                "                }\n" +
                "            } else {\n" +
                "                assert true == false;\n" +
                "            }\n" +
                "        } else {\n" +
                "            token = new Token(\"\", EOF);\n" +
                "        }\n" +
                "    }\n\n" +
                "    public Token curToken() {\n" +
                "        return token;\n" +
                "    }\n" +
                "}\n");
        out.close();
        tokenOut.println("EOF=-1");
        int num = 0;

        for (Pair<String, Pair<String, Boolean>> token : tokens) {
            tokenOut.println(token.getFirst() + "=" + (num++));
        }

        tokenOut.close();
    }

    private static void genParser(GrammarFields g, String className, PrintWriter out) throws FileNotFoundException {
        if (g.options.containsKey("header")) {
            out.println(g.options.get("header"));
        }
        out.print("import java.io.*;\n" +
                "import java.util.*;\n\n" +
                "import utils.*;\n\n" +
                "public class " + className + " {\n");
        BufferedReader br = new BufferedReader(new FileReader("my_gen/" + g.options.get("myparser") + ".tokens"));
        br.lines().forEach(s -> out.println("    public static final int " + s.replace("=", " = ") + ";"));
        out.print("\n" +
                "    private Tokens tokens;\n\n" +
                "    public " + className + "(Tokens tokens) {\n" +
                "        this.tokens = tokens;\n" +
                "        tokens.nextToken();\n" +
                "    }\n");

        for (String rule : g.grammar.keySet()) {
            genRule(rule, g, out);
        }

        out.print("}\n");
        out.close();
    }

    protected static void gen(String lexerName, String parserName) throws IOException {
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileReader(lexerName + ".gr"));
        Lexer lexer = new LexerForLexer(antlrInputStream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        List<Pair<String, Pair<String, Boolean>>> tokens = new ParserForLexer(tokenStream).token_rules().tokens;
        File lexerFile = new File("my_gen/" + GrammarUtils.firstToUpperCase(lexerName) + ".java");
        File tokensFile = new File("my_gen/" + GrammarUtils.firstToUpperCase(lexerName) + ".tokens");
        genLexer(tokens, GrammarUtils.firstToUpperCase(lexerName), new PrintWriter(lexerFile), new PrintWriter(tokensFile));

        ANTLRInputStream antlrInputStream1 = new ANTLRInputStream(new FileReader(parserName + ".gr"));
        Lexer lexer1 = new LexerForParser(antlrInputStream1);
        TokenStream tokenStream1 = new CommonTokenStream(lexer1);
        GrammarFields g = new ParserForParser(tokenStream1).rules().g;
        g.preCompute();
        File parserFile = new File("my_gen/" + GrammarUtils.firstToUpperCase(parserName) + ".java");
        genParser(g, GrammarUtils.firstToUpperCase(parserName), new PrintWriter(parserFile));
    }
}

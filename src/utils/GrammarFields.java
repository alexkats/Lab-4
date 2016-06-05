package utils;

import java.util.*;

/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

public class GrammarFields {
    public Map<String, Set<String>> first;
    public Map<String, Set<String>> follow;
    public Set<String> epsRules;
    public Map<String, Object> options;
    public Map<String, List<Pair<String, Type>>> parameters;
    public Map<String, List<Pair<String, Type>>> returns;
    public Map<String, List<List<Pair<String, String>>>> grammar;

    public GrammarFields() {
        first = new HashMap<>();
        follow = new HashMap<>();
        epsRules = new HashSet<>();
        options = new HashMap<>();
        parameters = new HashMap<>();
        returns = new HashMap<>();
        grammar = new HashMap<>();
    }

    public boolean isEpsRule(String s) {
        return epsRules.contains(s);
    }

    public void preCompute() {
        computeEps();
        computeFirst();
        computeFollow();
    }

    private void computeFirst() {
        for (String rule : grammar.keySet()) {
            first.put(rule, new HashSet<>());
        }

        boolean changed = true;

        while (changed) {
            changed = false;

            for (Map.Entry<String, List<List<Pair<String, String>>>> stringListEntry : grammar.entrySet()) {
                String rule = stringListEntry.getKey();
                List<List<Pair<String, String>>> productions = stringListEntry.getValue();

                for (List<Pair<String, String>> production : productions) {
                    for (Pair<String, String> stringStringPair : production) {
                        if (GrammarUtils.isToken(stringStringPair.getFirst())) {
                            changed |= first.get(rule).add(stringStringPair.getFirst());
                            break;
                        }

                        if (GrammarUtils.isRule(stringStringPair.getFirst())) {
                            changed |= first.get(rule).addAll(first.get(stringStringPair.getFirst()));

                            if (!isEpsRule(stringStringPair.getFirst())) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void computeFollow() {
        for (String rule : grammar.keySet()) {
            follow.put(rule, new HashSet<>());
        }

        follow.get(options.get("start")).add("EOF");
        boolean changed = true;

        while (changed) {
            changed = false;

            for (Map.Entry<String, List<List<Pair<String, String>>>> stringListEntry : grammar.entrySet()) {
                String rule = stringListEntry.getKey();
                List<List<Pair<String, String>>> productions = stringListEntry.getValue();

                for (List<Pair<String, String>> production : productions) {
                    for (int i = 0; i < production.size(); i++) {
                        if (GrammarUtils.isRule(production.get(i).getFirst())) {
                            if (i == production.size() - 1) {
                                changed |= follow.get(production.get(i).getFirst()).addAll(follow.get(rule));
                            } else {
                                if (GrammarUtils.isRule(production.get(i + 1).getFirst())) {
                                    changed |= follow.get(production.get(i).getFirst()).addAll(first.get(production.get(i + 1).getFirst()));

                                    if (isEpsRule(production.get(i + 1).getFirst())) {
                                        changed |= follow.get(production.get(i).getFirst()).addAll(follow.get(rule));
                                    }
                                } else if (GrammarUtils.isToken(production.get(i + 1).getFirst())) {
                                    changed |= follow.get(production.get(i).getFirst()).add(production.get(i + 1).getFirst());
                                } else if (GrammarUtils.isCodeBlock(production.get(i + 1).getFirst())) {
                                    changed |= follow.get(production.get(i).getFirst()).addAll(follow.get(rule));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void computeEps() {
        for (Map.Entry<String, List<List<Pair<String, String>>>> stringListEntry : grammar.entrySet()) {
            String rule = stringListEntry.getKey();
            List<List<Pair<String, String>>> productions = stringListEntry.getValue();

            for (List<Pair<String, String>> production : productions) {
                if (production.size() == 0) {
                    epsRules.add(rule);
                }
            }
        }

        boolean changed = true;

        while (changed) {
            changed = false;

            for (Map.Entry<String, List<List<Pair<String, String>>>> stringListEntry : grammar.entrySet()) {
                String rule = stringListEntry.getKey();
                List<List<Pair<String, String>>> productions = stringListEntry.getValue();

                for (List<Pair<String, String>> production : productions) {
                    boolean ok = true;

                    for (Pair<String, String> stringStringPair : production) {
                        if (GrammarUtils.isRule(stringStringPair.getFirst()) && !isEpsRule(stringStringPair.getFirst())) {
                            ok = false;
                            break;
                        }

                        if (GrammarUtils.isToken(stringStringPair.getFirst())) {
                            ok = false;
                            break;
                        }
                    }

                    if (ok) {
                        changed |= epsRules.add(rule);
                    }
                }
            }
        }
    }
}

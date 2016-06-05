parser grammar ParserForParser;

options {tokenVocab = LexerForParser;}

@header {
import java.io.*;
import java.util.*;

import utils.GrammarFields;
}

rules returns [GrammarFields g] : {$g = new GrammarFields();} (ruleName {
        switch ($ruleName.num) {
            case 0:
                $g.options.put($ruleName.optionString, $ruleName.optionValue);
                break;
            case 1:
                $g.options.put("header", $ruleName.optionString);
                break;
            case 2:
                $g.grammar.put($ruleName.optionString, (List<List<utils.Pair<String, String>>>) $ruleName.optionValue);
                $g.parameters.put($ruleName.optionString, $ruleName.parameters);
                $g.returns.put($ruleName.optionString, $ruleName.returns);
                break;
            default:
                System.err.println("Wrong rule number");
        }
    })* ;

ruleName returns [int num, String optionString, Object optionValue, List<utils.Pair<String, utils.Type>> parameters, List<utils.Pair<String, utils.Type>> returns] locals [boolean isParameters, boolean isReturns]
    : keyword=('start' | 'myparser') value=(RULE | TOKEN) SEMICOLON {$num = 0; $optionString = $keyword.text; $optionValue = $value.text;}
    | 'header' CODE_IN_BRACKETS SEMICOLON {$num = 1; $optionString = $CODE_IN_BRACKETS.text.substring(1, $CODE_IN_BRACKETS.text.length() - 1);}
    | {$isParameters = false; $isReturns = false;} RULE (parameterDeclaration {$isParameters = true;})? (returnDeclaration {$isReturns = true;})? '::' manyProductions SEMICOLON {
        $num = 2;
        $optionString = $RULE.text;
        $optionValue = $manyProductions.productions;
        if ($isParameters) {
            $parameters = $parameterDeclaration.parameters;
        } else {
            $parameters = Collections.emptyList();
        }
        if ($isReturns) {
            $returns = $returnDeclaration.returns;
        } else {
            $returns = Collections.emptyList();
        }
    } ;

parameterDeclaration returns [List<utils.Pair<String, utils.Type>> parameters] : {$parameters = new ArrayList<>();} LEFT_SQUARE_BRACKET type1=type id1=VALID_ID {$parameters.add(new utils.Pair($id1.text, $type1.t));} (COMMA typen=type idn=VALID_ID {$parameters.add(new utils.Pair($idn.text, $typen.t));})* RIGHT_SQUARE_BRACKET ;

returnDeclaration returns [List<utils.Pair<String, utils.Type>> returns] : 'returns' parameterDeclaration {$returns = $parameterDeclaration.parameters;} ;

type returns [utils.Type t] locals [List<utils.Type> subTypes] : VALID_ID {$t = utils.Type.normal($VALID_ID.text);}
    | type1=type LEFT_SQUARE_BRACKET RIGHT_SQUARE_BRACKET {$t = utils.Type.array($type1.t);}
    | {$subTypes = new ArrayList<>();} VALID_ID '<' type1=type {$subTypes.add($type1.t);} (COMMA typen=type {$subTypes.add($typen.t);})* '>' {$t = utils.Type.generic($VALID_ID.text, $subTypes);} ;

manyProductions returns [List<List<utils.Pair<String, String>>> productions] : {$productions = new ArrayList<>();} production1=productionDeclaration {$productions.add($production1.production);} (RULE_OR productionn=productionDeclaration {$productions.add($productionn.production);})* ;

productionDeclaration returns [List<utils.Pair<String, String>> production] locals [String attr] : {$production = new ArrayList<>();}
    ({$attr = "";} RULE (IN_SQUARE {$attr = $IN_SQUARE.text;})? {$production.add(new utils.Pair($RULE.text, $attr));}
    | TOKEN {$production.add(new utils.Pair($TOKEN.text, ""));}
    | CODE_IN_BRACKETS {$production.add(new utils.Pair($CODE_IN_BRACKETS.text, ""));})* ;
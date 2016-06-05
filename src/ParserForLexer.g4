parser grammar ParserForLexer;

options {tokenVocab = LexerForLexer;}

@header {
import java.io.*;
import java.util.*;

import utils.Pair;
}

token_rules returns [List<Pair<String, Pair<String, Boolean>>> tokens] : {$tokens = new ArrayList<>();} (token_rule {$tokens.add($token_rule.token);})* ;

token_rule returns [Pair<String, Pair<String, Boolean>> token] : TOKEN '::' EXPRESSION skipper SEMICOLON {$token = new Pair<>($TOKEN.text, new Pair<>($EXPRESSION.text.substring(1, $EXPRESSION.text.length() - 1), $skipper.isSkip));} ;

skipper returns [Boolean isSkip] : '##' SKIPPER {$isSkip = true;}
    | {$isSkip = false;} ;
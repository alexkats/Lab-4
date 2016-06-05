/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

import org.StructureGraphic.v1.DSutils;

import java.io.*;
import java.util.*;

public class Lab2Main {
    public static void main(String[] args) {
        DSutils.show(new Lab2Parser(new Lab2Lexer("a & (b | c ^ !!!a) | c & d & c")).parseExpr().t, 100, 50);
    }
}

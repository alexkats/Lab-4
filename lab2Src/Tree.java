/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

import org.StructureGraphic.v1.DSTreeNode;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Tree implements DSTreeNode {
    private NodeType node;
    private List<Tree> children;

    public Tree(NodeType node, Tree... children) {
        this.node = node;
        this.children = Arrays.asList(children);
    }

    public NodeType getNode() {
        return node;
    }

    public List<Tree> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(node).append("{");
        for (Tree child: children) {
            sb.append(child).append(", ");
        }
        if (!children.isEmpty()) {
            sb.replace(sb.length() - 2, sb.length(), "}");
        } else {
            sb.append("}");
        }
        return sb.toString();
    }

    @Override
    public DSTreeNode[] DSgetChildren() {
        return children != null ? children.toArray(new DSTreeNode[children.size()]) : new DSTreeNode[0];
    }

    @Override
    public Object DSgetValue() {
        return node;
    }

    @Override
    public Color DSgetColor() {
        return (children == null || children.size() == 0) ? Color.RED : null;
    }
}

package br.ufrgs.rmpestano.intrabundle.jdt;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Created by rmpestano on 2/10/14.
 */
public class StaleReferencesVisitor extends VoidVisitorAdapter {

    private int numGetServices;
    private int numUngetServices;


    public void init() {
        numUngetServices = 0;
        numGetServices = 0;
    }


    @Override
    public void visit(MethodCallExpr node, Object arg) {
        String name = node.getName();
        if(name != null){
            if (name.equals("getService")) {
                numGetServices++;
            } else if (name.toString().equals("ungetService")) {
                numUngetServices++;
            }
        }

        if(node.getChildrenNodes() != null){
            for (Node child :node.getChildrenNodes() ) {
                this.visit(child);
            }
        }

    }

    private void visit(Node node1) {
        if(node1 instanceof EnclosedExpr){
            visit((EnclosedExpr)node1,null);
        } else if(node1 instanceof CastExpr){
            visit((CastExpr)node1,null);
        } else if(node1 instanceof MethodCallExpr){
             visit((MethodCallExpr)node1,null);
        }
    }

    @Override
    public void visit(EnclosedExpr node, Object arg) {
        if(node.getInner() != null){
            this.visit(node.getInner());
        }


    }

    @Override
    public void visit(CastExpr node, Object arg) {
            String name = node.getExpr().toString();
            if(name != null){
                if (name.equals("getService")) {
                    numGetServices++;
                } else if (name.toString().equals("ungetService")) {
                    numUngetServices++;
                }
            }

        if(node.getChildrenNodes() != null){
            for (Node child :node.getChildrenNodes() ) {
                this.visit(child);
            }
        }


    }

    public boolean hasStaleReferences() {
        return numGetServices != numUngetServices;
    }
}



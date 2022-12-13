package chipmunk.compiler.ast.uniform.util;

import chipmunk.compiler.ast.uniform.AstNode;
import chipmunk.compiler.ast.NodeType;

public class VarDec {

    public static boolean isFinal(AstNode node){
        return node.ifType(NodeType.VAR_DEC, () -> node.symbol().get().isFinal());
    }

}

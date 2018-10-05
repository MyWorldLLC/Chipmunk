package chipmunk.truffle.codegen;

import com.oracle.truffle.api.nodes.Node;
import chipmunk.compiler.ast.AstNode;

public interface TruffleAstVisitor<T extends Node> {
	public T visit(AstNode node);

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.OperatorNode;

public abstract class BaseBinaryOperatorParselet implements InfixParselet {
	
	private final boolean leftAssoc;
	
	public BaseBinaryOperatorParselet(){
		leftAssoc = true;
	}
	
	public BaseBinaryOperatorParselet(boolean leftAssoc){
		this.leftAssoc = leftAssoc;
	}

	@Override
	public AstNode parse(ChipmunkParser parser, AstNode left, Token token) {
		AstNode right = parser.parseExpression(leftAssoc ? getPrecedence() : getPrecedence() - 1);
		return new OperatorNode(token, left, right);
	}
	
	@Override
	public int getPrecedence(){
		return 0;
	}

}

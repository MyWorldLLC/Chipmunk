package chipmunk.compiler.ir;

import java.util.ArrayDeque;
import java.util.Deque;

import chipmunk.compiler.Operator;
import chipmunk.compiler.Token;

public class ExpressionBlock extends Block {
	
	protected Deque<ExpressionElement> elements;
	
	public ExpressionBlock(){
		elements = new ArrayDeque<ExpressionElement>();
	}
	
	public Deque<ExpressionElement> getExpression(){
		return elements;
	}
	
	public class ExpressionElement {
		
		protected Token token;
		protected Operator operator;
		
		public ExpressionElement(Token token){
			this.token = token;
		}
		
		public ExpressionElement(Operator operator){
			this.operator = operator;
		}
		
		public boolean isIDOrLiteral(){
			return token != null;
		}
		
		public boolean isOperator(){
			return operator != null;
		}
		
		public Token getIDOrLiteral(){
			return token;
		}
		
		public Operator getOperator(){
			return operator;
		}
	}
}

package chipmunk.compiler.ir;

import java.util.ArrayDeque;
import java.util.Deque;

import chipmunk.compiler.Operator;
import chipmunk.compiler.Token;

public class ExpressionBlock extends Block {
	
	protected Deque<ExpressionPiece> pieces;
	
	public ExpressionBlock(){
		pieces = new ArrayDeque<ExpressionPiece>();
	}
	
	public Deque<ExpressionPiece> getExpression(){
		return pieces;
	}
	
	public class ExpressionPiece {
		
		protected Token token;
		//protected Operator operator;
		protected boolean openGroup;
		protected boolean closeGroup;
		
		public ExpressionPiece(boolean openGroup){
			this.openGroup = openGroup;
			this.closeGroup = !openGroup;
		}
		
		public ExpressionPiece(Token token){
			this.token = token;
		}
		/*
		public ExpressionPiece(Operator operator){
			this.operator = operator;
		}*/
		
		public boolean isIDOrLiteral(){
			return token != null;
		}
		/*
		public boolean isOperator(){
			return operator != null;
		}*/
		
		public boolean isOpenGroup(){
			return openGroup;
		}
		
		public boolean isCloseGroup(){
			return closeGroup;
		}
		
		public Token getIDOrLiteral(){
			return token;
		}
		/*
		public Operator getOperator(){
			return operator;
		}*/
	}
}

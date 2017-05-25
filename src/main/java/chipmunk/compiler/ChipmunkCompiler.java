package chipmunk.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import chipmunk.compiler.parser.ChipmunkBaseListener;
import chipmunk.compiler.parser.ChipmunkLexer;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.modules.lang.CBoolean;
import chipmunk.modules.lang.CFloat;
import chipmunk.modules.lang.CInt;
import chipmunk.modules.lang.CModule;

public class ChipmunkCompiler {
	
	protected TokenStream stream;
	protected ChipmunkLexer lexer;
	protected Deque<ChipmunkAssembler> assemblers;
	
	public ChipmunkCompiler(){
		//lexer = new ChipmunkLexer();
		assemblers = new ArrayDeque<ChipmunkAssembler>();
	}
	
	
	public CModule compile(String src){
		
		CommonTokenStream tokens = new CommonTokenStream(new ChipmunkLexer(CharStreams.fromString(src)));
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		return compile(parser);
	}
	
	public CModule compile(InputStream src) throws IOException {
		
		CommonTokenStream tokens = new CommonTokenStream(new ChipmunkLexer(CharStreams.fromStream(src)));
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		assemblers.push(new ChipmunkAssembler());
		
		return compile(parser);
	}
	
	private CModule compile(ChipmunkParser parser){
		parser.addParseListener(new LiteralListener());
		
		CModule module = new CModule();
		return module;
	}
	
	private class LiteralListener extends ChipmunkBaseListener {
		
		
		@Override
		public void enterHexliteral(ChipmunkParser.HexliteralContext ctx){
			CInt integer = new CInt(Integer.parseInt(ctx.getText().substring(2), 16));
			assemblers.peek().push(integer);
		}
		
		@Override
		public void enterOctliteral(ChipmunkParser.OctliteralContext ctx){
			CInt integer = new CInt(Integer.parseInt(ctx.getText().substring(2), 8));
			assemblers.peek().push(integer);
		}
		
		@Override
		public void enterBinliteral(ChipmunkParser.BinliteralContext ctx){
			CInt integer = new CInt(Integer.parseInt(ctx.getText().substring(2), 2));
			assemblers.peek().push(integer);
		}
		
		@Override
		public void enterBoolliteral(ChipmunkParser.BoolliteralContext ctx){
			CBoolean bool = new CBoolean(Boolean.parseBoolean(ctx.getText()));
			assemblers.peek().push(bool);
		}
		
		@Override
		public void enterFloatliteral(ChipmunkParser.FloatliteralContext ctx){
			CFloat f = new CFloat(Float.parseFloat(ctx.getText()));
			assemblers.peek().push(f);
		}
		
		@Override
		public void enterIntliteral(ChipmunkParser.IntliteralContext ctx){
			CInt integer = new CInt(Integer.parseInt(ctx.getText()));
			assemblers.peek().push(integer);
		}
	}

}

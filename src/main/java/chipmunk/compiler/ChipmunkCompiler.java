package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;

import chipmunk.modules.lang.CModule;

public class ChipmunkCompiler {
	
	protected TokenStream stream;
	protected ChipmunkLexer lexer;
	protected Deque<ChipmunkAssembler> assemblers;
	
	public ChipmunkCompiler(){
		lexer = new ChipmunkLexer();
		assemblers = new ArrayDeque<ChipmunkAssembler>();
	}
	
	
	public CModule compile(CharSequence src) throws CompileChipmunk, SyntaxErrorChipmunk {
		TokenStream tokens = lexer.lex(src);
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		return compile(parser);
	}
	
	public CModule compile(InputStream src) throws IOException, CompileChipmunk, SyntaxErrorChipmunk {
		StringBuilder builder = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(src, Charset.forName("UTF-8")));
		String line = reader.readLine();
		while(line != null){
			builder.append(line);
			line = reader.readLine();
		}
		
		TokenStream tokens = lexer.lex(builder);
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		return compile(parser);
	}
	
	private CModule compile(ChipmunkParser parser){
		// TODO
		assemblers.push(new ChipmunkAssembler());
		return null;
	}

}

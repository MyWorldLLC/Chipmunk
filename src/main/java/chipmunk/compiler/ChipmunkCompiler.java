package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import chipmunk.compiler.parser.ChipmunkBaseListener;
import chipmunk.compiler.parser.ChipmunkLexer;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.parser.ChipmunkParser.ModuleContext;
import chipmunk.modules.lang.CModule;
import chipmunk.modules.lang.CObject;

public class ChipmunkCompiler {
	
	protected TokenStream stream;
	protected ChipmunkLexer lexer;
	
	public ChipmunkCompiler(){
		//lexer = new ChipmunkLexer();
	}
	
	public void lex(InputStream srcStream){
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(srcStream, Charset.forName("UTF-8")));
		StringBuilder builder = new StringBuilder();
		
		// copy chars from input stream to StringBuilder
		try{
			
			char[] copyChars = new char[100];
			
			int read = reader.read(copyChars, 0, copyChars.length);
			
			while(read != -1){
				
				builder.append(copyChars, 0, read);
				
				read = reader.read(copyChars, 0, copyChars.length);
				
			}
			
			
		}catch(IOException ex){
			throw new CompileChipmunk("I/O error while reading source: ", ex);
		}
		
		lex(builder);
	}
	
	public void lex(CharSequence src){
		//stream = lexer.lex(src);
	}
	
	public CModule compile(String src){
		
		CommonTokenStream tokens = new CommonTokenStream(new ChipmunkLexer(new ANTLRInputStream(src)));
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		parser.addParseListener(new ChipmunkBaseListener(){
			
		});
		parser.module();
		
		return null;
	}
	
	public CModule compile(InputStream src) throws IOException {
		
		CommonTokenStream tokens = new CommonTokenStream(new ChipmunkLexer(new ANTLRInputStream(src)));
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		return null;
	}
	
	public CObject evaluate(String src) throws CompileChipmunk {
		
		CommonTokenStream tokens = new CommonTokenStream(new ChipmunkLexer(new ANTLRInputStream(src)));
		ChipmunkParser parser = new ChipmunkParser(tokens);
		
		return null;
	}

}

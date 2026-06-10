package chipmunk.compiler.codegen.lua;

public class LuaPrinter {
    protected final StringBuilder builder = new StringBuilder();

    private String indentation = "";

    public LuaPrinter emit(String src){
        for(var line : src.split("\\r\\n|\\n|\\r")){
            builder.append(indentation).append(line).append("\n");
        }
        return this;
    }

    public LuaPrinter enterBlock(){
        indentation += "  ";
        return this;
    }

    public LuaPrinter exitBlock(){
        if(indentation.length() >= 2){
            indentation = indentation.substring(0, indentation.length() - 2);
        }
        return this;
    }

    public String toSource(){
        return builder.toString();
    }
}

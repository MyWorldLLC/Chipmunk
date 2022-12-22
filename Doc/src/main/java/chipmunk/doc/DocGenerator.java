/*
 * Copyright (C) 2021 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.doc;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.transforms.SymbolTableBuilderVisitor;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.doc.tree.DocAstVisitor;
import chipmunk.doc.tree.DocNode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DocGenerator {

    public static final String INDEX_TEMPLATE = "index.ftl";
    public static final String MODULE_TEMPLATE = "module.ftl";

    protected final Configuration freeMarker;
    protected final List<DocNode> moduleRoots;

    protected DocWriterFactory writerFactory;
    protected Template indexTemplate;
    protected Template moduleTemplate;

    public DocGenerator(){
        freeMarker = new Configuration(Configuration.VERSION_2_3_21);
        moduleRoots = new ArrayList<>();
    }

    public List<DocNode> getDocumentationRoots(){
        return moduleRoots;
    }

    public List<DocNode> buildDocTree(TokenStream tokens, String sourceName){
        ChipmunkParser parser = new ChipmunkParser(tokens);

        DocAstVisitor astVisitor = new DocAstVisitor();
        astVisitor.setTokens(tokens);

        SymbolTableBuilderVisitor symbolVisitor = new SymbolTableBuilderVisitor();

        parser.parse();
        List<AstNode> modules = parser.getModuleRoots();

        for(AstNode module : modules){
            symbolVisitor.visit(module);
            astVisitor.visit(module);
        }

        moduleRoots.addAll(astVisitor.getModuleRoots());
        return astVisitor.getModuleRoots();
    }

    public void setWriterFactory(DocWriterFactory factory){
        writerFactory = factory;
    }

    public DocWriterFactory getWriterFactory(){
        return writerFactory;
    }

    public void setTemplateResource(String path){
        freeMarker.setClassForTemplateLoading(DocGenerator.class, path);
    }

    public void setTemplateResource(Class<?> cls, String path){
        freeMarker.setClassForTemplateLoading(cls, path);
    }

    public void setTemplateDirectory(Path directory) throws IOException {
        freeMarker.setDirectoryForTemplateLoading(directory.toFile());
    }

    public void loadTemplates() throws IOException {
        indexTemplate = freeMarker.getTemplate(INDEX_TEMPLATE);
        moduleTemplate = freeMarker.getTemplate(MODULE_TEMPLATE);
    }

    public void generate() throws IOException, TemplateException {
        if(indexTemplate == null || moduleTemplate == null){
            throw new IllegalStateException("Templates have not been loaded");
        }

        if(writerFactory == null){
            throw new IllegalStateException("Writer factory is null");
        }

        SummaryData summary = new SummaryData();
        summary.setModuleRoots(moduleRoots);

        indexTemplate.process(summary, writerFactory.makeWriter("index.html") );

        moduleRoots.sort(Comparator.comparing(DocNode::getName));

        for(DocNode node : moduleRoots){
            moduleTemplate.process(node, writerFactory.makeWriter(node.getName() + ".html"));
        }
    }
}

package com.bungleton.coreen.as;

import macromedia.asc.parser.MetaDataEvaluator;
import macromedia.asc.parser.ProgramNode;
import macromedia.asc.util.Context;
import flex2.compiler.CompilationUnit;
import flex2.compiler.CompilerContext;
import flex2.compiler.as3.Extension;
import flex2.compiler.as3.reflect.TypeTable;
import flex2.compiler.util.NameFormatter;

public class CoreenFlexCompilerExtension implements Extension
{
    public void parse1(CompilationUnit unit, TypeTable typeTable)
    {
    	System.out.println("p1");
    }

    public void parse2(CompilationUnit unit, TypeTable typeTable)
    {
    	System.out.println("p2");
    }

    public void analyze1(CompilationUnit unit, TypeTable typeTable)
    {
    	System.out.println("a1");
    }

    public void analyze2(CompilationUnit unit, TypeTable typeTable)
    {
    	System.out.println("a2");
    }

    public void analyze3(CompilationUnit unit, TypeTable typeTable)
    {
    	System.out.println("a3");
    }

    public void analyze4(CompilationUnit unit, TypeTable typeTable)
    {
    	System.out.println("a4");
    }

    public void generate(CompilationUnit unit, TypeTable typeTable)
    {
        // this code is similar to code in asc. We don't go through the main asc
        // path, though,
        // and have multiple compilation passes, so we have to have our own
        // version of this code
        CompilerContext flexCx = unit.getContext();
        // Don't do the HashMap lookup for the context. access strongly typed
        // variable for the ASC Context from CompilerContext
        Context cx = flexCx.getAscContext();
        ProgramNode node = (ProgramNode)unit.getSyntaxTree();

        // stop processing if unit.topLevelDefinitions.first() is null
        if (unit.topLevelDefinitions.first() == null)
        {
            return;
        }

        String className = NameFormatter.toDot(unit.topLevelDefinitions.first());

        cx.pushScope(node.frame);

        MetaDataEvaluator printer = new MetaDataEvaluator();
        node.evaluate(cx, printer);

        cx.popScope();
    }

}

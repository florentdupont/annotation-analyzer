package com.example.demo;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    
    public static void run() throws IOException {

        // appel en static
        // CompilationUnit cu = StaticJavaParser.parse(new File("src/main/java/com/example/demo/MyService.java"));
            
       

        // Our sample is in the root of this directory, so no package name.
        // CompilationUnit cu = sourceRoot.parse("", "Blabla.java");

        SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(Parser.class).resolve("src/main/java"));
        
        for(var parseResult : sourceRoot.tryToParse()) {
            
            analyzeThrowsBE(parseResult.getResult().get());
        }

    }
    
    private static void analyzeThrowsBE(CompilationUnit cu) {
        System.out.println("analyzing " + cu.getStorage().get().getFileName());
        cu.accept(new ModifierVisitor<Void>() {

            // analyse toutes les portions de code throw XXX
            @Override
            public Visitable visit(ThrowStmt n, Void arg) {
                if(n.getExpression().isObjectCreationExpr()) {
                    var objectCreationExpr = n.getExpression().asObjectCreationExpr();

                    if(objectCreationExpr.getType().asString().equals("BusinessException")) {
                        System.out.println("BusinessException lancée ");

                        var methodDeclaration = findMethodDeclaration(n);
                        System.out.println("Méthode analysée : " + methodDeclaration.getSignature().asString());

                        var throwsBEannotationExpr = findAnnotationThrowsBE(methodDeclaration);
                        var annotationValues = findAnnotationValue(throwsBEannotationExpr);

                        objectCreationExpr.getArguments().forEach(exceptionArg -> {

                            if(exceptionArg.isStringLiteralExpr()) {
                                var codeExpr = exceptionArg.asStringLiteralExpr().asString();

                                if(annotationValues.contains(exceptionArg)) {
                                    System.out.println("Valeur d'annotation " + codeExpr + " trouvée ! GOOD" );
                                } else {
                                    throw new RuntimeException("le @ThrowsBE n'est pas indiquée pour le code " + codeExpr);
                                }
                            }
                            if(exceptionArg.isNameExpr()) {
                                var codeExpr = exceptionArg.asNameExpr();
                                if(annotationValues.contains(exceptionArg)) {
                                    System.out.println("Valeur d'annotation " + codeExpr + " trouvée ! GOOD" );
                                } else {
                                    throw new RuntimeException("le @ThrowsBE n'est pas indiquée pour le code " + codeExpr);
                                }
                            }

                        });
                    }
                }
                return super.visit(n, arg);
            }

        }, null);
    }
    

    private static MethodDeclaration findMethodDeclaration(ThrowStmt n) {

        var parent = n.getParentNode().orElseThrow(() -> {
            // ParsingException()
            return new RuntimeException("ParsingException : il n'y a pas de noeud parent WTF!");
        });
        
        while(!(parent instanceof MethodDeclaration)) {
            parent = parent.getParentNode().orElseThrow(() -> {
                // ParsingException()
                return new RuntimeException("ParsingException : il n'y a pas de déclaration de méthode !");
            });
            
        }
        return ((MethodDeclaration) parent).asMethodDeclaration();
    }
    
    
    private static AnnotationExpr findAnnotationThrowsBE(MethodDeclaration methodDeclaration) {
        for(var annotationExpr : methodDeclaration.getAnnotations()) {
            if(annotationExpr.getName().asString().equals("ThrowsBE")) {
                return annotationExpr;
            }
        }
        throw new RuntimeException("Il n'y a pas d'annotation @THrowsBE !!");
    }

    /**
     * Retourne une liste de StringLiteralExpr (si valeur literale) ou NameExpr (en cas de constante)
     */
    private static List<Expression> findAnnotationValue(AnnotationExpr annotationExpr) {
        var result = new ArrayList<Expression>();
        if(annotationExpr.isSingleMemberAnnotationExpr()) {
            var singleMemberAnnotationExpr = annotationExpr.asSingleMemberAnnotationExpr();
            if(singleMemberAnnotationExpr.getMemberValue().isArrayInitializerExpr()) {
                var arrayInitializerExpr = singleMemberAnnotationExpr.getMemberValue().asArrayInitializerExpr();
                for(var annotationValue : arrayInitializerExpr.getValues()) {
                    // StringLiteralExpr ou NameExpr
                    result.add(annotationValue);
                }
               
            } else if(singleMemberAnnotationExpr.getMemberValue().isStringLiteralExpr()) {
                var stringLiteralExpr = singleMemberAnnotationExpr.getMemberValue().asStringLiteralExpr();
                result.add(stringLiteralExpr);
            } else if(singleMemberAnnotationExpr.getMemberValue().isNameExpr()) {
                var nameExpr = singleMemberAnnotationExpr.getMemberValue().asNameExpr();
                result.add(nameExpr);
            } else {
                System.out.println("cas pas géré !! " + singleMemberAnnotationExpr);
            }
           // System.out.println("member value " + singleMemberAnnotationExpr.getMemberValue());
        } else {
            throw new RuntimeException("cas non géré pour " + annotationExpr.getNameAsString());
        }
        return result;
    }
    
    
}

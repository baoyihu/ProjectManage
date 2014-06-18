package com.baoyihu.versionmodifier.extract;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.baoyihu.versionmodifier.tools.AstUtills;
import com.baoyihu.versionmodifier.tools.Utills;

public class ClassExtractor
{
    
    private final List<String> removedProperties;
    
    public ClassExtractor(List<String> properties)
    {
        removedProperties = properties;
    }
    
    @SuppressWarnings("unchecked")
    public boolean removeImport(String classFile, List<String> removedClasses)
    {
        String source = Utills.readFile(classFile);
        if (!source.contains(Extractor.ANNOTATION_IMPORT))
        {
            return false;
        }
        
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        if (parser == null)
        {
            return false;
        }
        System.out.println("remove import of :" + classFile);
        parser.setSource(source.toCharArray());
        
        // 使用解析器进行解析并返回AST上下文结果(CompilationUnit为根节点)
        ASTNode node = parser.createAST(null);
        CompilationUnit root = (CompilationUnit)node;
        
        List<ImportDeclaration> list = root.imports();
        TreeMap<Integer, Integer> removeList = new TreeMap<Integer, Integer>();
        
        for (ImportDeclaration temp : list)
        {
            String strTemp = temp.getName().getFullyQualifiedName();
            if (removedClasses.contains(strTemp))
            {
                removeList.put(temp.getStartPosition(), temp.getLength());
            }
        }
        
        if (!removeList.isEmpty())
        {
            while (!removeList.isEmpty())
            {
                Entry<Integer, Integer> entry = removeList.lastEntry();
                int start = entry.getKey();
                int lenth = entry.getValue();
                source = source.substring(0, start) + source.substring(start + lenth);
                removeList.remove(start);
            }
            Utills.writeFiles(classFile, source);
            System.out.println("deal end :" + classFile + " Important Removed!");
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public String doJob(String classFile, String des)
        throws IOException
    {
        String source = Utills.readFile(classFile);
        if (!source.contains(Extractor.ANNOTATION_IMPORT))
        {
            return null;
        }
        
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        if (parser == null)
        {
            return null;
        }
        
        parser.setSource(source.toCharArray());
        
        // 使用解析器进行解析并返回AST上下文结果(CompilationUnit为根节点)
        ASTNode node = parser.createAST(null);
        CompilationUnit root = (CompilationUnit)node;
        
        TreeMap<Integer, Integer> removeList = new TreeMap<Integer, Integer>();
        List<TypeDeclaration> typeDeclaration = root.types();
        boolean removedClass = dealClasses(removeList, typeDeclaration);
        if (removedClass)
        {
            Utills.writeFiles(classFile, null);
            String dir = classFile.substring(0, classFile.lastIndexOf('\\'));
            Utills.deleteDirIfEmpty(dir);
            System.out.println("deal end :" + classFile + " Removed!");
            return AstUtills.getClassFullName(root);
        }
        else
        {
            dealFields(removeList, typeDeclaration);
            dealMethods(removeList, typeDeclaration);
            dealNotes(removeList, source, root);
            
            if (!removeList.isEmpty())
            {
                while (!removeList.isEmpty())
                {
                    Entry<Integer, Integer> entry = removeList.lastEntry();
                    int start = entry.getKey();
                    int lenth = entry.getValue();
                    source = source.substring(0, start) + source.substring(start + lenth);
                    removeList.remove(start);
                }
                Utills.writeFiles(classFile, source);
            }
            System.out.println("deal end :" + classFile + " Modified!");
            return null;
        }
        
    }
    
    private boolean dealClasses(TreeMap<Integer, Integer> removeList, List<TypeDeclaration> types)
    {
        boolean ret = true;
        if (types != null && !types.isEmpty())
        {
            for (TypeDeclaration field : types)
            {
                for (String property : removedProperties)
                {
                    boolean remove = AstUtills.isSelected(field, "SelectedClass", property);
                    if (remove)
                    {
                        removeList.put(field.getStartPosition(), field.getLength());
                    }
                    else
                    {
                        ret = false;
                    }
                }
            }
            
        }
        return ret;
    }
    
    private void dealFields(Map<Integer, Integer> removeList, List<TypeDeclaration> types)
    {
        if (types != null && !types.isEmpty())
        {
            for (TypeDeclaration type : types)
            {
                FieldDeclaration[] fields = type.getFields();
                for (BodyDeclaration field : fields)
                {
                    for (String property : removedProperties)
                    {
                        boolean remove = AstUtills.isSelected(field, "SelectedField", property);
                        if (remove)
                        {
                            removeList.put(field.getStartPosition(), field.getLength());
                        }
                    }
                }
                List<TypeDeclaration> innerClasses = AstUtills.getInnerClass(type);
                if (!innerClasses.isEmpty())
                {
                    dealFields(removeList, innerClasses);
                }
            }
        }
        return;
        
    }
    
    private void dealMethods(Map<Integer, Integer> removeList, List<TypeDeclaration> types)
    {
        if (types != null && !types.isEmpty())
        {
            for (TypeDeclaration type : types)
            {
                BodyDeclaration[] methods = type.getMethods();
                for (BodyDeclaration method : methods)
                {
                    for (String property : removedProperties)
                    {
                        boolean remove = AstUtills.isSelected(method, "SelectedMethod", property);
                        if (remove)
                        {
                            removeList.put(method.getStartPosition(), method.getLength());
                        }
                    }
                    
                }
                List<TypeDeclaration> innerClasses = AstUtills.getInnerClass(type);
                if (!innerClasses.isEmpty())
                {
                    dealMethods(removeList, innerClasses);
                }
            }
        }
        return;
    }
    
    @SuppressWarnings("unchecked")
    private void dealNotes(Map<Integer, Integer> removeList, String source, CompilationUnit root)
    {
        List<Comment> commentList = root.getCommentList();
        int begin = -1;
        int end = -1;
        for (Comment comment : commentList)
        {
            if (comment instanceof LineComment)
            {
                LineComment lineComment = (LineComment)comment;
                String body =
                    source.substring(lineComment.getStartPosition(),
                        lineComment.getStartPosition() + lineComment.getLength());
                body = body.trim().toUpperCase();
                for (String property : removedProperties)
                {
                    String strStart = ("//@Inject_" + property + "_begin").toUpperCase();
                    String strEnd = ("//@Inject_" + property + "_end").toUpperCase();
                    if (begin < 0 && body.startsWith(strStart))
                    {
                        begin = lineComment.getStartPosition();
                        end = -1;
                    }
                    else if (body.startsWith(strEnd))
                    {
                        end = lineComment.getStartPosition() + lineComment.getLength();
                    }
                }
                if (begin > -1 && end > -1)
                {
                    removeList.put(begin, end - begin);
                    begin = -1;
                    end = -1;
                }
            }
        }
        return;
    }
    
}

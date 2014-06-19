package com.baoyihu.versionmodifier.decorate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.baoyihu.versionmodifier.extract.Extractor;
import com.baoyihu.versionmodifier.model.InsertNode;
import com.baoyihu.versionmodifier.tools.AstUtills;
import com.baoyihu.versionmodifier.tools.BlockComparator;
import com.baoyihu.versionmodifier.tools.Utills;
import com.baoyihu.versionmodifier.tools.ValueInterface;

public class ClassDecorator
{
    List<String> addedProperties = null;
    
    public ClassDecorator(List<String> addedProperties)
    {
        this.addedProperties = addedProperties;
    }
    
    @SuppressWarnings("unchecked")
    public String doJob(String sourceFileName, String destFileName)
    {
        if (sourceFileName.contains("ProfilePasswordFragment"))
        {
            System.out.println("deal end :ProfilePasswordFragment");
        }
        
        String source = Utills.readFile(sourceFileName);
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
        ASTNode astNode = parser.createAST(null);
        CompilationUnit root = (CompilationUnit)astNode;
        
        List<InsertNode> insertList = new ArrayList<InsertNode>();
        List<TypeDeclaration> typeDeclaration = root.types();
        
        boolean addedClass = dealClasses(typeDeclaration);
        if (addedClass)
        {
            if (!(new File(destFileName).exists()))
            {
                Utills.writeFiles(destFileName, source);
                System.out.println("deal end :" + destFileName + " Added!");
                return AstUtills.getClassFullName(root);
            }
            else
            {
                System.out.println("deal end :" + destFileName + " no change!");
                return null;
            }
        }
        else
        {
            String dest = Utills.readFile(destFileName);
            
            dealFields(insertList, source, typeDeclaration);
            dealMethods(insertList, source, typeDeclaration);
            dealNotes(insertList, source, dest, root);
            
            if (!insertList.isEmpty())
            {
                addNodeToClass(insertList, source, dest);
            }
            Collections.sort(insertList, new Comparator<InsertNode>()
            {
                
                @Override
                public int compare(InsertNode o1, InsertNode o2)
                {
                    return o2.getInsertStart() - o1.getInsertStart();
                }
            });
            if (!insertList.isEmpty())
            {
                while (!insertList.isEmpty())
                {
                    InsertNode entry = insertList.remove(0);
                    int start = entry.getInsertStart();
                    StringBuffer buffer = new StringBuffer(dest.substring(0, start));
                    buffer.append(Utills.SPLITE_LINE);
                    buffer.append(Utills.SPLITE_LINE);
                    buffer.append(entry.getData());
                    buffer.append(Utills.SPLITE_LINE);
                    buffer.append(Utills.SPLITE_LINE);
                    buffer.append(dest.substring(start));
                    dest = buffer.toString();
                }
                dest = dealImports(insertList, source, dest);
                Utills.writeFiles(destFileName, dest);
            }
            System.out.println("deal end :" + destFileName + " Modified!");
            return null;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private void dealNotes(List<InsertNode> insertList, String source, String dest, CompilationUnit sourceRoot)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        if (parser == null)
        {
            return;
        }
        
        parser.setSource(dest.toCharArray());
        
        // 使用解析器进行解析并返回AST上下文结果(CompilationUnit为根节点)
        ASTNode astNode = parser.createAST(null);
        CompilationUnit destRoot = (CompilationUnit)astNode;
        
        List<Comment> commentList = sourceRoot.getCommentList();
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
                for (String property : addedProperties)
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
                    
                    MethodDeclaration inMethod = getMethodOfBlock(begin, end, sourceRoot.types());
                    MethodDeclaration targetMethod = getMethodOfAs(inMethod, destRoot.types());
                    
                    if (inMethod != null && targetMethod != null)
                    {//TODO if the target is null and inMethod is not null ,we should notify the user
                        String sourceBlock = getBlockStringOf(inMethod, source);
                        String targetBlock = getBlockStringOf(targetMethod, dest);
                        String addbody = source.substring(begin, end);
                        int targetStart = targetMethod.getBody().getStartPosition();
                        begin -= inMethod.getBody().getStartPosition();
                        end -= inMethod.getBody().getStartPosition();
                        int insertPoint = findInsertPoint(sourceBlock, targetBlock, begin, end);
                        InsertNode insertNode =
                            new InsertNode((TypeDeclaration)targetMethod.getParent(), insertPoint + targetStart, end
                                - begin);
                        insertNode.setData(addbody);
                        insertNode.setInsertStart(insertNode.getStart());
                        insertList.add(insertNode);
                    }
                    else
                    {
                        System.out.printf("Can not inert the Annotation:" + body);
                    }
                    begin = -1;
                    end = -1;
                }
            }
        }
        return;
    }
    
    private String getBlockStringOf(MethodDeclaration method, String str)
    {
        Block block = method.getBody();
        return str.substring(block.getStartPosition(), block.getStartPosition() + block.getLength());
    }
    
    class WrapNode implements ValueInterface
    {
        
        @Override
        public boolean valueEquals(ValueInterface other)
        {
            if (other instanceof WrapNode)
            {
                WrapNode otherNode = (WrapNode)other;
                return (otherNode.innerData.equals(innerData));
            }
            return false;
        }
        
        String innerData = null;
        
        int start = 0;
        
        public WrapNode(String data, int start)
        {
            this.innerData = data;
            this.start = start;
        }
        
        public int getStartPosition()
        {
            return start;
        }
        
        public int getLength()
        {
            return innerData.length();
        }
        
        public int getEnd()
        {
            return start + innerData.length();
        }
        
        @Override
        public String toString()
        {
            if (innerData != null)
            {
                return innerData;
            }
            else
            {
                return "innerData is null";
            }
        }
        
        @Override
        public int hashCode()
        {
            return innerData.hashCode();
        }
        
    }
    
    public List<WrapNode> wrap(String innerList)
    {
        List<WrapNode> ret = new ArrayList<WrapNode>();
        int index = 0;
        for (String node : innerList.split("\r\n"))
        {
            index = innerList.indexOf(node, index);
            WrapNode wrapNode = this.new WrapNode(node, index);
            index += node.length() + 2;//for \r\n;
            ret.add(wrapNode);
        }
        return ret;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private int findInsertPoint(String sourceMethod, String targetMethod, int begin, int end)
    {
        int ret = 0;
        List<WrapNode> sourceWrap = wrap(sourceMethod);
        List<WrapNode> targetWrap = wrap(targetMethod);
        ret = 0; // TODO we need to test the list is empty;
        WrapNode theNode = null;
        for (WrapNode node : sourceWrap)
        {
            if (node.getStartPosition() <= begin && node.getEnd() > begin)
            {
                theNode = node;
                break;
            }
        }
        
        if (theNode != null)
        {
            BlockComparator<WrapNode> comparator = new BlockComparator<WrapNode>(sourceWrap, targetWrap);
            comparator.compare();
            WrapNode comparedNode = comparator.findPreItem(theNode, sourceWrap, targetWrap, true);
            if (comparedNode != null)
            {
                ret = comparedNode.getStartPosition() + comparedNode.innerData.length();
            }
        }
        System.out.printf("the position is " + ret);
        return ret;
    }
    
    private MethodDeclaration getMethodOfAs(MethodDeclaration declaration, List<TypeDeclaration> types)
    {
        for (TypeDeclaration type : types)
        {
            MethodDeclaration[] methods = type.getMethods();
            for (MethodDeclaration method : methods)
            {
                if (AstUtills.theSameAs(method, declaration))
                {
                    return method;
                }
            }
        }
        return null;
        
    }
    
    private MethodDeclaration getMethodOfBlock(int start, int end, List<TypeDeclaration> types)
    {
        for (TypeDeclaration type : types)
        {
            MethodDeclaration[] methods = type.getMethods();
            for (MethodDeclaration method : methods)
            {
                int thisStart = method.getStartPosition();
                int thisEnd = method.getLength() + thisStart;
                if (start >= thisStart && end <= thisEnd)
                {
                    return method;
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private String dealImports(List<InsertNode> insertList, String source, String dest)
    {
        
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        if (parser == null)
        {
            return dest;
        }
        //     System.out.println("remove import of :" + classFile);
        parser.setSource(source.toCharArray());
        ASTNode node = parser.createAST(null);
        CompilationUnit root = (CompilationUnit)node;
        List<ImportDeclaration> sourceImport = root.imports();
        
        parser.setSource(dest.toCharArray());
        node = parser.createAST(null);
        root = (CompilationUnit)node;
        List<ImportDeclaration> destImport = root.imports();
        
        for (ImportDeclaration import1 : sourceImport)
        {
            boolean add = true;
            String name1 = import1.getName().getFullyQualifiedName();
            for (ImportDeclaration import2 : destImport)
            {
                String name2 = import2.getName().getFullyQualifiedName();
                if (name1.equals(name2))
                {
                    add = false;
                    break;
                }
            }
            if (add)
            {
                InsertNode insertNode = new InsertNode(null, import1.getStartPosition(), import1.getLength());
                insertNode.setData(source.substring(import1.getStartPosition(),
                    import1.getStartPosition() + import1.getLength()));
                insertList.add(insertNode);
            }
        }
        
        if (!insertList.isEmpty())
        {
            ImportDeclaration lastImport = destImport.get(destImport.size() - 1);
            if (lastImport != null)
            {
                int insertPoint = lastImport.getLength() + lastImport.getStartPosition();
                while (!insertList.isEmpty())
                {
                    InsertNode entry = insertList.remove(0);
                    String data = entry.getData();
                    dest =
                        dest.substring(0, insertPoint) + Utills.SPLITE_LINE + Utills.SPLITE_LINE + data
                            + Utills.SPLITE_LINE + dest.substring(insertPoint);
                }
                //Utills.writeFiles(classFile, source);
                return dest;
                // System.out.println("deal end :" + classFile + " Important Added!");
            }
        }
        return dest;
    }
    
    private void dealMethods(List<InsertNode> insertList, String source, List<TypeDeclaration> types)
    {
        if (types != null && !types.isEmpty())
        {
            for (TypeDeclaration type : types)
            {
                BodyDeclaration[] methods = type.getMethods();
                BodyDeclaration preMethod = null;
                for (BodyDeclaration method : methods)
                {
                    
                    for (String property : addedProperties)
                    {
                        boolean added = AstUtills.isSelected(method, "SelectedMethod", property);
                        if (added)
                        {
                            InsertNode node = new InsertNode(type, method.getStartPosition(), method.getLength());
                            node.setData(source.substring(method.getStartPosition(),
                                method.getStartPosition() + method.getLength()));
                            node.setPreNode(preMethod);
                            insertList.add(node);
                        }
                    }
                    preMethod = method;
                }
                List<TypeDeclaration> innerClasses = AstUtills.getInnerClass(type);
                if (!innerClasses.isEmpty())
                {
                    //FIXME we need to check this carefully
                    dealMethods(insertList, source, innerClasses);
                }
            }
        }
        return;
    }
    
    private void dealFields(List<InsertNode> insertList, String source, List<TypeDeclaration> types)
    {
        if (types != null && !types.isEmpty())
        {
            for (TypeDeclaration type : types)
            {
                FieldDeclaration[] fields = type.getFields();
                BodyDeclaration preField = null;
                for (BodyDeclaration field : fields)
                {
                    for (String property : addedProperties)
                    {
                        boolean add = AstUtills.isSelected(field, "SelectedField", property);
                        if (add)
                        {
                            
                            InsertNode node = new InsertNode(type, field.getStartPosition(), field.getLength());
                            node.setData(source.substring(field.getStartPosition(),
                                field.getStartPosition() + field.getLength()));
                            node.setPreNode(preField);
                            insertList.add(node);
                        }
                    }
                    preField = field;
                }
                List<TypeDeclaration> innerClasses = AstUtills.getInnerClass(type);
                if (!innerClasses.isEmpty())
                {
                    //FIXME we need to check this carefully
                    dealFields(insertList, source, innerClasses);
                }
            }
        }
        return;
        
    }
    
    private boolean dealClasses(List<TypeDeclaration> types)
    {
        boolean ret = true;
        if (types != null && !types.isEmpty())
        {
            for (TypeDeclaration field : types)
            {
                for (String property : addedProperties)
                {
                    boolean remove = AstUtills.isSelected(field, "SelectedClass", property);
                    if (remove)
                    {
                        ;
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
    
    @SuppressWarnings("unchecked")
    private void addNodeToClass(List<InsertNode> insertList, String source, String dest)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        if (parser == null)
        {
            return;
        }
        
        parser.setSource(dest.toCharArray());
        
        // 使用解析器进行解析并返回AST上下文结果(CompilationUnit为根节点)
        ASTNode astNode = parser.createAST(null);
        CompilationUnit root = (CompilationUnit)astNode;
        
        List<TypeDeclaration> typeDeclaration = root.types();
        for (TypeDeclaration destType : typeDeclaration)
        {
            int start;
            int lenth;
            
            for (InsertNode node : insertList)
            {
                TypeDeclaration nodeType = node.getType();
                if (AstUtills.theSameAs(destType, nodeType))
                {
                    
                    ASTNode sourcePreNode = node.getPreNode();
                    if (sourcePreNode == null)
                    {
                        //it is Anonation
                        node.setInsertStart(node.getStart());
                    }
                    else
                    {
                        ASTNode targetPreNode = getParedBodyNodeFrom(destType, (BodyDeclaration)sourcePreNode);
                        if (targetPreNode != null)
                        {
                            start = targetPreNode.getStartPosition();
                            lenth = targetPreNode.getLength();
                            node.setInsertStart(start + lenth);
                        }
                        else
                        {
                            //TODO maybe we can insert field before Methods
                            start = destType.getStartPosition();
                            lenth = destType.getLength();
                            node.setInsertStart(start + lenth - 1);
                        }
                    }
                }
            }
        }
        
    }
    
    private ASTNode getParedBodyNodeFrom(TypeDeclaration type, BodyDeclaration sourceField)
    {
        if (sourceField instanceof FieldDeclaration)
        {
            for (FieldDeclaration field : type.getFields())
            {
                if (AstUtills.theSameAs(field, sourceField))
                {
                    return field;
                }
            }
        }
        else if (sourceField instanceof MethodDeclaration)
        {
            for (MethodDeclaration field : type.getMethods())
            {
                if (AstUtills.theSameAs(field, sourceField))
                {
                    return field;
                }
            }
        }
        return null;
    }
}

package com.baoyihu.versionmodifier.model;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class InsertNode
{
    TypeDeclaration type;
    
    ASTNode proNode;
    
    public ASTNode getPreNode()
    {
        return proNode;
    }
    
    public void setPreNode(ASTNode before)
    {
        this.proNode = before;
    }
    
    String data;
    
    int start;
    
    int lenth;
    
    int insertStart;
    
    public int getInsertStart()
    {
        return insertStart;
    }
    
    public void setInsertStart(int insertStart)
    {
        this.insertStart = insertStart;
    }
    
    public int getStart()
    {
        return start;
    }
    
    public int getLenth()
    {
        return lenth;
    }
    
    public TypeDeclaration getType()
    {
        return type;
    }
    
    public void setType(TypeDeclaration type)
    {
        this.type = type;
    }
    
    public String getData()
    {
        return data;
    }
    
    public void setData(String data)
    {
        this.data = data;
    }
    
    public InsertNode(TypeDeclaration type, String data)
    {
        this.type = type;
        this.data = data;
    }
    
    public InsertNode(TypeDeclaration type, int start, int lenth)
    {
        this.type = type;
        this.start = start;
        this.lenth = lenth;
    }
}

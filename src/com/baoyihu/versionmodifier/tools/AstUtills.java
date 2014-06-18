package com.baoyihu.versionmodifier.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AstUtills
{
    @SuppressWarnings("unchecked")
    public static String getClassFullName(CompilationUnit root)
    {
        List<TypeDeclaration> types = root.types();
        String ret = null;
        if (types != null && !types.isEmpty())
        {
            TypeDeclaration field = types.get(0);
            PackageDeclaration declare = root.getPackage();
            if (declare != null)
            {
                ret = declare.getName().getFullyQualifiedName() + "." + field.getName();
            }
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    public static List<TypeDeclaration> getInnerClass(TypeDeclaration parentClass)
    {
        List<TypeDeclaration> innerClasses = new ArrayList<TypeDeclaration>();
        List<BodyDeclaration> declares = parentClass.bodyDeclarations();
        for (BodyDeclaration obj : declares)
        {
            if (obj instanceof TypeDeclaration)
            {
                TypeDeclaration innerType = (TypeDeclaration)obj;
                innerClasses.add(innerType);
            }
        }
        return innerClasses;
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isSelected(BodyDeclaration declare, String tag, String condition)
    {
        
        List<?> list = declare.modifiers();
        for (Object obj : list)
        {
            if (obj instanceof NormalAnnotation)
            {
                NormalAnnotation anno = (NormalAnnotation)obj;
                if (anno.getTypeName().getFullyQualifiedName().equals(tag))
                {
                    List<MemberValuePair> propertyList = anno.values();
                    for (MemberValuePair valueObj : propertyList)
                    {
                        SimpleName name = valueObj.getName();
                        Expression expression = valueObj.getValue();
                        if (expression instanceof StringLiteral)
                        {
                            StringLiteral literal = (StringLiteral)expression;
                            if (literal.getLiteralValue().equals(condition))
                            {
                                return true;
                            }
                        }
                        else if (expression instanceof ArrayInitializer)
                        {
                            ArrayInitializer value = (ArrayInitializer)valueObj.getValue();
                            if (name.getIdentifier().equals("when"))
                            {
                                List<StringLiteral> whens = value.expressions();
                                for (StringLiteral literal : whens)
                                {
                                    if (literal.getLiteralValue().equals(condition))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                        
                    }
                }
                
            }
        }
        return false;
    }
    
    public static boolean theSameAs(TypeDeclaration type1, TypeDeclaration type2)
    {
        if (type1 == null || type2 == null)
        {
            return false;
        }
        SimpleName name1 = type1.getName();
        SimpleName name2 = type2.getName();
        if (name1 == null || name2 == null)
        {
            return false;
        }
        return (name1.getFullyQualifiedName().equals(name2.getFullyQualifiedName()));
    }
    
    public static boolean theSameAs(BodyDeclaration type1, BodyDeclaration type2)
    {
        if (type1 == null || type2 == null)
        {
            return false;
        }
        String name1 = getDeclarationName(type1);
        String name2 = getDeclarationName(type2);
        if (name1 == null || name2 == null)
        {
            return false;
        }
        return (name1.equals(name2));
    }
    
    @SuppressWarnings("unchecked")
    public static String getDeclarationName(BodyDeclaration body)
    {
        if (body instanceof FieldDeclaration)
        {
            FieldDeclaration field = (FieldDeclaration)body;
            List<VariableDeclarationFragment> list = field.fragments();
            if (list != null && !list.isEmpty())
            {
                VariableDeclarationFragment variable = list.get(0);
                String dd = variable.getName().getFullyQualifiedName();
                TypeDeclaration type = (TypeDeclaration)field.getParent();
                return (type.getName().getFullyQualifiedName() + '.' + dd);
            }
            return null;
            
        }
        else if (body instanceof MethodDeclaration)
        {
            MethodDeclaration method = (MethodDeclaration)body;
            
            return method.getName().getFullyQualifiedName();
        }
        return null;
        
    }
}

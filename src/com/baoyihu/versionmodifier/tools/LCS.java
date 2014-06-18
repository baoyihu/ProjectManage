package com.baoyihu.versionmodifier.tools;

import java.util.ArrayList;
import java.util.List;

public class LCS<T extends ValueInterface>
{
    private List<T> listLeft = null;
    
    private List<T> listRight = null;
    
    public LCS(List<T> x, List<T> y)
    {
        listLeft = x;
        listRight = y;
    }
    
    public List<T> getResult()
    {
        Node<T> ret = lcsdyn(listLeft, listRight);
        return ret.ss;
    }
    
    class Node<P>
    {
        public Node<P> append(P data)
        {
            Node<P> node = new Node<P>();
            node.ss = new ArrayList<P>(this.ss);
            node.count = this.count;
            node.ss.add(data);
            node.count += 1;
            return node;
        }
        
        List<P> ss = new ArrayList<P>();
        
        int count = 0;
    }
    
    public Node<T> lcsdyn(List<T> x, List<T> y)
    {
        
        int i, j;
        int lenx = x.size();
        int leny = y.size();
        Node<T>[][] table = new Node[lenx + 1][leny + 1];
        
        for (i = 0; i <= lenx; i++)
            table[i][0] = new Node<T>();
        for (i = 0; i <= leny; i++)
            table[0][i] = new Node<T>();
        
        for (i = 1; i <= lenx; i++)
        {
            for (j = 1; j <= leny; j++)
            {
                
                if (x.get(i - 1).valueEquals(y.get(j - 1)))
                {
                    table[i][j] = table[i - 1][j - 1].append(x.get(i - 1));
                    
                }
                
                else
                {
                    table[i][j] = max(table[i][j - 1], table[i - 1][j]);
                }
            }
        }
        
        Node<T> max = table[lenx][leny];
        return max;
    }
    
    public List<T> find(T c, List<T> x)
    {
        List<T> ret = new ArrayList<T>();
        for (int i = 0; i < x.size(); i++)
        {
            if (c.valueEquals(x.get(i)))
            {
                ret.add(c);
                return ret;
            }
        }
        
        return ret;
    }
    
    public Node<T> max(Node<T> x, Node<T> y)
    {
        if (x.count > y.count)
            return x;
        else
            return y;
    }
}
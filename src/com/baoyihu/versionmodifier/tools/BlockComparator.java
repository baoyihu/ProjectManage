package com.baoyihu.versionmodifier.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockComparator<T extends ValueInterface>
{
    
    private final List<T> teamLeft;
    
    private final List<T> teamRight;
    
    private List<T> lagestCommonList;
    
    public BlockComparator(List<T> teamLeft, List<T> teamRight)
    {
        this.teamLeft = teamLeft;
        this.teamRight = teamRight;
    }
    
    public void compare()
    {
        LCS<T> lcs = new LCS<T>(teamLeft, teamRight);
        lagestCommonList = lcs.getResult();
    }
    
    public T findEqualItem(T target, List<T> teamLeft, List<T> otherTeam, boolean isEqual)
    {
        T ret = null;
        List<T> commonList = new ArrayList<T>();
        Iterator<T> iterator = teamLeft.iterator();
        Iterator<T> iterator1 = lagestCommonList.iterator();
        T preItem = iterator1.next();
        ret = preItem;
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            
            if (preItem.equals(temp))
            {
                if (preItem != null)
                {
                    commonList.add(preItem);
                    if (iterator1.hasNext())
                    {
                        preItem = iterator1.next();
                    }
                    else
                    {
                        preItem = null;
                    }
                }
            }
            if (temp.equals(target))
            {
                break;
            }
        }
        
        iterator = otherTeam.iterator();
        iterator1 = commonList.iterator();
        preItem = iterator1.next();
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            if (temp.valueEquals(preItem))
            {
                if (iterator1.hasNext())
                {
                    preItem = iterator1.next();
                }
                else
                {
                    ret = temp;
                    break;
                }
            }
        }
        return ret;
    }
    
    public T findNextItem(T target, List<T> teamLeft, List<T> otherTeam, boolean isEqual)
    {
        T ret = null;
        List<T> commonList = new ArrayList<T>();
        Iterator<T> iterator = teamLeft.iterator();
        Iterator<T> iterator1 = lagestCommonList.iterator();
        T preItem = iterator1.next();
        ret = preItem;
        commonList.add(preItem);
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            if (preItem.equals(temp))
            {
                if (iterator1.hasNext())
                {
                    preItem = iterator1.next();
                    commonList.add(preItem);
                }
            }
            
            if (temp.equals(target))
            {
                //                if (iterator1.hasNext())
                //                {
                //                    preItem = iterator1.next();
                //                    commonList.add(preItem);
                //                }
                break;
            }
            
        }
        
        iterator = otherTeam.iterator();
        iterator1 = commonList.iterator();
        preItem = iterator1.next();
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            if (temp.valueEquals(preItem))
            {
                if (iterator1.hasNext())
                {
                    preItem = iterator1.next();
                }
                else
                {
                    ret = temp;
                    break;
                }
            }
        }
        return ret;
    }
    
    public T findPreItem(T target, List<T> teamLeft, List<T> otherTeam, boolean isEqual)
    {
        T ret = null;
        List<T> commonList = new ArrayList<T>();
        Iterator<T> iterator = teamLeft.iterator();
        Iterator<T> iterator1 = lagestCommonList.iterator();
        T preItem = iterator1.next();
        ret = preItem;
        commonList.add(preItem);
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            if (temp.equals(target))
            {
                break;
            }
            if (preItem.equals(temp))
            {
                if (iterator1.hasNext())
                {
                    ret = preItem;//TODO it is not necessary
                    preItem = iterator1.next();
                    commonList.add(preItem);
                }
            }
        }
        
        iterator = otherTeam.iterator();
        iterator1 = commonList.iterator();
        preItem = iterator1.next();
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            if (temp.valueEquals(preItem))
            {
                if (iterator1.hasNext())
                {
                    ret = temp;
                    preItem = iterator1.next();
                }
                else
                {
                    break;
                }
            }
        }
        return ret;
    }
}

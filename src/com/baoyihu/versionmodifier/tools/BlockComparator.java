package com.baoyihu.versionmodifier.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockComparator<T extends ValueInterface>
{
    
    private final List<T> selfTeam;
    
    private final List<T> otherTeam;
    
    /**
     * the lagestCommonList contain the selfTeam and otherTeam which is value equals
     * but the LCS algorithm guarantee that all the node in lagestCommonList comes from selfTeam;
     * */
    private List<T> lagestCommonList;
    
    public BlockComparator(List<T> teamLeft, List<T> teamRight)
    {
        this.selfTeam = teamLeft;
        this.otherTeam = teamRight;
    }
    
    public void compare()
    {
        LCS<T> lcs = new LCS<T>(selfTeam, otherTeam);
        lagestCommonList = lcs.getResult();
    }
    
    /**
     * T target
     * List<T>
     * */
    private List<T> findSubCommonList(T target, List<T> teamLeft, List<T> largestCommon, int position)
    {
        T ret = null;
        List<T> commonList = new ArrayList<T>();
        Iterator<T> iterator = teamLeft.iterator();
        Iterator<T> common = largestCommon.iterator();
        T commonItem = common.next();
        while (iterator.hasNext() && commonItem != null)
        {
            T temp = iterator.next();
            if (commonItem.equals(temp))
            {
                commonList.add(commonItem);
                if (common.hasNext())
                {
                    commonItem = common.next();
                }
            }
            if (temp.equals(target))
            {
                if (position == 1)
                {
                    commonList.add(commonItem);
                    
                }
                ret = temp;
                break;
            }
        }
        if (position == 0)
        {
            if (ret == null)
            {
                commonList.clear();
            }
        }
        return commonList;
    }
    
    private T findLastMatchInLists(List<T> otherTeam, List<T> commonList)
    {
        T ret = null;
        Iterator<T> iterator = otherTeam.iterator();
        Iterator<T> common = commonList.iterator();
        T commonItem = common.next();
        while (iterator.hasNext())
        {
            T temp = iterator.next();
            if (temp.valueEquals(commonItem))
            {
                if (common.hasNext())
                {
                    commonItem = common.next();
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
    
    /**
     * the target must in teamSelf
     * */
    public T findEqualItem(T target, List<T> teamSelf, List<T> teamOther, boolean isEqual)
    {
        List<T> commonList = findSubCommonList(target, teamSelf, lagestCommonList, 0);
        if (commonList.isEmpty())
        {
            return null;
        }
        return findLastMatchInLists(teamOther, commonList);
    }
    
    /**
     * the target must in teamSelf
     * */
    public T findPreItem(T target, List<T> teamSelf, List<T> teamOther, boolean isEqual)
    {
        List<T> commonList = findSubCommonList(target, teamSelf, lagestCommonList, -1);
        
        return findLastMatchInLists(teamOther, commonList);
    }
    
    /**
     * the target must in teamSelf
     * */
    public T findNextItem(T target, List<T> teamSelf, List<T> teamOther, boolean isEqual)
    {
        List<T> commonList = findSubCommonList(target, teamSelf, lagestCommonList, 1);
        return findLastMatchInLists(teamOther, commonList);
    }
    
}

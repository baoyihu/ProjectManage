package com.baoyihu.versionmodifier.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Issue", strict = false)
public class Issue
{
    @Element(name = "name", required = true)
    String name;
    
    @Element(name = "use", required = true)
    boolean use;
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public boolean isUse()
    {
        return use;
    }
    
    public void setUse(boolean use)
    {
        this.use = use;
    }
}

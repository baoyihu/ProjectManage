package com.baoyihu.versionmodifier.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.PersistenceException;

import com.baoyihu.versionmodifier.tools.SerializerService;

@Root(name = "Version", strict = false)
public class VersionConfig
{
    @Element(name = "ActionDelete", required = false)
    private boolean actionDelete = false;
    
    @ElementList(name = "BuildFlag", required = false)
    private List<Issue> buildFlag;
    
    public List<Issue> getBuildFlag()
    {
        return buildFlag;
    }
    
    public void setBuildFlag(List<Issue> buildFlag)
    {
        this.buildFlag = buildFlag;
    }
    
    public boolean isActionDelete()
    {
        return actionDelete;
    }
    
    public void setActionDelete(boolean value)
    {
        actionDelete = value;
    }
    
    public List<String> getRemovedFlags()
    {
        List<String> ret = new ArrayList<String>();
        if (buildFlag != null)
        {
            for (Issue temp : buildFlag)
            {
                if (!temp.isUse())
                {
                    ret.add(temp.getName());
                }
            }
        }
        return ret;
    }
    
    public static VersionConfig readFromFile(String fileName)
    {
        FileInputStream stream = null;
        VersionConfig config = null;
        try
        {
            try
            {
                stream = new FileInputStream(fileName);
                config = SerializerService.fromStream(VersionConfig.class, stream);
            }
            finally
            {
                if (stream != null)
                {
                    stream.close();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return config;
    }
}

package com.baoyihu.versionmodifier.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.baoyihu.versionmodifier.model.VersionConfig;
import com.baoyihu.versionmodifier.tools.Utills;

public class Extractor
{
    public static final String ANNOTATION_IMPORT = "com.huawei.ott.annotation";
    
    public static final String NAME_SPACE = "http://schemas.android.com/apk/res/com.huawei.ott.etb";
    
    public static final String ANDROID_SPACE = "http://schemas.android.com/apk/res/android";
    
    private final List<String> removedProperties;
    
    private final boolean isDelete;
    
    private final String dir;
    
    private final List<String> removedClasses = new ArrayList<String>();
    
    public Extractor(String path, VersionConfig config)
    {
        List<String> removedFlags = config.getRemovedFlags();
        isDelete = config.isActionDelete();
        dir = path;
        removedProperties = removedFlags;
        
    }
    
    public void work()
    {
        modifyJava();
        System.out.println();
        modifyXML();
        System.out.println();
        removeUnusedImport();
        System.out.println();
        removeUnusedResource();
        System.out.println();
    }
    
    private void modifyJava()
    {
        if (isDelete)
        {
            try
            {
                List<String> classPaths = Utills.findAllClassPath(dir, new String[] {".java"});
                for (String temp : classPaths)
                {
                    ClassExtractor modifier = new ClassExtractor(removedProperties);
                    String ret = modifier.doJob(temp, dir);
                    if (ret != null)
                    {
                        removedClasses.add(ret);
                    }
                }
                System.out.println("ModifyJava finished!!!!");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("ActionType is not delete ,so no need to deal with java files!");
        }
    }
    
    private boolean modifyXML()
    {
        List<String> classPaths = Utills.findAllClassPath(dir, new String[] {".xml"});
        for (String temp : classPaths)
        {
            XmlExtractor dom = new XmlExtractor(temp, removedProperties, NAME_SPACE);
            dom.doJob(isDelete);
        }
        System.out.println("Modify XML finished!!!!");
        return true;
    }
    
    private void removeUnusedImport()
    {
        if (isDelete)
        {
            List<String> classPaths = Utills.findAllClassPath(dir, new String[] {".java"});
            for (String temp : classPaths)
            {
                ClassExtractor modifier = new ClassExtractor(removedProperties);
                modifier.removeImport(temp, removedClasses);
                
            }
            System.out.println("Remove Important finished!!!!");
        }
        else
        {
            System.out.println("ActionType is not delete ,so no need to deal with java files!");
        }
    }
    
    private void removeUnusedResource()
    {
        List<String> pictureList = Utills.findAllClassPath(dir, new String[] {".9.png", ".png", ".bmp", "jpg", "gif"});
        Map<String, List<String>> pictureMap = new HashMap<String, List<String>>();
        for (String temp : pictureList)
        {
            String name = temp.substring(temp.lastIndexOf('\\') + 1);
            name = name.substring(0, name.indexOf('.'));
            if (name != null)
            {
                if (!pictureMap.containsKey(name))
                {
                    pictureMap.put(name, new ArrayList<String>());
                }
                pictureMap.get(name).add(temp);
            }
        }
        
        List<String> classPaths = Utills.findAllClassPath(dir, new String[] {".java"});
        List<String> xmlPaths = Utills.findAllClassPath(dir, new String[] {".xml"});
        filterMapWithFile(pictureMap, "R.drawable.", classPaths);
        filterMapWithFile(pictureMap, "R.raw.", classPaths);
        filterMapWithFile(pictureMap, "@drawable/", xmlPaths);
        filterMapWithFile(pictureMap, "@raw/", xmlPaths);
        for (Entry<String, List<String>> entry : pictureMap.entrySet())
        {
            for (String filePath : entry.getValue())
            {
                new File(filePath).delete();
                System.out.println("deal end :" + filePath + " Removed!");
            }
        }
        System.out.println("Remove Unused Resource finished!!!!");
    }
    
    private void filterMapWithFile(Map<String, List<String>> pictureMap, String preFix, List<String> classPaths)
    {
        for (String temp : classPaths)
        {
            String source = Utills.readFile(temp);
            Iterator<Map.Entry<String, List<String>>> it = pictureMap.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String, List<String>> entry = it.next();
                String flag = preFix + entry.getKey();
                if (source.contains(flag))
                {
                    it.remove();
                }
            }
        }
    }
}
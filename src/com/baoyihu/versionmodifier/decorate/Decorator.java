package com.baoyihu.versionmodifier.decorate;

import java.util.ArrayList;
import java.util.List;

import com.baoyihu.versionmodifier.model.VersionConfig;
import com.baoyihu.versionmodifier.tools.Utills;

public class Decorator
{
    private final String sourceDir;
    
    private final String destDir;
    
    private final VersionConfig configFile;
    
    private static final String ANDROID_SPACE_TAB = "android:visibility";
    
    public static final String NAME_SPACE = "http://schemas.android.com/apk/res/com.huawei.ott.etb";
    
    private List<String> addedFlags;
    
    private final static boolean DEBUG = true;
    
    public Decorator(String source, String dest, VersionConfig config)
    {
        sourceDir = source;
        destDir = dest;
        configFile = config;
        if (DEBUG)
        {
            addedFlags = new ArrayList<String>();
            addedFlags.add("multiProfile");
        }
        else
        {
            List<String> addedFlags = config.getRemovedFlags();
        }
    }
    
    public void work()
    {
        modifyJava();
        System.out.println();
        modifyXML();
        System.out.println();
        //addUsedImport();
        System.out.println();
        //addUsedResource();
        System.out.println();
    }
    
    private void modifyJava()
    {
        //  if (isDelete)
        {
            // try
            {
                List<String> classPaths = Utills.findAllClassPath(sourceDir, new String[] {".java"});
                for (String temp : classPaths)
                {
                    ClassDecorator modifier = new ClassDecorator(addedFlags);
                    String ret = modifier.doJob(temp, temp.replace(sourceDir, destDir));
                }
                System.out.println("ModifyJava finished!!!!");
            }
            //            catch (IOException e)
            //            {
            //                e.printStackTrace();
            //            }
        }
        //                else
        //                {
        //                    System.out.println("ActionType is not delete ,so no need to deal with java files!");
        //                }
    }
    
    private boolean modifyXML()
    {
        List<String> classPaths = Utills.findAllClassPath(sourceDir, new String[] {".xml"});
        for (String sourceFile : classPaths)
        {
            String targetFile = sourceFile.replace(sourceDir, destDir);
            XmlInsertor dom = new XmlInsertor(sourceFile, targetFile, addedFlags, NAME_SPACE);
            dom.doJob();
        }
        System.out.println("Modify XML finished!!!!");
        return true;
    }
}

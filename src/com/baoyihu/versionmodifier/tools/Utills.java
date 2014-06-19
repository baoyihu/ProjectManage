package com.baoyihu.versionmodifier.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class Utills
{
    public static final String SPLITE_LINE = "\r\n";
    
    private static final char SPLITE = '\\';
    
    public static boolean createDirs(String dir)
    {
        File file = new File(dir);
        if (!file.exists())
        {
            file.mkdirs();
        }
        return true;
    }
    
    public enum MyColor
    {
        RED, BLUE, BLACK
    }
    
    public static void printColor(String data, MyColor color, boolean newLine)
    {
        String value = null;
        switch (color)
        {
            case RED:
                value = "/e[31m " + "Hello World." + "/e[0m";
                break;
            case BLUE:
                
            case BLACK:
            default:
                
        }
        if (newLine)
        {
            value = value + SPLITE_LINE;
        }
        
    }
    
    public static boolean deleteDirIfEmpty(String dir)
    {
        File file = new File(dir);
        if (file.exists() && file.list().length == 0)
        {
            if (file.delete())
            {
                String parent = dir.substring(0, dir.lastIndexOf(SPLITE));
                return deleteDirIfEmpty(parent);
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    public static boolean copyFile(String source, String dest)
    {
        String buffer = readFile(source);
        return writeFiles(dest, buffer);
        
    }
    
    public static boolean writeFiles(String name, String buffer)
    {
        String dir = name.substring(0, name.lastIndexOf(SPLITE));
        createDirs(dir);
        File file = new File(name);
        if (buffer == null)
        {
            file.delete();
            return true;
        }
        OutputStreamWriter writer = null;
        try
        {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(buffer);
            writer.flush();
            return true;
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
            catch (IOException e)
            {
                System.out.println(e.toString());
            }
        }
        return false;
    }
    
    public static String readFile(String name)
    {
        File file = new File(name);
        if (!file.exists())
            return null;
        
        InputStreamReader reader = null;
        try
        {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            char[] buffer = new char[1024];
            int count = 0;
            StringBuilder builder = new StringBuilder();
            while ((count = reader.read(buffer, 0, 1024)) > 0)
            {
                builder.append(buffer, 0, count);
            }
            return builder.toString();
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
            }
            catch (IOException e)
            {
                System.out.println(e.toString());
            }
        }
        return null;
    }
    
    public static List<String> findAllClassPath(String dirPath, String[] typeArray)
    {
        List<String> ret = new ArrayList<String>();
        
        if (dirPath == null)
            return ret;
        
        if (!dirPath.endsWith("\\"))
        {
            dirPath = dirPath + SPLITE;
        }
        
        File dirFile = new File(dirPath);
        String[] filesInDir = dirFile.list();
        for (String tempString : filesInDir)
        {
            File tempFile = new File(dirPath + tempString);
            if (tempFile.isDirectory())
            {
                ret.addAll(findAllClassPath(dirPath + tempString + SPLITE, typeArray));
            }
            else
            {
                if (endWithIgnoreCase(tempString, typeArray))
                {
                    ret.add(dirPath + tempString);
                }
            }
        }
        
        return ret;
    }
    
    private static boolean endWithIgnoreCase(String str, String[] typeArray)
    {
        if (typeArray == null || typeArray.length == 0 || str == null)
        {
            return false;
        }
        for (String flag : typeArray)
        {
            int len = flag.length();
            if (str.length() >= len)
            {
                String temp = str.substring(str.length() - len);
                boolean ret = temp.equalsIgnoreCase(flag);
                if (ret)
                {
                    return true;
                }
            }
        }
        return false;
        
    }
    
}

package com.baoyihu.versionmodifier;

import com.baoyihu.versionmodifier.decorate.Decorator;
import com.baoyihu.versionmodifier.extract.Extractor;
import com.baoyihu.versionmodifier.model.VersionConfig;

public class AstAnalyzer
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Parameter is wrong!!!!");
            return;
        }
        
        //        if (2 > 1)
        //        {
        //            
        //            //LCS.main(null);
        //            BlockComparator2.test();
        //            return;
        //        }
        String action = args[0];
        if (action.equalsIgnoreCase("get"))
        {
            if (args.length != 3)
            {
                System.out.println("Parameter is wrong!!!!");
                return;
            }
            String dir = args[1];
            VersionConfig config = VersionConfig.readFromFile(args[2]);
            
            Extractor analyzer = new Extractor(dir, config);
            analyzer.work();
            System.out.printf("All work finished!!!!");
        }
        else if (action.equalsIgnoreCase("put"))
        {
            if (args.length != 4)
            {
                System.out.println("Parameter is wrong!!!!");
                return;
            }
            String sourceDir = args[1];
            String destDir = args[2];
            VersionConfig config = VersionConfig.readFromFile(args[3]);
            
            Decorator decorator = new Decorator(sourceDir, destDir, config);
            decorator.work();
            System.out.printf("All work finished!!!!");
        }
    }
}

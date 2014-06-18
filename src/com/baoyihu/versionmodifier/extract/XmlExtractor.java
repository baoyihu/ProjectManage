package com.baoyihu.versionmodifier.extract;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.baoyihu.versionmodifier.tools.Utills;

public class XmlExtractor
{
    private String file = null;
    
    private String space = null;
    
    private List<String> removedProperties = null;
    
    private static final String ANDROID_SPACE_TAB = "android:visibility";
    
    private static final int PARSE_STATE_NO_PROPERTY = 0;
    
    private static final int PARSE_STATE_NOT_CHANGED = 1;
    
    private static final int PARSE_STATE_CHANGED = 2;
    
    private static final int PARSE_STATE_DELETED = 3;
    
    public XmlExtractor(String fileName, List<String> flags, String workSpace)
    {
        file = fileName;
        space = workSpace;
        removedProperties = flags;
    }
    
    private Document doc;
    
    public void doJob(boolean isDelete)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        
        try
        {
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();
            
            doc = db.parse(new File(file));
            Element rootElement = doc.getDocumentElement();
            int changed = parseNode(rootElement, isDelete);
            if (changed == PARSE_STATE_CHANGED)
            {
                printXML(rootElement);
                System.out.println("File:" + file + "  Changed!!!");
            }
            else if (changed == PARSE_STATE_NOT_CHANGED)
            {
                System.out.println("File:" + file + "  Ignored!!!");
            }
            else if (changed == PARSE_STATE_DELETED)
            {
                Utills.writeFiles(file, null);
                String dir = file.substring(0, file.lastIndexOf('\\'));
                Utills.deleteDirIfEmpty(dir);
            }
        }
        catch (SAXException | IOException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e1)
        {
            e1.printStackTrace();
        }
        catch (TransformerException e2)
        {
            e2.printStackTrace();
        }
    }
    
    private boolean printXML(Element rootElement)
        throws TransformerException
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        Source xmlSource = new DOMSource(rootElement);
        Result outputTarget = new StreamResult(new File(file));
        
        transformer.transform(xmlSource, outputTarget);
        return true;
        
    }
    
    private int parseNode(Node rootElement, boolean isDelete)
    {
        int ret = PARSE_STATE_NO_PROPERTY;
        if (rootElement.getNodeType() != Node.ELEMENT_NODE)
        {
            return ret;
        }
        ret = dealWithNode(rootElement, isDelete);
        if (ret > PARSE_STATE_NO_PROPERTY)
        {
            return ret;
        }
        Node node = rootElement.getFirstChild();
        
        while (node != null)
        {
            int removed = parseNode(node, isDelete);
            Node nextNode = node.getNextSibling();
            if (removed > ret)
            {
                ret = removed;
                if (isDelete && ret == PARSE_STATE_DELETED)
                {
                    ret = PARSE_STATE_CHANGED;
                    rootElement.removeChild(node);
                }
                
            }
            node = nextNode;
        }
        
        return ret;
        
    }
    
    private int dealWithNode(Node rootElement, boolean isDelete)
    {
        int ret = PARSE_STATE_NO_PROPERTY;
        NamedNodeMap map = rootElement.getAttributes();
        Node whenNode = map.getNamedItemNS(space, "when");
        if (whenNode == null)
        {
            return ret;
        }
        String property = whenNode.getTextContent();
        if (removedProperties.contains(property))
        {
            if (!isDelete)
            {
                Node visibleNode = map.getNamedItemNS(Extractor.ANDROID_SPACE, "visibility");
                if (visibleNode == null)
                {
                    Attr attr = rootElement.getOwnerDocument().createAttribute(ANDROID_SPACE_TAB);
                    attr.setValue("gone");
                    map.setNamedItem(attr);
                    ret = PARSE_STATE_CHANGED;
                }
                else
                {
                    if (visibleNode.getTextContent().equals("gone"))
                    {
                        ret = PARSE_STATE_NOT_CHANGED;
                    }
                    else
                    {
                        visibleNode.setTextContent("gone");
                        ret = PARSE_STATE_CHANGED;
                    }
                }
            }
            else
            {
                ret = PARSE_STATE_DELETED;
            }
            
        }
        
        return ret;
    }
}

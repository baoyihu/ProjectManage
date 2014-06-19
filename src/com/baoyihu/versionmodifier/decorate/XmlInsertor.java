package com.baoyihu.versionmodifier.decorate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.baoyihu.versionmodifier.tools.BlockComparator;
import com.baoyihu.versionmodifier.tools.Utills;
import com.baoyihu.versionmodifier.tools.ValueInterface;

public class XmlInsertor
{
    private String sourceFile = null;
    
    private String targetFile = null;
    
    private String space = null;
    
    private List<String> addedProperties = null;
    
    private static final String ANDROID_SPACE_TAB = "android:visibility";
    
    private static final int PARSE_STATE_NO_PROPERTY = 0;
    
    private static final int PARSE_STATE_NOT_CHANGED = 1;
    
    private static final int PARSE_STATE_CHANGED = 2;
    
    private static final int PARSE_STATE_APPEND = 3;
    
    public XmlInsertor(String sourceFileName, String targetFileName, List<String> flags, String workSpace)
    {
        this.sourceFile = sourceFileName;
        this.targetFile = targetFileName;
        space = workSpace;
        addedProperties = flags;
    }
    
    private Document doc;
    
    public void doJob()
    {
        try
        {
            Element sourceRoot = null;
            Element targetRoot = null;
            String sourceStr = Utills.readFile(sourceFile);
            String targetStr = Utills.readFile(targetFile);
            if (targetStr != null && sourceStr.equals(targetStr))
            {
                return;
            }
            sourceRoot = fileToElement(sourceFile);
            targetRoot = fileToElement(targetFile);
            //            if (sourceFile.contains("more_main"))
            //            {
            //                System.out.println("xml:" + sourceFile);
            //            }
            int changed = parseNode(sourceRoot, targetRoot);
            if (changed == PARSE_STATE_CHANGED)
            {
                printXML(targetRoot, sourceFile, targetFile);
                System.out.println("File:" + sourceFile + "  Changed!!!");
            }
            else if (changed == PARSE_STATE_NOT_CHANGED)
            {
                System.out.println("File:" + sourceFile + "  Ignored!!!");
            }
            else if (changed == PARSE_STATE_APPEND)
            {
                Utills.copyFile(sourceFile, targetFile);
            }
        }
        catch (TransformerException e2)
        {
            e2.printStackTrace();
        }
    }
    
    private boolean printXML(Element rootElement, String source, String dest)
        throws TransformerException
    {
        if (rootElement != null)
        {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            Source xmlSource = new DOMSource(rootElement);
            Result outputTarget = new StreamResult(new File(dest));
            transformer.transform(xmlSource, outputTarget);
        }
        else
        {
            Utills.copyFile(source, dest);
        }
        return true;
        
    }
    
    private String elementToString(Element rootElement)
        throws TransformerException
    {
        if (rootElement != null)
        {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            Source xmlSource = new DOMSource(rootElement);
            Result outputTarget = new StreamResult(new java.io.StringWriter());
            transformer.transform(xmlSource, outputTarget);
            return outputTarget.toString();
        }
        else
        {
            return "";
        }
    }
    
    private Element fileToElement(String fileName)
    {
        File file = new File(fileName);
        if (!file.exists())
        {
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Element sourceRoot = null;
        try
        {
            
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();
            doc = db.parse(file);
            sourceRoot = doc.getDocumentElement();
        }
        catch (SAXException | IOException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e1)
        {
            e1.printStackTrace();
        }
        
        return sourceRoot;
    }
    
    private Element stringToElement(String input)
    {
        if (input == null)
        {
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Element sourceRoot = null;
        try
        {
            
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();
            //   doc = db.parse(input);
            doc = db.parse(new ByteArrayInputStream(input.getBytes()));
            
            sourceRoot = doc.getDocumentElement();
        }
        catch (SAXException | IOException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e1)
        {
            e1.printStackTrace();
        }
        
        return sourceRoot;
    }
    
    private int parseNode(Node sourceRoot, Node targetRoot)
    {
        int ret = PARSE_STATE_NO_PROPERTY;
        
        if (sourceRoot.getNodeType() != Node.ELEMENT_NODE)
        {
            return ret;
        }
        ret = dealWithNode(sourceRoot);
        if (ret > PARSE_STATE_NO_PROPERTY)
        {
            return ret;
        }
        Node node = sourceRoot.getFirstChild();
        
        if (targetRoot == null)
        {
            return ret;
        }
        if (sourceRoot.getLocalName() != targetRoot.getLocalName())
        {
            // this is for we can not solve the getMatchedElement error,when insert node to List;
            return ret;
        }
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (node.getLocalName().equals("application"))
                {
                    int i = 9;
                    i++;
                }
                Node dd = node.getAttributes().getNamedItem("android:id");
                if (dd != null)
                {
                    if (dd.getTextContent().equals("@+id/favorites_layout"))
                    {
                        int ii = 9;
                        ii++;
                    }
                }
                Node targetNode = getMatchedElement(sourceRoot, targetRoot, node);
                if (targetNode == null)
                {
                    // dealWithNode(sourceRoot);
                }
                else
                {
                    int append = parseNode(node, targetNode);
                    if (append > ret)
                    {
                        ret = append;
                        if (ret == PARSE_STATE_APPEND)
                        {
                            ret = PARSE_STATE_CHANGED;
                            if (targetRoot != null)
                            {
                                Node preNode = getInsertPreNode(sourceRoot, targetRoot, node);
                                Document document = preNode.getOwnerDocument();
                                Node newNode = document.adoptNode(node.cloneNode(true));
                                targetRoot.insertBefore(newNode, preNode);
                            }
                        }
                    }
                    
                }
            }
            node = node.getNextSibling();
        }
        
        return ret;
    }
    
    private Node getInsertPreNode(Node sourceRoot, Node targetRoot, Node child)
    {
        List<XmlWrap> list1 = wrap(sourceRoot);
        List<XmlWrap> list2 = wrap(targetRoot);
        XmlWrap theNode = null;
        for (XmlWrap node : list1)
        {
            if (node.innerData == child)
            {
                theNode = node;
                break;
            }
        }
        if (theNode != null)
        {
            BlockComparator<XmlWrap> comparator = new BlockComparator<XmlWrap>(list1, list2);
            comparator.compare();
            XmlWrap preNode = comparator.findNextItem(theNode, list1, list2, true);
            return preNode.innerData;
        }
        else
        {
            return targetRoot.getFirstChild();
        }
        
    }
    
    class XmlWrap implements ValueInterface
    {
        
        @Override
        public boolean valueEquals(ValueInterface other)
        {
            if (other instanceof XmlWrap)
            {
                XmlWrap otherNode = (XmlWrap)other;
                return (nodeEquals(otherNode.innerData, innerData));
            }
            return false;
        }
        
        Node innerData = null;
        
        int start = 0;
        
        public XmlWrap(Node data, int start)
        {
            this.innerData = data;
            this.start = start;
        }
        
        public int getStartPosition()
        {
            return start;
        }
        
        public int getLength()
        {
            return innerData.toString().length();
        }
        
        public int getEnd()
        {
            return start + getLength();
        }
        
        @Override
        public String toString()
        {
            if (innerData != null)
            {
                return innerData.toString();
            }
            else
            {
                return "innerData is null";
            }
        }
        
        @Override
        public int hashCode()
        {
            return innerData.hashCode();
        }
        
    }
    
    public List<XmlWrap> wrap(Node parent)
    {
        List<XmlWrap> ret = new ArrayList<XmlWrap>();
        int index = 0;
        Node temp = parent.getFirstChild();
        while (temp != null)
        {
            XmlWrap wrapNode = this.new XmlWrap(temp, index);
            index += temp.toString().length() + 2;//for \r\n;
            ret.add(wrapNode);
            temp = temp.getNextSibling();
        }
        return ret;
    }
    
    private Node getMatchedElement(Node sourceRoot, Node targetRoot, Node child)
    {
        
        List<XmlWrap> list1 = wrap(sourceRoot);
        List<XmlWrap> list2 = wrap(targetRoot);
        if (list1.isEmpty() || list2.isEmpty())
        {
            // this is for the error of getMatchedElement when we insert node to list
            return targetRoot.getFirstChild();
        }
        XmlWrap theNode = null;
        for (XmlWrap node : list1)
        {
            if (node.innerData == child)
            {
                theNode = node;
                break;
            }
        }
        if (theNode != null)
        {
            BlockComparator<XmlWrap> comparator = new BlockComparator<XmlWrap>(list1, list2);
            comparator.compare();
            XmlWrap preNode = comparator.findEqualItem(theNode, list1, list2, true);
            if (preNode != null)
            {
                return preNode.innerData;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
        
    }
    
    private boolean nodeEquals(Node thisNode, Node thatNode)
    {
        boolean ret = false;
        String thisName = thisNode.getLocalName();
        String thatName = thatNode.getLocalName();
        if (thisName != null && thatName != null && thisName.equals(thatName))
        {
            Node node1 = thisNode.getAttributes().getNamedItem("android:id");
            Node node2 = thatNode.getAttributes().getNamedItem("android:id");
            if (node1 != null && node2 != null)
            {
                if (node1.getTextContent().equals(node2.getTextContent()))
                {
                    ret = true;
                }
            }
            else
            {
                node1 = thisNode.getAttributes().getNamedItem("android:name");
                node2 = thatNode.getAttributes().getNamedItem("android:name");
                if (node1 != null && node2 != null)
                {
                    if (node1.getTextContent().equals(node2.getTextContent()))
                    {
                        ret = true;
                    }
                }
                else if (node1 == null && node2 == null)
                {
                    ret = true;
                }
            }
        }
        return ret;
    }
    
    private int dealWithNode(Node sourceElement)
    {
        int ret = PARSE_STATE_NO_PROPERTY;
        NamedNodeMap map = sourceElement.getAttributes();
        Node whenNode = map.getNamedItemNS(space, "when");
        if (whenNode == null)
        {
            return ret;
        }
        String property = whenNode.getTextContent();
        if (addedProperties.contains(property))
        {
            // if (!isDelete)
            //            {
            //                Node visibleNode = map.getNamedItemNS(Extractor.ANDROID_SPACE, "visibility");
            //                if (visibleNode == null)
            //                {
            //                    Attr attr = rootElement.getOwnerDocument().createAttribute(ANDROID_SPACE_TAB);
            //                    attr.setValue("gone");
            //                    map.setNamedItem(attr);
            //                    ret = PARSE_STATE_CHANGED;
            //                }
            //                else
            //                {
            //                    if (visibleNode.getTextContent().equals("gone"))
            //                    {
            //                        ret = PARSE_STATE_NOT_CHANGED;
            //                    }
            //                    else
            //                    {
            //                        visibleNode.setTextContent("gone");
            //                        ret = PARSE_STATE_CHANGED;
            //                    }
            //                }
            //            }
            //            else
            {
                ret = PARSE_STATE_APPEND;
            }
            
        }
        
        return ret;
    }
}
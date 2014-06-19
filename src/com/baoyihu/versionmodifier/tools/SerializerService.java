package com.baoyihu.versionmodifier.tools;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public final class SerializerService
{
    private static Serializer xmlSerializer;
    static
    {
        Strategy strategy = new AnnotationStrategy();
        xmlSerializer = new Persister(strategy, new Matcher()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Transform match(Class type)
            {
                if (type.equals(boolean.class) || type.equals(Boolean.class))
                {
                    return new Transform<Boolean>()
                    {
                        
                        @Override
                        public Boolean read(String value)
                        {
                            if (value == null)
                            {
                                return Boolean.FALSE;
                            }
                            
                            if (value.equals("1"))
                            {
                                return Boolean.TRUE;
                            }
                            
                            return Boolean.valueOf(value);
                        }
                        
                        @Override
                        public String write(Boolean value)
                        {
                            return value ? "1" : "0";
                        }
                    };
                }
                return null;
            }
            
        });
    }
    
    private SerializerService()
    {
        
    }
    
    public static <T> byte[] toXml(final T object)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] retBytes = null;
        if (outputStream != null)
        {
            try
            {
                try
                {
                    xmlSerializer.write(object, outputStream);
                    retBytes = outputStream.toByteArray();
                }
                finally
                {
                    outputStream.close();
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return retBytes;
        
    }
    
    public static <T> T fromStream(final Class<T> klass, InputStream stream)
        throws PersistenceException
    {
        try
        {
            return xmlSerializer.read(klass, stream, false);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e, "all parseException to psersistence");
        }
        
    }
    
    public static <T> T fromXml(final Class<T> klass, final String xml)
        throws PersistenceException
    {
        try
        {
            return xmlSerializer.read(klass, xml, false);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e, "all parseException to psersistence");
        }
        
    }
}

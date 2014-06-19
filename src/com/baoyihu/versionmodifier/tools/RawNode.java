package com.baoyihu.versionmodifier.tools;

class RawNode<T extends ValueInterface>
{
    private byte flag;
    
    private T key;
    
    private T preKey;
    
    public T getPreKey()
    {
        return preKey;
    }
    
    public void setPreKey(T preKey)
    {
        this.preKey = preKey;
    }
    
    private Integer singlePower;
    
    private Integer power;
    
    public boolean getFlag(byte bit)
    {
        return (byte)(flag & bit) > 0;
    }
    
    public void setFlag(byte bit)
    {
        
        this.flag = (byte)(this.flag | bit);
    }
    
    public void clearFlag(byte bit)
    {
        this.flag = (byte)(this.flag & ~bit);
    }
    
    public T getKey()
    {
        return key;
    }
    
    public void setKey(T key)
    {
        this.key = key;
    }
    
    public Integer getSinglePowr()
    {
        return singlePower;
    }
    
    public void setSinglePowr(Integer singlePowr)
    {
        this.singlePower = singlePowr;
    }
    
    public Integer getPower()
    {
        return power;
    }
    
    public void setPower(Integer power)
    {
        this.power = power;
    }
    
    public RawNode(T key, T preKey, Integer singlePower)
    {
        this.key = key;
        this.preKey = preKey;
        this.singlePower = singlePower;
    }
    
    @Override
    public int hashCode()
    {
        return key.hashCode();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        
        if (obj instanceof RawNode)
        {
            RawNode<T> theOther = (RawNode<T>)obj;
            return theOther.key.valueEquals(this.key);
        }
        else
        {
            return false;
        }
        
    }
}

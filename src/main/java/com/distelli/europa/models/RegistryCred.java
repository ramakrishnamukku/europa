package com.distelli.europa.models;

import java.util.Arrays;
import java.util.Objects;
import com.distelli.europa.util.ObjectComparator;

public class RegistryCred implements Comparable
{
    protected Long created = null;
    protected String key = null;
    protected RegistryProvider provider = null;
    protected String region = null;
    protected String secret = null;

    public RegistryCred()
    {

    }

    public void setCreated(Long created)
    {
        this.created = created;
    }

    public Long getCreated()
    {
        return this.created;
    }

    public RegistryCred withCreated(Long created)
    {
        this.created = created;
        return this;
    }

    public void setCreated_asStr(String created)
    {
        this.created = toLong(created);
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return this.key;
    }

    public RegistryCred withKey(String key)
    {
        this.key = key;
        return this;
    }

    public void setProvider(RegistryProvider provider)
    {
        this.provider = provider;
    }

    public RegistryProvider getProvider()
    {
        return this.provider;
    }

    public RegistryCred withProvider(RegistryProvider provider)
    {
        this.provider = provider;
        return this;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getRegion()
    {
        return this.region;
    }

    public RegistryCred withRegion(String region)
    {
        this.region = region;
        return this;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }

    public String getSecret()
    {
        return this.secret;
    }

    public RegistryCred withSecret(String secret)
    {
        this.secret = secret;
        return this;
    }

    private Integer toInt(String value)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    private Double toDouble(String value)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    private Long toLong(String value)
    {
        try
        {
            return Long.parseLong(value);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    private Boolean toBoolean(String value)
    {
        try
        {
            return Boolean.parseBoolean(value);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("RegistryCred[created=%s, key=%s, provider=%s, region=%s, secret=%s]", created, key, provider, region, secret);
    }
    @Override
    public boolean equals(Object obj) {
        if ( null == obj || ! getClass().equals(obj.getClass()) ) return false;
        RegistryCred other = (RegistryCred)obj;
        return Objects.deepEquals(created, other.created) &&
            Objects.deepEquals(key, other.key) &&
            Objects.deepEquals(provider, other.provider) &&
            Objects.deepEquals(region, other.region) &&
            Objects.deepEquals(secret, other.secret);
    }

    @Override
    public int compareTo(Object obj) {
        int res;
        if ( null == obj ) return 1;
        String a = getClass().getName();
        String b = obj.getClass().getName();
        if ( ! a.equals(b) ) return a.compareTo(b);
        RegistryCred other = (RegistryCred)obj;
        res = ObjectComparator.compareTo(created, other.created);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(key, other.key);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(provider, other.provider);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(region, other.region);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(secret, other.secret);
        if ( 0 != res ) return res;
        return 0;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{created, key, provider, region, secret});
    }

}

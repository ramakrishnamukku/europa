package com.distelli.europa.models;

import java.util.Arrays;
import java.util.Objects;
import com.distelli.europa.util.*;

public class ContainerRepo implements Comparable
{
    protected String name = null;
    protected RegistryProvider provider = null;
    protected String region = null;

    public ContainerRepo()
    {

    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public ContainerRepo withName(String name)
    {
        this.name = name;
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

    public ContainerRepo withProvider(RegistryProvider provider)
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

    public ContainerRepo withRegion(String region)
    {
        this.region = region;
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
        return String.format("ContainerRepo[name=%s, provider=%s, region=%s]", name, provider, region);
    }
    @Override
    public boolean equals(Object obj) {
        if ( null == obj || ! getClass().equals(obj.getClass()) ) return false;
        ContainerRepo other = (ContainerRepo)obj;
        return Objects.deepEquals(name, other.name) &&
            Objects.deepEquals(provider, other.provider) &&
            Objects.deepEquals(region, other.region);
    }

    @Override
    public int compareTo(Object obj) {
        int res;
        if ( null == obj ) return 1;
        String a = getClass().getName();
        String b = obj.getClass().getName();
        if ( ! a.equals(b) ) return a.compareTo(b);
        ContainerRepo other = (ContainerRepo)obj;
        res = ObjectComparator.compareTo(name, other.name);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(provider, other.provider);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(region, other.region);
        if ( 0 != res ) return res;
        return 0;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{name, provider, region});
    }

}

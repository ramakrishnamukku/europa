package com.distelli.europa.models;

import com.distelli.europa.util.ObjectComparator;
import java.util.Arrays;
import java.util.Objects;

public class Notification implements Comparable
{
    protected String id = null;
    protected String region = null;
    protected String repoName = null;
    protected RegistryProvider repoProvider = null;
    protected String secret = null;
    protected String target = null;
    protected NotificationType type = null;

    public Notification()
    {

    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public Notification withId(String id)
    {
        this.id = id;
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

    public Notification withRegion(String region)
    {
        this.region = region;
        return this;
    }

    public void setRepoName(String repoName)
    {
        this.repoName = repoName;
    }

    public String getRepoName()
    {
        return this.repoName;
    }

    public Notification withRepoName(String repoName)
    {
        this.repoName = repoName;
        return this;
    }

    public void setRepoProvider(RegistryProvider repoProvider)
    {
        this.repoProvider = repoProvider;
    }

    public RegistryProvider getRepoProvider()
    {
        return this.repoProvider;
    }

    public Notification withRepoProvider(RegistryProvider repoProvider)
    {
        this.repoProvider = repoProvider;
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

    public Notification withSecret(String secret)
    {
        this.secret = secret;
        return this;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getTarget()
    {
        return this.target;
    }

    public Notification withTarget(String target)
    {
        this.target = target;
        return this;
    }

    public void setType(NotificationType type)
    {
        this.type = type;
    }

    public NotificationType getType()
    {
        return this.type;
    }

    public Notification withType(NotificationType type)
    {
        this.type = type;
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
        return String.format("Notification[id=%s, region=%s, repoName=%s, repoProvider=%s, secret=%s, target=%s, type=%s]", id, region, repoName, repoProvider, secret, target, type);
    }
    @Override
    public boolean equals(Object obj) {
        if ( null == obj || ! getClass().equals(obj.getClass()) ) return false;
        Notification other = (Notification)obj;
        return Objects.deepEquals(id, other.id) &&
            Objects.deepEquals(region, other.region) &&
            Objects.deepEquals(repoName, other.repoName) &&
            Objects.deepEquals(repoProvider, other.repoProvider) &&
            Objects.deepEquals(secret, other.secret) &&
            Objects.deepEquals(target, other.target) &&
            Objects.deepEquals(type, other.type);
    }

    @Override
    public int compareTo(Object obj) {
        int res;
        if ( null == obj ) return 1;
        String a = getClass().getName();
        String b = obj.getClass().getName();
        if ( ! a.equals(b) ) return a.compareTo(b);
        Notification other = (Notification)obj;
        res = ObjectComparator.compareTo(id, other.id);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(region, other.region);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(repoName, other.repoName);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(repoProvider, other.repoProvider);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(secret, other.secret);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(target, other.target);
        if ( 0 != res ) return res;
        res = ObjectComparator.compareTo(type, other.type);
        if ( 0 != res ) return res;
        return 0;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{id, region, repoName, repoProvider, secret, target, type});
    }

}

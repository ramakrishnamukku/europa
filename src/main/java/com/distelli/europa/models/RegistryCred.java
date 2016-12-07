package com.distelli.europa.models;

import lombok.*;

@Data
public class RegistryCred
{
    protected Long created = null;
    protected String description = null;
    protected String key = null;
    protected String name = null;
    protected RegistryProvider provider = null;
    protected String region = null;
    protected String secret = null;

    @Builder
    public RegistryCred()
    {

    }
}

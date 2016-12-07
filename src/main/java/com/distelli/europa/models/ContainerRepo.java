package com.distelli.europa.models;

import lombok.*;

@Data
public class ContainerRepo
{
    protected String name = null;
    protected RegistryProvider provider = null;
    protected String region = null;

    @Builder
    public ContainerRepo()
    {

    }
}

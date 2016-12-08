package com.distelli.europa.models;

import lombok.*;

@Data
@Builder
public class ContainerRepo
{
    protected String name = null;
    protected String credName = null;
    protected RegistryProvider provider = null;
    protected String region = null;
}

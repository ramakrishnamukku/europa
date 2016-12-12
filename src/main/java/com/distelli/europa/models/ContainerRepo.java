package com.distelli.europa.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerRepo
{
    protected String domain = null;
    protected String id = null;
    protected String name = null;
    protected String credId = null;
    protected String region = null;
    protected RegistryProvider provider = null;
}

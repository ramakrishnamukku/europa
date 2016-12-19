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
    //This is the AWS Account for ECR registries
    protected String registryId = null;
    //This is the repoUri that can be used for docker push / pull operations
    protected String repoUri = null;
}

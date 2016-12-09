package com.distelli.europa.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerRepo
{
    private String id = null;
    private String name = null;
    private String credId = null;
    private String region = null;
    private RegistryProvider provider = null;
}

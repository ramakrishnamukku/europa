package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryCred
{
    protected String domain = null;
    protected String id = null;
    protected Long created = null;
    protected String description = null;
    protected String username = null;
    protected String key = null;
    protected String name = null;
    protected RegistryProvider provider = null;
    protected String region = null;
    protected String password = null;
    protected String secret = null;
    protected String endpoint = null;
}

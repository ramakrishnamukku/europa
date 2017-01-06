/*
  $Id: $
  @file User.java
  @brief Contains the User.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User
{
    public static final String DOMAIN_PREFIX = "d";

    protected Long id;
    protected String email;
    protected String username;

    public String getDomain()
    {
        return String.format("%s%d",
                             DOMAIN_PREFIX,
                             this.id);
    }

    public static final Long domainToId(String domain)
    {
        if(domain == null)
            return null;
        if(domain.startsWith(DOMAIN_PREFIX))
            return Long.parseLong(domain.substring(DOMAIN_PREFIX.length()));
        return null;
    }
}

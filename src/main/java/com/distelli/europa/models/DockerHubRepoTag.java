package com.distelli.europa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerHubRepoTag {
    // Tag name:
    private String name;
    // TODO: at a minimum, we need the sha!
}

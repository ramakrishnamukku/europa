package com.distelli.europa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DockerHubRepository {
    private String name;
    private boolean isAutomated;
    private boolean isTrusted;
    private boolean isOfficial;
    private Integer starCount;
    private String description;
}

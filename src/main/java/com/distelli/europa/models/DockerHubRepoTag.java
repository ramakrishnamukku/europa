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
    private String tag;
    private String digest;
    private Long pushTime;
    private Long size;
}

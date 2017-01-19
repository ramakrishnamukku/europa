package com.distelli.europa.models;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pipeline
{
    private String domain;
    private String id;
    // Trigger for pipeline execution:
    private String containerRepoId;
    private String name;
    @Singular
    private List<PipelineComponent> components;
}

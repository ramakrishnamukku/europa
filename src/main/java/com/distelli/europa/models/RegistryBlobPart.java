package com.distelli.europa.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryBlobPart
{
    private Long chunkSize;
    private Integer partNum;
    private String partId;
}

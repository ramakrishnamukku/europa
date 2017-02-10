package com.distelli.europa.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page<T>
{
    protected String next;
    protected String prev;
    protected List<T> list;
}

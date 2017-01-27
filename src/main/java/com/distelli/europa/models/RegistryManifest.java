package com.distelli.europa.models;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
public class RegistryManifest
{
    /* Primary key (part 1). This is the domain of the owner.
     */
    private String domain;
    /* Primary key (part 2).
     */
    private String containerRepoId;
    /* Primary key (part 3).
     */
    private String tag;
    /* The content digest of the manifest sha256:<hex>.
     */
    private String manifestId;
    /* List of sha256:<hex> string BLOB digests referenced by this manifest.
     */
    @Singular
    private Set<String> digests;
    /* Domain of uploader. (book-keeping)
     */
    private String uploadedBy;
    /* The content type of the manifest.
     */
    private String contentType;
}

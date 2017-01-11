package com.distelli.europa.models;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryBlob
{
    /* Primary key. This is a compact UUID.
     */
    private String blobId;
    /* Alternative key, this should be a sha256:<hex> string.
     */
    private String digest;
    /* Domain of uploader. (book-keeping)
     */
    private String uploadedBy;
    /* If upload is in progress, this is an array of parts
     * that have been uploaded.
     */
    @Singular
    private List<RegistryBlobPart> partIds;
    /* If upload is in progress, this is the uploadId.
     */
    private String uploadId;
    /* Message digest encoded state.
     */
    private byte[] mdEncodedState;
    /* A list of manifest ids which reference this blob
     * (so we can implement GC).
     */
    @Singular
    private Set<String> manifestIds;
}

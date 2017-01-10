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
public class RegistryBlob
{
    /* Primary key. This is a compact UUID.
     */
    protected String blobId;
    /* Alternative key, this should be a sha256:<hex> string.
     */
    protected String digest;
    /* Domain of uploader. (book-keeping)
     */
    protected String uploadedBy;
    /* If upload is in progress, this is an array of parts
     * that have been uploaded.
     */
    @Singular
    protected List<RegistryBlobPart> partIds;
    /* If upload is in progress, this is the uploadId.
     */
    protected String uploadId;
    /* Message digest encoded state.
     */
    protected byte[] mdEncodedState;
}

package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Singular;
import javax.inject.Inject;
import com.distelli.europa.db.RegistryManifestDb;
import com.distelli.europa.db.ContainerRepoDb;

/**
 * Pipeline component that copies from one repository to another.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PCCopyToRepository extends PipelineComponent {
    private String destinationContainerRepoDomain;
    private String destinationContainerRepoId;
    private String tag;
    private Long lastExecutionTime;
    private ExecutionStatus lastExecutionStatus;

    @Inject
    private RegistryManifestDb _manifestDb;
    @Inject
    private ContainerRepoDb _repoDb;

    @Override
    public boolean execute(ContainerRepo repo, String tag, RegistryManifest manifest) {
        if ( null == _repoDb || null == _manifestDb ) {
            throw new IllegalStateException("Injector.injectMembers(this) has not been called");
        }
        if ( null == repo || null == manifest ) return true;
        if ( null == destinationContainerRepoId ) return true;
        if ( null == destinationContainerRepoDomain ) return true;
        if ( destinationContainerRepoId.equals(repo.getId()) ) return true;
        ContainerRepo destRepo = _repoDb.getRepo(destinationContainerRepoDomain, destinationContainerRepoId);
        if ( null == destRepo ) return true;
        if ( repo.getProvider() == RegistryProvider.EUROPA ) {
            RegistryManifest copy = manifest.toBuilder()
                .domain(destRepo.getDomain())
                .containerRepoId(destRepo.getId())
                .tag(tag)
                .build();
            _manifestDb.put(copy);
        }
        // TODO: Support other registry providers.
        return true;
    }

    protected PCCopyToRepository(String id, String destinationContainerRepoDomain, String destinationContainerRepoId, String tag, Long lastExecutionTime, ExecutionStatus lastExecutionStatus) {
        super(id);
        this.destinationContainerRepoDomain = destinationContainerRepoDomain;
        this.destinationContainerRepoId = destinationContainerRepoId;
        this.tag = tag;
        this.lastExecutionTime = lastExecutionTime;
        this.lastExecutionStatus = lastExecutionStatus;
    }

    public static class Builder<T extends Builder<T>> extends PipelineComponent.Builder<T> {
        protected String destinationContainerRepoDomain;
        protected String destinationContainerRepoId;
        protected String tag;
        protected Long lastExecutionTime;
        protected ExecutionStatus lastExecutionStatus;

        public T destinationContainerRepoDomain(String destinationContainerRepoDomain) {
            this.destinationContainerRepoDomain = destinationContainerRepoDomain;
            return self();
        }

        public T destinationContainerRepoId(String destinationContainerRepoId) {
            this.destinationContainerRepoId = destinationContainerRepoId;
            return self();
        }

        public T tag(String tag) {
            this.tag = tag;
            return self();
        }

        public T lastExecutionTime(Long lastExecutionTime) {
            this.lastExecutionTime = lastExecutionTime;
            return self();
        }

        public T lastExecutionStatus(ExecutionStatus lastExecutionStatus) {
            this.lastExecutionStatus = lastExecutionStatus;
            return self();
        }

        public PCCopyToRepository build() {
            return new PCCopyToRepository(id, destinationContainerRepoDomain, destinationContainerRepoId, tag, lastExecutionTime, lastExecutionStatus);
        }
    }

    public static Builder<?> builder() {
        return new Builder();
    }
}

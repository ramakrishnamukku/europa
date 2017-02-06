package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Singular;
import javax.inject.Inject;
import com.distelli.europa.db.RegistryManifestDb;
import com.distelli.europa.db.ContainerRepoDb;
import com.distelli.europa.db.RepoEventsDb;
import lombok.extern.log4j.Log4j;
import java.util.Collections;
import com.distelli.webserver.AjaxClientException;
import com.distelli.webserver.JsonError;

/**
 * Pipeline component that copies from one repository to another.
 */
@Log4j
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
    @Inject
    private RepoEventsDb _eventDb;

    @Override
    public boolean execute(ContainerRepo repo, String tag, String manifestDigestSha) {
        if ( null == _repoDb || null == _manifestDb ) {
            throw new IllegalStateException("Injector.injectMembers(this) has not been called");
        }
        if ( null == repo ) {
            throw new IllegalStateException("ContainerRepo must not be null");
        }
        if ( null == tag ) {
            throw new IllegalStateException("Tag must not be null");
        }
        // Not configured? Ignore...
        if ( null == destinationContainerRepoId || 
             null == destinationContainerRepoDomain )
        {
            log.debug("PipelineComponentId="+getId()+" has null destinationContainerRepoId or destinationContainerRepoDomain");
            return true;
        }
        // From the same repo? Ignore...
        if ( destinationContainerRepoId.equals(repo.getId()) ) {
            log.debug("PipelineComponentId="+getId()+" pushes to itself!?");
            return true;
        }
        // TODO: if manifestDigestSha is null, we should issue a "DELETE"
        if ( null == manifestDigestSha ) {
            log.debug("Tag delete is not implemented");
            return true;
        }
        ContainerRepo destRepo = _repoDb.getRepo(destinationContainerRepoDomain, destinationContainerRepoId);
        // To repo that doesn't exist...
        if ( null == destRepo ) {
            log.debug("PipelineComponentId="+getId()+" repo does not exist domain="+
                      destinationContainerRepoDomain+" id="+
                      destinationContainerRepoId);
            return true;
        }
        if ( repo.isLocal() && destRepo.isLocal() ) {
            // Optimization, simply update the DB:
            RegistryManifest manifest = _manifestDb.getManifestByRepoIdTag(
                repo.getDomain(),
                repo.getId(),
                manifestDigestSha);
            if ( null == manifest ) {
                log.debug("PipelineComponentId="+getId()+" missing manifest for domain="+repo.getDomain()+
                          " repoId="+repo.getId()+" tag="+manifestDigestSha);
                return true;
            }
            RegistryManifest copy = manifest.toBuilder()
                .domain(destRepo.getDomain())
                .containerRepoId(destRepo.getId())
                .tag(tag)
                .build();
            _manifestDb.put(copy);
            RepoEvent event = RepoEvent.builder()
                .domain(copy.getDomain())
                .repoId(copy.getContainerRepoId())
                .eventType(RepoEventType.PUSH)
                .eventTime(System.currentTimeMillis())
                .imageTags(Collections.singletonList(copy.getTag()))
                .imageSha(copy.getManifestId())
                .build();
            _eventDb.save(event);
            _repoDb.setLastEvent(event.getDomain(), event.getRepoId(), event);
        }
        // TODO: Support other registry providers.
        return true;
    }

    @Override
    public void validate(String key) {
        if ( null == destinationContainerRepoDomain ) {
            throw new AjaxClientException(
                "Missing Param '"+key+".destinationContainerRepoDomain' in request",
                JsonError.Codes.MissingParam,
                400);
        }
        if ( null == destinationContainerRepoId) {
            throw new AjaxClientException(
                "Missing Param '"+key+".destinationContainerRepoId' in request",
                JsonError.Codes.MissingParam,
                400);
        }
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

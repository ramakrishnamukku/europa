package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerRepo
{
    protected String domain = null;
    protected String id = null;
    protected String name = null;
    protected String credId = null;
    protected String region = null;
    protected RegistryProvider provider = null;
    //This is the AWS Account for ECR registries
    protected String registryId = null;
    protected String endpoint = null;
    protected RepoEvent lastEvent = null;
    protected boolean publicRepo = false;
    protected boolean local = true;
    //The ID of the object in the ObjectStore that holds the readme
    protected String overviewId;
    protected long lastSyncTime;
    protected long syncCount; // Incremented at the beginning of each sync.

    public String getPullCommand()
    {
        if(this.provider == null)
            return null;
        switch(provider)
        {
        case GCR:
            //gcloud docker -- pull us.gcr.io/distelli-alpha/europa-enterprise
            return String.format("gcloud docker -- pull %s/%s", this.region, this.name);
        case ECR:
            if(this.registryId == null)
                return null;
            //docker pull 708141427824.dkr.ecr.us-east-1.amazonaws.com/distelli:latest
            return String.format("docker pull %s.dkr.ecr.%s.amazonaws.com/%s",this.registryId, this.region, this.name);
        case DOCKERHUB:
            return String.format("docker pull %s", this.name);
        case PRIVATE:
            if(this.endpoint == null)
                return null;
            return String.format("docker pull %s/%s", this.endpoint, this.name);
        case EUROPA:
            if(this.endpoint == null)
                return null;
            return String.format("docker pull %s/%s", this.endpoint, this.name);
        default:
            return null;
        }
    }
}

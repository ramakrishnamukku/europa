package com.distelli.europa.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Singular;
import com.distelli.webserver.AjaxClientException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineComponent {
    private String id;

    // Used by RunPipeline:
    public boolean execute(ContainerRepo repo, String tag, String manifestDigestSha) throws Exception {
        return true;
    }

    // Used by AddPipelineComponent AJAX handler:
    // key is the location within the JSON
    public void validate(String key) throws AjaxClientException {
    }

    public static class Builder<T extends Builder<T>> {
        protected String id;
        @SuppressWarnings("unchecked")
        protected T self() {
            return (T)this;
        }
        public T id(String id) {
            this.id = id;
            return self();
        }
        public PipelineComponent build() {
            return new PipelineComponent(id);
        }
    }

    public static Builder<?> builder() {
        return new Builder();
    }
}

package com.distelli.europa.clients;

import java.util.Map;
import java.util.HashMap;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import java.net.URI;
import lombok.extern.log4j.Log4j;
import com.distelli.persistence.PageIterator;
import java.util.Base64;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import com.distelli.europa.models.HttpError;
import com.distelli.europa.models.DockerHubRepository;
import com.distelli.europa.models.DockerHubRepoTag;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.distelli.jackson.transform.TransformModule;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Instant;

@Log4j
public class DockerHubClient {
    private static long NANO_TO_SEC = 1000000000;
    private static final ObjectMapper OM = new ObjectMapper();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private OkHttpClient _client;
    private URI _hubEndpoint;
    private URI _registryEndpoint;
    private URI _registryAuthEndpoint;
    private String _username;
    private String _password;

    // Used in getHubToken() and refreshHubToken():
    private String _hubToken;
    private long _lastHubTokenRefresh;
    private ReadWriteLock _hubTokenLock = new ReentrantReadWriteLock();

    // Used in getRegistryToken() and refreshRegistryToken():
    private Map<String, String> _registryTokens = Collections.synchronizedMap(new HashMap<>());

    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.registerModule(createTransforms(new TransformModule()));
    }

    public static class Builder {
        private OkHttpClient.Builder _clientBuilder = new OkHttpClient.Builder();
        private URI _hubEndpoint;
        private URI _registryEndpoint;
        private URI _registryAuthEndpoint;
        private String _username;
        private String _password;

        public Builder hubEndpoint(URI endpoint) {
            _hubEndpoint = endpoint;
            return this;
        }

        public Builder registryEndpoint(URI endpoint) {
            _registryEndpoint = endpoint;
            return this;
        }

        public Builder registryAuthEndpoint(URI endpoint) {
            _registryAuthEndpoint = endpoint;
            return this;
        }

        public Builder connectionPool(ConnectionPool pool) {
            _clientBuilder.connectionPool(pool);
            return this;
        }

        public Builder credentials(String username, String password) {
            _username = username;
            _password = password;
            return this;
        }

        public DockerHubClient build() {
            return new DockerHubClient(this);
        }
    }

    private DockerHubClient(Builder builder) {
        _client = builder._clientBuilder.build();
        _hubEndpoint = builder._hubEndpoint;
        _registryEndpoint = builder._registryEndpoint;
        _registryAuthEndpoint = builder._registryAuthEndpoint;
        if ( null == _hubEndpoint ) _hubEndpoint = URI.create("https://hub.docker.com/");
        if ( null == _registryEndpoint ) _registryEndpoint = URI.create("https://index.docker.io/");
        if ( null == _registryAuthEndpoint ) _registryAuthEndpoint = URI.create("https://auth.docker.io/");
        _username = builder._username;
        _password = builder._password;
        if ( isEmpty(_username) || isEmpty(_password) ) {
            throw new IllegalArgumentException("The username or password must be non-empty");
        }
    }

    public Builder toBuilder() {
        return new Builder()
            .hubEndpoint(_hubEndpoint)
            .registryEndpoint(_registryEndpoint)
            .registryAuthEndpoint(_registryAuthEndpoint)
            .connectionPool(_client.connectionPool())
            .credentials(_username, _password);
    }

    public ConnectionPool connectionPool() {
        return _client.connectionPool();
    }

    // GET TOKEN:
    // curl -s -H "Content-Type: application/json" -X POST -d '{"username": "...", "password": "..."}' https://hub.docker.com/v2/users/login/ | jq -r .token

    // curl -s -H "Authorization: JWT ${TOKEN}" https://hub.docker.com/v2/repositories/distelli/?page_size=10
    public List<DockerHubRepository> listRepositories(String orgName, PageIterator iter) throws IOException {
        if ( null == orgName ) orgName = _username;
        Request req = addHubTokenAuth(new Request.Builder())
            .get()
            .url(addPageIterator(hubEndpoint(), iter)
                 .addPathSegments("v2/repositories")
                 .addPathSegment(orgName)
                 .addPathSegment("")
                 .build())
            .build();
        try ( Response res = _client.newCall(req).execute() ) {
            if ( res.code() / 100 != 2 ) {
                throw new HttpError(res.code(), res.body().string());
            }
            JsonNode json = OM.readTree(res.body().byteStream());
            HttpUrl next = HttpUrl.parse(json.at("/next").asText());
            if ( null == next ) {
                // no more pages left:
                iter.setMarker(null);
            } else {
                iter.setMarker(next.queryParameter("page"));
            }
            List<DockerHubRepository> results = new ArrayList<>();
            for ( JsonNode result : json.at("/results") ) {
                results.add(OM.convertValue(result, DockerHubRepository.class));
            }
            return results;
        }
    }

    private static class FutureCallback implements okhttp3.Callback {
        private Response response;
        private IOException ex;
        private Call call;

        public FutureCallback(Call call) {
            this.call = call;
            call.enqueue(this);
        }

        @Override
        public synchronized void onFailure(Call call, IOException ex) {
            this.ex = ex;
            notifyAll();
        }

        @Override
        public synchronized void onResponse(Call call, Response response) {
            this.response = response;
            notifyAll();
        }

        public void cancel() {
            call.cancel();
        }

        public synchronized Response get() throws IOException {
            try {
                while ( null == ex && null == response ) wait();
            } catch ( InterruptedException ex ) {
                Thread.currentThread().interrupt();
                return null;
            }
            // Throw a new exception so the backtrace is useful:
            if ( null != ex ) throw new IOException(ex);
            return response;
        }
    }

    public List<DockerHubRepoTag> listRepoTags(DockerHubRepository repo, PageIterator iter) throws IOException {
        return listRepoTags(repo.getNamespace() + "/" + repo.getName(), iter);
    }

    // curl -s -H "Authorization: JWT ${TOKEN}" 'https://hub.docker.com/v2/repositories/brimworks/test/tags/?page_size=3' | jq
    public List<DockerHubRepoTag> listRepoTags(String repoName, PageIterator iter) throws IOException {
        Request req = addHubTokenAuth(new Request.Builder())
            .get()
            .url(addPageIterator(hubEndpoint(), iter)
                 .addPathSegments("v2/repositories/"+repoName)
                 .addPathSegments("tags/")
                 .build())
            .build();
        JsonNode json;
        try ( Response res = _client.newCall(req).execute() ) {
            if ( res.code() / 100 != 2 ) {
                throw new HttpError(res.code(), res.body().string());
            }
            json = OM.readTree(res.body().byteStream());
        }
        HttpUrl next = HttpUrl.parse(json.at("/next").asText());
        iter.setMarker(null == next ? null : next.queryParameter("page"));
        List<DockerHubRepoTag> results = new ArrayList<>();

        List<FutureCallback> calls = new ArrayList<>();
        try {
            for ( JsonNode result : json.at("/results") ) {
                DockerHubRepoTag tag =
                    OM.convertValue(result, DockerHubRepoTag.class);
                results.add(tag);
                req = addRegistryTokenAuth(new Request.Builder(), repoName)
                    .head()
                    .header("Accept", "application/vnd.docker.distribution.manifest.v2+json")
                    .url(registryEndpoint()
                         .addPathSegments("v2/"+repoName)
                         .addPathSegment("manifests")
                         .addPathSegment(tag.getTag())
                         .build())
                    .build();
                calls.add(new FutureCallback(_client.newCall(req)));
            }
            int idx = 0;
            for ( FutureCallback call : calls ) {
                try ( Response res = call.get() ) {
                    results.get(idx++).setDigest(res.header("Docker-Content-Digest"));
                }
            }
        } finally {
            for ( FutureCallback call : calls ) {
                call.cancel();
            }
        }
        return results;
    }

    private static boolean isEmpty(String str) {
        return null == str || str.isEmpty();
    }

    private static HttpUrl.Builder addPageIterator(HttpUrl.Builder url, PageIterator iter) {
        if ( null == iter ) return url;
        if ( Integer.MAX_VALUE != iter.getPageSize() ) {
            url.addQueryParameter("page_size", ""+iter.getPageSize());
        }
        if ( null != iter.getMarker() ) {
            url.addQueryParameter("page", iter.getMarker());
        }
        return url;
    }

    private static TransformModule createTransforms(TransformModule module) {
        module.createTransform(DockerHubRepository.class)
            .put("name", String.class, "name")
            .put("namespace", String.class, "namespace")
            .put("description", String.class, "description");
        module.createTransform(DockerHubRepoTag.class)
            .put("name", String.class, "tag")
            .put("full_size", Long.class, "size")
            .put("last_updated", String.class,
                 (tag) -> (null == tag.getPushTime()) ? null : ""+Instant.ofEpochMilli(tag.getPushTime()),
                 (tag, time) -> tag.setPushTime(null == time ? null : Instant.parse(time).toEpochMilli()));
        return module;
    }

    private String getHubToken() throws IOException {
        Lock readLock = _hubTokenLock.readLock();
        readLock.lock();
        try {
            if ( null != _hubToken ) return _hubToken;
        } finally {
            readLock.unlock();
        }
        refreshHubToken();
        // Try again:
        readLock.lock();
        try {
            return _hubToken;
        } finally {
            readLock.unlock();
        }
    }

    private String getRegistryToken(String repositoryName) throws IOException {
        String token = _registryTokens.get(repositoryName);
        if ( null != token ) return token;
        synchronized ( _registryTokens ) {
            token = _registryTokens.get(repositoryName);
            if ( null == token ) {
                refreshRegistryToken(repositoryName);
                token = _registryTokens.get(repositoryName);
            }
        }
        return token;
    }

    private void refreshHubToken() throws IOException {
        Lock writeLock = _hubTokenLock.writeLock();
        writeLock.lock();
        try {
            if ( null != _hubToken && System.nanoTime() - _lastHubTokenRefresh - 5*NANO_TO_SEC > 0 ) {
                return;
            }
            JsonNodeFactory jnf = OM.getNodeFactory();
            JsonNode body = jnf.objectNode()
                .put("username", _username)
                .put("password", _password);
            Request req = new Request.Builder()
                .post(RequestBody.create(JSON_MEDIA_TYPE, OM.writeValueAsString(body)))
                .url(hubEndpoint()
                     .addPathSegments("/v2/users/login/")
                     .build())
                .build();
            try ( Response res = _client.newCall(req).execute() ) {
                if ( res.code() / 100 != 2 ) {
                    throw new HttpError(res.code(), res.body().string());
                }
                JsonNode json = OM.readTree(res.body().byteStream());
                _hubToken = json.at("/token").asText();
                _lastHubTokenRefresh = System.nanoTime();
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void refreshRegistryToken(String repositoryName) throws IOException {
        synchronized ( _registryTokens ) {
            Request req = addBasicAuth(new Request.Builder())
                .get()
                .url(registryAuthEndpoint()
                     .addPathSegments("/token")
                     .addQueryParameter("service", "registry.docker.io")
                     .addQueryParameter("scope", "repository:"+repositoryName+":pull")
                     .build())
                .build();
            try ( Response res = _client.newCall(req).execute() ) {
                if ( res.code() / 100 != 2 ) {
                    throw new HttpError(res.code(), res.body().string());
                }
                JsonNode json = OM.readTree(res.body().byteStream());
                _registryTokens.put(repositoryName, json.at("/token").asText());
            }
        }
    }

    private Request.Builder addHubTokenAuth(Request.Builder req) throws IOException {
        return req.header("Authorization", "JWT " + getHubToken());
    }

    private Request.Builder addRegistryTokenAuth(Request.Builder req, String repositoryName) throws IOException {
        return req.header("Authorization", "Bearer " + getRegistryToken(repositoryName));
    }

    private Request.Builder addBasicAuth(Request.Builder req) {
        req.header("Authorization",
                   "Basic " +
                   Base64.getEncoder()
                   .encodeToString((_username + ":" + _password).getBytes(UTF_8)));
        return req;
    }

    private HttpUrl.Builder hubEndpoint() {
        return HttpUrl.get(_hubEndpoint).newBuilder();
    }

    private HttpUrl.Builder registryEndpoint() {
        return HttpUrl.get(_registryEndpoint).newBuilder();
    }

    private HttpUrl.Builder registryAuthEndpoint() {
        return HttpUrl.get(_registryAuthEndpoint).newBuilder();
    }
}

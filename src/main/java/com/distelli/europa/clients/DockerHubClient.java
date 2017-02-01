package com.distelli.europa.clients;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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

@Log4j
public class DockerHubClient {
    private static long NANO_TO_SEC = 1000000000;
    private static final ObjectMapper OM = new ObjectMapper();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private OkHttpClient _client;
    private URI _endpoint;
    private String _username;
    private String _password;

    // Used in getToken() and refreshToken():
    private String _token;
    private long _lastRefresh;
    private ReadWriteLock _tokenLock = new ReentrantReadWriteLock();

    static {
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.registerModule(createTransforms(new TransformModule()));
    }

    protected static class Builder<Self extends Builder> {
        private OkHttpClient.Builder _clientBuilder = new OkHttpClient.Builder();
        private URI _endpoint;
        private String _username;
        private String _password;

        public Self endpoint(URI endpoint) {
            _endpoint = endpoint;
            return self();
        }

        public Self connectionPool(ConnectionPool pool) {
            _clientBuilder.connectionPool(pool);
            return self();
        }

        public Self credentials(String username, String password) {
            _username = username;
            _password = password;
            return self();
        }

        public DockerHubClient build() {
            return new DockerHubClient(this);
        }

        protected Self self() {
            return (Self)this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private DockerHubClient(Builder builder) {
        _client = builder._clientBuilder.build();
        _endpoint = builder._endpoint;
        if ( null == _endpoint ) _endpoint = URI.create("https://hub.docker.com/");
        _username = builder._username;
        _password = builder._password;
        if ( isEmpty(_username) || isEmpty(_password) ) {
            throw new IllegalArgumentException("The username or password must be non-empty");
        }
    }

    public Builder toBuilder() {
        return new Builder()
            .endpoint(_endpoint)
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
        Request req = addTokenAuth(new Request.Builder())
            .get()
            .url(addPageIterator(endpoint(), iter)
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

    // curl -s -H "Authorization: JWT ${TOKEN}" 'https://hub.docker.com/v2/repositories/brimworks/test/tags/?page_size=3' | jq
    public List<DockerHubRepoTag> listRepoTags(DockerHubRepository repo, PageIterator iter) throws IOException {
        Request req = addTokenAuth(new Request.Builder())
            .get()
            .url(addPageIterator(endpoint(), iter)
                 .addPathSegments("v2/repositories")
                 .addPathSegment(repo.getNamespace())
                 .addPathSegment(repo.getName())
                 .addPathSegments("tags/")
                 .build())
            .build();
        try ( Response res = _client.newCall(req).execute() ) {
            if ( res.code() / 100 != 2 ) {
                throw new HttpError(res.code(), res.body().string());
            }
            JsonNode json = OM.readTree(res.body().byteStream());
            HttpUrl next = HttpUrl.parse(json.at("/next").asText());
            iter.setMarker(null == next ? null : next.queryParameter("page"));
            List<DockerHubRepoTag> results = new ArrayList<>();
            for ( JsonNode result : json.at("/results") ) {
                results.add(OM.convertValue(result, DockerHubRepoTag.class));
            }
            return results;
        }
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
            .put("name", String.class, "name");
        return module;
    }

    private String getToken() throws IOException {
        Lock readLock = _tokenLock.readLock();
        readLock.lock();
        try {
            if ( null != _token ) return _token;
        } finally {
            readLock.unlock();
        }
        refreshToken();
        // Try again:
        readLock.lock();
        try {
            return _token;
        } finally {
            readLock.unlock();
        }
    }

    private void refreshToken() throws IOException {
        Lock writeLock = _tokenLock.writeLock();
        writeLock.lock();
        try {
            if ( null != _token && System.nanoTime() - _lastRefresh - 5*NANO_TO_SEC > 0 ) {
                return;
            }
            JsonNodeFactory jnf = OM.getNodeFactory();
            JsonNode body = jnf.objectNode()
                .put("username", _username)
                .put("password", _password);
            Request req = new Request.Builder()
                .post(RequestBody.create(JSON_MEDIA_TYPE, OM.writeValueAsString(body)))
                .url(endpoint()
                     .addPathSegments("/v2/users/login/")
                     .build())
                .build();
            try ( Response res = _client.newCall(req).execute() ) {
                if ( res.code() / 100 != 2 ) {
                    throw new HttpError(res.code(), res.body().string());
                }
                JsonNode json = OM.readTree(res.body().byteStream());
                _token = json.at("/token").asText();
                _lastRefresh = System.nanoTime();
            }
        } finally {
            writeLock.unlock();
        }
    }

    private Request.Builder addTokenAuth(Request.Builder req) throws IOException {
        return req.header("Authorization", "JWT " + getToken());
    }

    private HttpUrl.Builder endpoint() {
        return HttpUrl.get(_endpoint).newBuilder();
    }
}

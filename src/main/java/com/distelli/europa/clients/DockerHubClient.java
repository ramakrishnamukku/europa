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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.distelli.jackson.transform.TransformModule;

@Log4j
public class DockerHubClient {
    private static final ObjectMapper OM = new ObjectMapper();
    private OkHttpClient _client;
    private URI _endpoint;
    private String _username;
    private String _password;

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
        if ( null == _endpoint ) _endpoint = URI.create("https://index.docker.io/");
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

    public List<DockerHubRepository> listRepositories(PageIterator iter) throws IOException {
        // Use the v1 API :(. Note that the search is NOT a prefix search... so we have to do post-filtering.

        int page = ( null == iter.getMarker() ) ? 1 : Integer.parseInt(iter.getMarker());

        String query = _username + "/";
        Request req = addBasicAuth(new Request.Builder())
            .get()
            .url(addPageIterator(endpoint(), iter)
                 .addPathSegments("v1/search")
                 .addQueryParameter("q", query)
                 .build())
            .build();
        try ( Response res = _client.newCall(req).execute() ) {
            if ( res.code() / 100 != 2 ) {
                throw new HttpError(res.code(), res.body().string());
            }
            JsonNode json = OM.readTree(res.body().byteStream());
            if ( json.at("/num_pages").asInt(0) <= page ) {
                // no more pages left:
                iter.setMarker(null);
            } else {
                iter.setMarker(""+(page+1));
            }
            List<DockerHubRepository> results = new ArrayList<>();
            for ( JsonNode result : json.at("/results") ) {
                if ( ! result.at("/name").asText().startsWith(query) ) continue;
                results.add(OM.convertValue(result, DockerHubRepository.class));
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
            url.addQueryParameter("n", ""+iter.getPageSize());
        }
        if ( null != iter.getMarker() ) {
            url.addQueryParameter("page", iter.getMarker());
        }
        return url;
    }

    private static TransformModule createTransforms(TransformModule module) {
        module.createTransform(DockerHubRepository.class)
            .put("is_automated", Boolean.class, "isAutomated")
            .put("name", String.class, "name")
            .put("is_trusted", Boolean.class, "isTrusted")
            .put("is_official", Boolean.class, "isOfficial")
            .put("star_count", Integer.class, "starCount")
            .put("description", String.class, "description");
        return module;
    }

    private Request.Builder addBasicAuth(Request.Builder req) {
        req.header("Authorization",
                   "Basic " +
                   Base64.getEncoder()
                   .encodeToString((_username + ":" + _password).getBytes(UTF_8)));
        return req;
    }

    private HttpUrl.Builder endpoint() {
        return HttpUrl.get(_endpoint).newBuilder();
    }
}

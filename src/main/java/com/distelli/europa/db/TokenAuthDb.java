package com.distelli.europa.db;

import com.distelli.europa.models.TokenAuth;
import com.distelli.europa.models.TokenAuthStatus;
import com.distelli.jackson.transform.TransformModule;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.RollbackException;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import lombok.extern.log4j.Log4j;


@Log4j
@Singleton
public class TokenAuthDb extends BaseDb {
    private Index<TokenAuth> _main;
    private Index<TokenAuth> _byDomain;

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName("auth")
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("tok", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("dom-index")
                    .hashKey(attr("dom", AttrType.STR))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(TokenAuth.class)
            .put("tok", String.class, "token")
            .put("dom", String.class, "domain")
            .put("uname", String.class, "username")
            .put("ctime", Long.class, "created")
            .put("stat", TokenAuthStatus.class, "status");
        return module;
    }

    @Inject
    protected TokenAuthDb(Index.Factory indexFactory,
                          ConvertMarker.Factory convertMarkerFactory)
    {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(TokenAuth.class)
            .withTableName("auth")
            .withHashKeyName("tok")
            .withNoEncrypt("dom")
            .withConvertValue(om::convertValue)
            .withConvertMarker(convertMarkerFactory.create("tok"))
            .build();

        _byDomain = indexFactory.create(TokenAuth.class)
            .withIndexName("auth", "dom-index")
            .withNoEncrypt("tok")
            .withHashKeyName("dom")
            .withConvertValue(om::convertValue)
            .withConvertMarker(convertMarkerFactory.create("dom", "tok"))
            .build();
    }

    public TokenAuth getToken(String token) {
        return _main.getItem(token);
    }

    public List<TokenAuth> getTokens(String domain, PageIterator iter) {
        return _byDomain.queryItems(domain, iter).list();
    }

    public void save(TokenAuth tokenAuth) throws EntityExistsException {
        if ( null == tokenAuth.getDomain() || "".equals(tokenAuth.getDomain()) ) {
            throw new IllegalArgumentException("TokenAuth.domain must be non-empty!");
        }
        _main.putItemOrThrow(tokenAuth);
    }

    public void setStatus(String domain, String token, TokenAuthStatus status)
    {
        _main.updateItem(token, null)
        .set("stat", status)
        .when((expr) -> expr.and(expr.exists("tok"),
                                 expr.eq("dom", domain.toLowerCase())));
    }

    public void deleteToken(String domain, String token)
        throws RollbackException
    {
        _main.deleteItem(token, null,
                         (expr) -> expr.and(expr.eq("stat", "INACTIVE"),
                                            expr.eq("dom", domain.toLowerCase())));
    }
}

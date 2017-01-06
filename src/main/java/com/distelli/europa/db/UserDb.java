/*
  $Id: $
  @file UserDb.java
  @brief Contains the UserDb.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.db;

import java.util.List;
import com.distelli.persistence.AttrDescription;
import com.distelli.persistence.AttrType;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.IndexDescription;
import com.distelli.persistence.IndexType;
import com.distelli.persistence.PageIterator;
import com.distelli.persistence.TableDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;

import com.distelli.europa.models.User;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;
import java.util.Arrays;

@Log4j
@Singleton
public class UserDb
{
    private static final String TABLE_NAME = "users";

    private Index<User> _main;
    private Index<User> _byEmail;
    private Index<User> _byUsername;

    private final ObjectMapper _om = new ObjectMapper();
    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(User.class)
        .put("id", Long.class, "id")
        .put("email", String.class, "email")
        .put("username", String.class, "username");
        return module;
    }

    private static AttrDescription attr(String name, AttrType type) {
        return AttrDescription.builder()
            .attrName(name)
            .attrType(type)
            .build();
    }

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName(TABLE_NAME)
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("id", AttrType.NUM))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("email-index")
                    .hashKey(attr("email", AttrType.STR))
                    .rangeKey(attr("id", AttrType.NUM))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build(),
                    IndexDescription.builder()
                    .indexName("username-index")
                    .hashKey(attr("username", AttrType.STR))
                    .rangeKey(attr("id", AttrType.NUM))
                    .indexType(IndexType.GLOBAL_SECONDARY_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    @Inject
    protected UserDb(Index.Factory indexFactory,
                     ConvertMarker.Factory convertMarkerFactory)
    {
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(User.class)
            .withTableName(TABLE_NAME)
            .withNoEncrypt("id", "email", "username")
            .withHashKeyName("id")
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create("id"))
            .build();

        // Make sure the d0 account exists:
        _main.putItemIfNotExists(
            User.builder()
            .id(0L)
            .email("root@localhost")
            .username("root")
            .build());

        _byEmail = indexFactory.create(User.class)
            .withIndexName(TABLE_NAME, "email-index")
            .withNoEncrypt("id", "email", "username")
            .withHashKeyName("email")
            .withRangeKeyName("id") // so it is sorted.
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create("email", "id"))
            .build();

        _byUsername = indexFactory.create(User.class)
            .withIndexName(TABLE_NAME, "username-index")
            .withNoEncrypt("id", "email", "username")
            .withHashKeyName("username")
            .withRangeKeyName("id") // so it is sorted.
            .withConvertValue(_om::convertValue)
            .withConvertMarker(convertMarkerFactory.create("username", "id"))
        .build();
    }

    public void save(User user)
    {
        Long id = user.getId();
        if(id == null)
            throw(new IllegalArgumentException("Invalid id in User: "+user));
        String email = user.getEmail();
        if(email == null)
            throw(new IllegalArgumentException("Invalid email in User: "+user));
        String username = user.getUsername();
        if(username == null)
            throw(new IllegalArgumentException("Invalid username in User: "+user));

        email = email.toLowerCase();
        username = username.toLowerCase();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);

        _main.putItemOrThrow(user);
    }

    public User getUserByDomain(String domain)
    {
        Long id = User.domainToId(domain);
        return getUserById(id);
    }

    public User getUserById(long id)
    {
        return _main.getItem(id);
    }

    public User getUserByEmail(String email)
    {
        List<User> users =
            _byEmail.queryItems(email.toLowerCase(), new PageIterator().pageSize(1))
            .list();
        if(users == null || users.size() == 0)
            return null;
        return users.get(0);
    }

    public User getUserByUsername(String username)
    {
        List<User> users =
            _byUsername.queryItems(username.toLowerCase(), new PageIterator().pageSize(1))
            .list();
        if(users == null || users.size() == 0)
            return null;
        return users.get(0);
    }
}

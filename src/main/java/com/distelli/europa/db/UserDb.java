/*
  $Id: $
  @file UserDb.java
  @brief Contains the UserDb.java class

  @author Rahul Singh [rsingh]
*/
package com.distelli.europa.db;

import java.util.List;
import com.distelli.persistence.ConvertMarker;
import com.distelli.persistence.Index;
import com.distelli.persistence.Index;
import com.distelli.persistence.PageIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.jackson.transform.TransformModule;

import com.distelli.europa.ajax.*;
import com.distelli.europa.models.*;
import javax.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class UserDb
{
    private Index<User> _main;
    private Index<User> _byEmail;
    private Index<User> _byUsername;

    private static final String HASH_KEY_CONSTANT = "d0";

    private final ObjectMapper _om = new ObjectMapper();
    private TransformModule createTransforms(TransformModule module) {
        module.createTransform(User.class)
        .put("hk", String.class, (t) -> { return HASH_KEY_CONSTANT; })
        .put("id", String.class, "id")
        .put("email", String.class, "email")
        .put("username", String.class, "ussername");
        return module;
    }

    public UserDb(Index.Factory indexFactory,
                  ConvertMarker.Factory convertMarkerFactory)
    {
        _om.registerModule(createTransforms(new TransformModule()));
        _main = indexFactory.create(User.class)
        .withTableName("users")
        .withNoEncrypt("hk", "id", "email", "username")
        .withHashKeyName("hk")
        .withRangeKeyName("id")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "id"))
        .build();

        _byEmail = indexFactory.create(User.class)
        .withIndexName("users", "email-index")
        .withNoEncrypt("hk", "id", "email", "username")
        .withHashKeyName("hk")
        .withRangeKeyName("email")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "email"))
        .build();

        _byUsername = indexFactory.create(User.class)
        .withIndexName("users", "username-index")
        .withNoEncrypt("hk", "id", "email", "username")
        .withHashKeyName("hk")
        .withRangeKeyName("username")
        .withConvertValue(_om::convertValue)
        .withConvertMarker(convertMarkerFactory.create("hk", "username"))
        .build();
    }

    public void save(User user)
    {
        String id = user.getId();
        if(id == null)
            throw(new IllegalArgumentException("Invalid id in User: "+user));
        String email = user.getEmail();
        if(email == null)
            throw(new IllegalArgumentException("Invalid email in User: "+user));
        String username = user.getUsername();
        if(username == null)
            throw(new IllegalArgumentException("Invalid username in User: "+user));

        id = id.toLowerCase();
        email = email.toLowerCase();
        username = username.toLowerCase();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);

        _main.putItem(user);
    }

    public User getUserByDomain(String domain)
    {
        String id = User.domainToId(domain);
        return getUserById(id);
    }

    public User getUserById(String id)
    {
        return _main.getItem(HASH_KEY_CONSTANT,
                             id.toLowerCase());
    }

    public User getUserByEmail(String email)
    {
        List<User> users = _byEmail.queryItems(HASH_KEY_CONSTANT,
                                               new PageIterator().pageSize(10))
        .eq(email.toLowerCase())
        .list();
        if(users == null || users.size() == 0)
            return null;
        if(users.size() != 1)
            throw(new RuntimeException("Unexpected User List of size: "+users.size()+" for email: "+email));
        return users.get(0);
    }

    public User getUserByUsername(String username)
    {
        List<User> users = _byUsername.queryItems(HASH_KEY_CONSTANT,
                                      new PageIterator().pageSize(10))
        .eq(username.toLowerCase())
        .list();
        if(users == null || users.size() == 0)
            return null;
        if(users.size() != 1)
            throw(new RuntimeException("Unexpected User List of size: "+users.size()+" for username: "+username));
        return users.get(0);
    }
}

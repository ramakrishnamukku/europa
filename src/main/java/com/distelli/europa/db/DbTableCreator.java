/*
  $Id: $
  @file DbTableCreator.java
  @brief Contains the DbTableCreator.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.europa.db;

import com.distelli.cred.CredPair;
import lombok.extern.log4j.Log4j;
import com.distelli.persistence.Schema;
import com.distelli.persistence.TableDescription;
import java.net.URI;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

@Log4j
public class DbTableCreator
{
    private static final double NS_IN_MS = 1000000.0;

    @Inject @Named("BASE")
    private Schema.Factory _baseSchemaFactory;

    private URI _endpoint;
    private CredPair _creds;

    private Set<TableDescription> _tableDescriptions = null;

    public DbTableCreator(URI endpoint, CredPair creds)
    {
        _endpoint = endpoint;
        _creds = creds;
    }

    public void createTables(Set<TableDescription> tableDescriptions)
    {
        long t0 = System.nanoTime();
        try {
            log.info("DB schema initializing");
            _baseSchemaFactory.create()
                .withTableNameFormat("%s.europa") //TODO: Add prefix support
                .withEndpoint(_endpoint)
                .withCredProvider(() -> _creds)
                .build()
                .createMissingTablesOrIndexes(tableDescriptions);
        } catch(Throwable ex) {
            throw(new RuntimeException(ex));
        } finally {
            log.info("DB schema initialized in "+(System.nanoTime()-t0)/NS_IN_MS+"ms");
        }
    }
}

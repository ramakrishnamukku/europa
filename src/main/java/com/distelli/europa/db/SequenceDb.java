package com.distelli.europa.db;

import lombok.extern.log4j.Log4j;
import java.util.List;
import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.Arrays;

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

@Log4j
@Singleton
public class SequenceDb extends BaseDb
{
    private static final String TABLE_NAME = "sequences";

    private static final ObjectMapper OM = new ObjectMapper();

    protected static class Sequence {
        public String seq;
        public Long num;
    }

    private Index<Sequence> _main;

    public static TableDescription getTableDescription() {
        return TableDescription.builder()
            .tableName(TABLE_NAME)
            .indexes(
                Arrays.asList(
                    IndexDescription.builder()
                    .hashKey(attr("seq", AttrType.STR))
                    .indexType(IndexType.MAIN_INDEX)
                    .readCapacity(1L)
                    .writeCapacity(1L)
                    .build()))
            .build();
    }

    @Inject
    protected SequenceDb(Index.Factory indexFactory,
                         ConvertMarker.Factory convertMarkerFactory)
    {
        _main = indexFactory.create(Sequence.class)
            .withTableName(TABLE_NAME)
            .withNoEncrypt("seq", "num")
            .withHashKeyName("seq")
            .withConvertValue(OM::convertValue)
            .withConvertMarker(convertMarkerFactory.create("seq"))
            .build();
    }

    private Long next(String seqName) {
        Sequence seq = _main.updateItem(seqName, null)
            .increment("num", 1L)
            .returnAllNew()
            .always();
        return seq.num;
    }

    //////////////////////////////////////////////////////////////////////
    // Add usages of sequence generator below here.
    public Long nextUserId() {
        return next("user");
    }

    public Long nextManifestId() {
        return next("rmanifest");
    }
}

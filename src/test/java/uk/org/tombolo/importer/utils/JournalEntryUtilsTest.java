package uk.org.tombolo.importer.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import uk.org.tombolo.core.DatabaseJournalEntry;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class JournalEntryUtilsTest {

    @Test
    public void getJournalEntryForDatasourceIdNull() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                null,
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id", entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdEmpty() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id", entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdEmptyNull() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Collections.emptyList(),
                null);
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id", entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdGeoTime() throws Exception {

        DatabaseJournalEntry entry = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("geo"),
                Arrays.asList("time"));
        assertEquals("class.name", entry.getClassName());
        assertEquals("datasource-id:"+ DigestUtils.md5Hex("geo|time"), entry.getKey());
    }

    @Test
    public void getJournalEntryForDatasourceIdOneTwoThree() throws Exception {

        DatabaseJournalEntry entry1 = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("one", "two"),
                Arrays.asList("three"));
        assertEquals("class.name", entry1.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("one\ttwo|three"), entry1.getKey());

        // Here we test that the arrays are sorted before joining
        DatabaseJournalEntry entry2 = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("one"),
                Arrays.asList("two", "three"));
        assertEquals("class.name", entry2.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("one|three\ttwo"), entry2.getKey());

        DatabaseJournalEntry entry3 = JournalEntryUtils.getJournalEntryForDatasourceId(
                "class.name",
                "datasource-id",
                Arrays.asList("one"),
                Arrays.asList("three", "two"));
        assertEquals("class.name", entry3.getClassName());
        assertEquals("datasource-id:" + DigestUtils.md5Hex("one|three\ttwo"), entry3.getKey());

        assertEquals(entry2.getKey(), entry3.getKey());

        // This one is to make sure that we use a different separator intra-scope and inter-scope
        assertNotSame(entry1.getKey(), entry2.getKey());
    }
}
package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class DocumentManagerTest {

    private DocumentManager documentManager;
    private DocumentManager.Author author;

    @Before
    public void setUp() {
        documentManager = new DocumentManager();
        author = DocumentManager.Author.builder().id("1").name("John Doe").build();
    }

    @After
    public void tearDown() {
        documentManager.storage.clear();
    }

    @Test
    public void testSaveDocument() {
        DocumentManager.Document doc = DocumentManager.Document.builder()
                .title("Title")
                .content("Content")
                .author(author)
                .build();

        DocumentManager.Document savedDoc = documentManager.save(doc);
        assertNotNull(savedDoc.getId());
        assertNotNull(savedDoc.getCreated());

        Optional<DocumentManager.Document> foundDoc = documentManager.findById(savedDoc.getId());
        assertTrue(foundDoc.isPresent());
        assertEquals(savedDoc, foundDoc.get());
    }

    @Test
    public void testSearchDocumentByTitlePrefix() {
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .title("Title One")
                .content("Content One")
                .author(author)
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .title("Another Title")
                .content("Content Two")
                .author(author)
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Collections.singletonList("Title"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(searchRequest);
        assertEquals(1, results.size());
        assertEquals(doc1, results.get(0));
    }

    @Test
    public void testSearchDocumentByContent() {
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .title("Title One")
                .content("Content One")
                .author(author)
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .title("Another Title")
                .content("Content Two")
                .author(author)
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .containsContents(Collections.singletonList("Content Two"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(searchRequest);
        assertEquals(1, results.size());
        assertEquals(doc2, results.get(0));
    }

    @Test
    public void testSearchDocumentByAuthor() {
        DocumentManager.Author author2 = DocumentManager.Author.builder().id("2").name("Jane Doe").build();
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .title("Title One")
                .content("Content One")
                .author(author)
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .title("Another Title")
                .content("Content Two")
                .author(author2)
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .authorIds(Collections.singletonList("1"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(searchRequest);
        assertEquals(1, results.size());
        assertEquals(doc1, results.get(0));
    }

    @Test
    public void testSearchDocumentByDateRange() {
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .title("Title One")
                .content("Content One")
                .author(author)
                .created(Instant.now().minusSeconds(3600))
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .title("Another Title")
                .content("Content Two")
                .author(author)
                .created(Instant.now().plusSeconds(3600))
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .createdFrom(Instant.now().minusSeconds(7200))
                .createdTo(Instant.now())
                .build();

        List<DocumentManager.Document> results = documentManager.search(searchRequest);
        System.out.println(results);
        assertEquals(2, results.size());
        assertEquals(doc1, results.get(0));
    }
}

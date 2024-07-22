package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    final Map<String, Document> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(String.valueOf(idGenerator.incrementAndGet()));
        }
        if (document.getCreated() == null || document.getCreated().isAfter(Instant.now()))
            document.setCreated(Instant.now());
        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {

        return storage.values().stream()
                .filter(doc -> matches(doc, request))
                .collect(Collectors.toList());
    }

    private boolean matches(Document doc, SearchRequest request) {
        if (request.getTitlePrefixes() != null) {
            boolean titleMatches = request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> doc.getTitle().startsWith(prefix));
            if (!titleMatches) return false;
        }

        if (request.getContainsContents() != null) {
            boolean contentMatches = request.getContainsContents().stream()
                    .anyMatch(content -> doc.getContent().contains(content));
            if (!contentMatches) return false;
        }

        if (request.getAuthorIds() != null) {
            boolean authorMatches = request.getAuthorIds().contains(doc.getAuthor().getId());
            if (!authorMatches) return false;
        }

        if (request.getCreatedFrom() != null && doc.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }

        if (request.getCreatedTo() != null && doc.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }

        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}

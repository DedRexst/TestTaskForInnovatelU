package org.test_task;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
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

    private final HashMap<String, Document> documentStorage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        // Iterate through each entry in documentStorage to check for existing documents with the same properties
        for (Map.Entry<String, Document> entry : documentStorage.entrySet()) {
            Document value = entry.getValue();

            // Check if the existing document has the same author, content, created date, and title
            if (value.getAuthor().equals(document.getAuthor()) &&
                    value.getContent().equals(document.getContent()) &&
                    value.getCreated().equals(document.getCreated()) &&
                    value.getTitle().equals(document.getTitle())) {
                // If a match is found, return the existing document
                return value;
            }
        }

        // If the document does not have an ID, generate a new unique ID for it
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        // If the document does not have a creation timestamp, set the current timestamp as the created date
        if (document.getCreated() == null) {
            document.setCreated(Instant.now());
        }

        // Store the document in the documentStorage map with its ID as the key
        documentStorage.put(document.getId(), document);

        // Return the newly saved or updated document
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        // Stream through all documents in documentStorage and filter based on search criteria
        return documentStorage.values().stream()
                .filter(doc -> matchesSearchRequest(doc, request)) // Filter documents that match the search request
                .collect(Collectors.toList()); // Collect matching documents into a list
    }

    private boolean matchesSearchRequest(Document doc, SearchRequest request) {
        // If no search criteria are provided, return true for all documents
        if (request == null) {
            return true;
        }

        // Check each search criterion and return false if the document does not match any of them
        if (request.getTitlePrefixes() != null &&                       // Check if the document title starts with any of the specified prefixes
                request.getTitlePrefixes().stream().noneMatch(prefix -> doc.getTitle().startsWith(prefix)) ||
                request.getContainsContents() != null &&                // Check if the document content contains any of the specified strings
                        request.getContainsContents().stream().noneMatch(content -> doc.getContent().contains(content)) ||
                request.getAuthorIds() != null &&                       // Check if the document author ID is in the list of specified author IDs
                        !request.getAuthorIds().contains(doc.getAuthor().getId()) ||
                request.getCreatedFrom() != null &&                     // Check if the document creation date is after or on the specified "from" date
                        doc.getCreated().isBefore(request.getCreatedFrom()) ||
                request.getCreatedTo() != null &&                       // Check if the document creation date is before or on the specified "to" date
                        doc.getCreated().isAfter(request.getCreatedTo())) {

            return false; // Return false if any criterion is not met
        } else {
            return true; // Return true if all criteria are met
        }
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        // Retrieve the document from documentStorage by its ID.
        // If the document is found, wrap it in an Optional; if not, return an empty Optional.
        return Optional.ofNullable(documentStorage.get(id));
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
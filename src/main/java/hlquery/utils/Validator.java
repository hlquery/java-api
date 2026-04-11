package hlquery.utils;

public class Validator {
    public static void validateCollectionName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection name cannot be empty");
        }
    }

    public static void validateDocumentId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be empty");
        }
    }
}

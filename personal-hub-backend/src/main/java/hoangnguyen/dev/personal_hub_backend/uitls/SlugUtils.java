package hoangnguyen.dev.personal_hub_backend.uitls;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Utility class for generating URL-friendly slugs
 */
public class SlugUtils {

    /**
     * Converts a text input into a URL-friendly slug
     * - Removes diacritical marks (accents)
     * - Replaces spaces with hyphens
     * - Converts to lowercase
     * - Removes special characters
     *
     * @param input The string to convert to a slug
     * @return A URL-friendly slug string
     */
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String noUnderscore = input.trim().replaceAll("_", "-");
        // Replace multiple whitespace with a single hyphen
        String noWhiteSpace = noUnderscore.replaceAll("\\s+", "-");
        // Normalize accents and special characters
        String normalized = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD);

        return normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "") // Remove diacritical marks
                .replaceAll("[^a-zA-Z0-9-]", "") // Keep only letters, numbers, and hyphens
                .toLowerCase(Locale.ROOT); // Convert to lowercase
    }
}

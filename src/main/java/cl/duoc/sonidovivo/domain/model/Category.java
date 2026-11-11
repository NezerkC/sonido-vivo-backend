package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Homologated musical instrument and audio equipment categories.
 */
public enum Category {
    CUERDAS("Cuerdas"),
    AUDIO_PRO("Audio Pro"),
    PERCUSION("Percusion"),
    TECLADOS("Teclados"),
    VIENTOS("Vientos"),
    ACCESORIOS("Accesorios");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a {@link Category} from either its enum constant name or persistence/REST display name.
     */
    public static Category fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainDataException("Category must not be null or blank");
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        for (Category category : values()) {
            String normalizedDisplay = category.displayName.toUpperCase(Locale.ROOT);
            if (category.name().equals(normalized) || normalizedDisplay.equals(normalized)) {
                return category;
            }
        }
        throw new InvalidDomainDataException("Unknown category: " + value);
    }
}

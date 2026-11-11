package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.time.Instant;

/**
 * Immutable customer product review with validated rating (1..5) and comment length.
 */
public record Review(
        String customer,
        int rating,
        String comment,
        Instant date
) {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_COMMENT_LENGTH = 500;

    public Review {
        if (customer == null || customer.trim().length() < 2) {
            throw new InvalidDomainDataException("Reviewer name must have at least 2 characters");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new InvalidDomainDataException(
                    "Rating must be between " + MIN_RATING + " and " + MAX_RATING + ", got: " + rating
            );
        }
        String normalizedComment = comment == null ? null : comment.trim();
        if (normalizedComment != null && normalizedComment.length() > MAX_COMMENT_LENGTH) {
            throw new InvalidDomainDataException(
                    "Review comment cannot exceed " + MAX_COMMENT_LENGTH + " characters"
            );
        }
        if (date == null) {
            throw new InvalidDomainDataException("Review date must not be null");
        }
        customer = customer.trim();
        comment = normalizedComment;
    }

    public static Review create(String customer, int rating, String comment) {
        return new Review(customer, rating, comment, Instant.now());
    }
}

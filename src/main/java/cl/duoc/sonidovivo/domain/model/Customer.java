package cl.duoc.sonidovivo.domain.model;

import cl.duoc.sonidovivo.domain.exception.InvalidDomainDataException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Customer entity representing a registered buyer in Sonido Vivo.
 */
public final class Customer {

    private static final Pattern RUT_PATTERN = Pattern.compile("^\\d{1,2}(?:\\.?\\d{3}){2}-[0-9Kk]$");

    private final String id;
    private final String rut;
    private String firstName;
    private String lastName;
    private EmailAddress email;
    private String phone;
    private String commune;
    private String region;

    private Customer(
            String id,
            String rut,
            String firstName,
            String lastName,
            EmailAddress email,
            String phone,
            String commune,
            String region
    ) {
        this.id = id;
        this.rut = validateRut(rut);
        this.firstName = requireText(firstName, "First name", 2, 100);
        this.lastName = requireText(lastName, "Last name", 2, 100);
        this.email = requireNonNull(email, "Email");
        this.phone = requireText(phone, "Phone", 6, 25);
        this.commune = requireText(commune, "Commune", 2, 100);
        this.region = requireText(region, "Region", 2, 100);
    }

    public static Customer create(
            String rut,
            String firstName,
            String lastName,
            EmailAddress email,
            String phone,
            String commune,
            String region
    ) {
        return new Customer(null, rut, firstName, lastName, email, phone, commune, region);
    }

    public static Customer reconstitute(
            String id,
            String rut,
            String firstName,
            String lastName,
            EmailAddress email,
            String phone,
            String commune,
            String region
    ) {
        String validId = requireText(id, "Customer id", 1, 100);
        return new Customer(validId, rut, firstName, lastName, email, phone, commune, region);
    }

    public Customer withId(String assignedId) {
        String validId = requireText(assignedId, "Customer id", 1, 100);
        return new Customer(validId, rut, firstName, lastName, email, phone, commune, region);
    }

    public void updateProfile(
            String firstName,
            String lastName,
            EmailAddress email,
            String phone,
            String commune,
            String region
    ) {
        this.firstName = requireText(firstName, "First name", 2, 100);
        this.lastName = requireText(lastName, "Last name", 2, 100);
        this.email = requireNonNull(email, "Email");
        this.phone = requireText(phone, "Phone", 6, 25);
        this.commune = requireText(commune, "Commune", 2, 100);
        this.region = requireText(region, "Region", 2, 100);
    }

    public String getId() {
        return id;
    }

    public String getRut() {
        return rut;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCommune() {
        return commune;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customer other)) {
            return false;
        }
        return Objects.equals(rut, other.rut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rut);
    }

    private static String validateRut(String rut) {
        if (rut == null || rut.isBlank() || !RUT_PATTERN.matcher(rut.trim()).matches()) {
            throw new InvalidDomainDataException("Invalid Chilean RUT format: " + rut);
        }
        return rut.trim().toUpperCase();
    }

    private static String requireText(String value, String fieldName, int minLen, int maxLen) {
        if (value == null) {
            throw new InvalidDomainDataException(fieldName + " must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.length() < minLen || trimmed.length() > maxLen) {
            throw new InvalidDomainDataException(
                    fieldName + " length must be between " + minLen + " and " + maxLen
            );
        }
        return trimmed;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidDomainDataException(fieldName + " must not be null");
        }
        return value;
    }
}

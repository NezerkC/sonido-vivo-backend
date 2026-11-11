package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.oracle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping Oracle table {@code CLIENTES} for customer persistence.
 */
@Entity
@Table(name = "CLIENTES")
public class OracleCustomerEntity {

    @Id
    @Column(name = "ID", length = 100, nullable = false)
    private String id;

    @Column(name = "RUT", length = 20, nullable = false, unique = true)
    private String rut;

    @Column(name = "FIRST_NAME", length = 100, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 100, nullable = false)
    private String lastName;

    @Column(name = "EMAIL", length = 254, nullable = false)
    private String email;

    @Column(name = "PHONE", length = 25, nullable = false)
    private String phone;

    @Column(name = "COMMUNE", length = 100, nullable = false)
    private String commune;

    @Column(name = "REGION", length = 100, nullable = false)
    private String region;

    public OracleCustomerEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}

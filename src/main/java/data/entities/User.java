package data.entities;

import data.services.DataException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "ClientUser")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"userName"})
public class User {

    public static final String INVALID_USERNAME = "A valid username must be provided";
    public static final String POINTS_MUST_BE_GREATER_THAN_ZERO = "Points must be greater than zero";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    private String userName;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Person person;
    // password must not escape by any means out of this object
    private String password;
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "purchaser")
    private List<Sale> purchases;
    private int points;

    public User(Person person, String userName, String password) {
        this.person = person;
        this.userName = new NotBlankString(userName,
                INVALID_USERNAME).value();
        this.password = password;
        this.points = 0;
        this.purchases = new ArrayList<>();
    }

    public boolean hasPassword(String password) {
        return this.password.equals(password);
    }

    public void setNewPassword(String newPassword) {
        this.password = newPassword;
    }

    public void newEarnedPoints(int points) {
        if (points <= 0) {
            throw new DataException(POINTS_MUST_BE_GREATER_THAN_ZERO);
        }
        this.points += points;
    }

    public boolean hasPoints(int points) {
        return this.points == points;
    }

    public String userName() {
        return userName;
    }

    public boolean hasName(String aName) {
        return this.person.hasName(aName);
    }

    public boolean hasSurname(String aSurname) {
        return this.person.aSurname(aSurname);
    }

    public boolean hasUsername(String aUserName) {
        return this.userName.equals(aUserName);
    }

    void newPurchase(Sale sale, int pointsWon) {
        this.newEarnedPoints(pointsWon);
        this.purchases.add(sale);
    }

    public String email() {
        return this.person.email();
    }

    public Map<String, Object> toMap() {
        return Map.of("id", this.id);
    }

    public Long id() {
        return id;
    }

    public String fullName() {
        return this.person.fullName();
    }

    public int points() {
        return this.points;
    }
}

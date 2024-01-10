package data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"name"})
public class Theater {

    static final String NAME_INVALID = "Theater name cannot be blank";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    private String name;
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<Integer> seatNumbers;

    public Theater(String name, Set<Integer> seats) {
        this.name = new NotBlankString(name, NAME_INVALID).value();
        this.seatNumbers = seats;
    }

    Set<ShowSeat> seatsForShow(ShowTime show) {
        return this.seatNumbers.stream()
                .map(s -> new ShowSeat(show, s))
                .collect(Collectors.toUnmodifiableSet());
    }

    public String name() {
        return name;
    }

    public Long id() {
        return id;
    }
}

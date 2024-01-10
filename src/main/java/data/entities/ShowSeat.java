package data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    private boolean reserved;
    @Getter
    private boolean confirmed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_show")
    private ShowTime show;
    private LocalDateTime reservedUntil;
    private Integer seatNumber;
    @Version
    private int version;

    public ShowSeat(ShowTime s, Integer seatNumber) {
        this.show = s;
        this.seatNumber = seatNumber;
        this.reserved = false;
        this.confirmed = false;
    }

    public boolean isBusy() {
        return !isAvailable();
    }

    public boolean isAvailable() {
        return (!reserved || LocalDateTime.now().isAfter(this.reservedUntil)) && !confirmed;
    }

    public boolean isConfirmedBy(User user) {
        if (this.user == null) {
            return false;
        }
        return confirmed && this.user.equals(user);
    }

    public boolean isReservedBy(User user) {
        if (this.user == null) {
            return false;
        }
        return reserved && this.user.equals(user)
                && LocalDateTime.now().isBefore(this.reservedUntil);
    }

    public boolean isSeatNumbered(int aSeatNumber) {
        return this.seatNumber.equals(aSeatNumber);
    }

    public int seatNumber() {
        return seatNumber;
    }

    public void reservedBy(User user) {
        this.user = user;
    }

    public void confirmedBy(User user) {
        this.user = user;
    }

    public void reserve() {
        this.reserved = true;
    }

    public void confirm() {
        this.confirmed = true;
    }

    public void reservedUntil(LocalDateTime time) {
        this.reservedUntil = time;
    }
}

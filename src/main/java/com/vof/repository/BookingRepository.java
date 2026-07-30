package com.vof.repository;
import com.vof.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = "aPackage")
    Optional<Booking> findByBookingId(String bookingId);

    @Override
    @EntityGraph(attributePaths = "aPackage")
    List<Booking> findAll();

    @Query("SELECT b FROM Booking b WHERE b.status <> 'DELETED'")
    List<Booking> findAllNotDeleted();

    @Query("SELECT b FROM Booking b WHERE b.bookingId = ?1 AND b.status <> 'DELETED'")
    Optional<Booking> findByBookingIdAndNotDeleted(String bookingId);

    List<Booking> findAllByCreatedByOrderByCreatedAtDesc(com.vof.entity.User user);
}

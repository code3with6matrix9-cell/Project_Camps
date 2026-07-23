package com.vof.repository;
import com.vof.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = "aPackage")
    Optional<Booking> findByBookingId(String bookingId);

    @Override
    @EntityGraph(attributePaths = "aPackage")
    java.util.List<Booking> findAll();
}

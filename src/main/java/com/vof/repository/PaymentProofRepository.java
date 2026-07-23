package com.vof.repository;
import com.vof.entity.PaymentProof;
import com.vof.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PaymentProofRepository extends JpaRepository<PaymentProof, Long> {
    boolean existsByBooking(Booking booking);
    boolean existsByUtr(String utr);
}

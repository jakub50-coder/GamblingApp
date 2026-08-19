/*Database access layer for transactions which finds all transation belonging to a specific user
*/
package com.gambingapp.gaminghub.repository;

import com.gambingapp.gaminghub.model.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {
    List<CoinTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<CoinTransaction> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
        Long userId, LocalDateTime after
    );
    List<CoinTransaction> findByRoundId(String roundId);
}
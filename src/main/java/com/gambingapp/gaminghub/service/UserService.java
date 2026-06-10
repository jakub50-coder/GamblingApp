/*The service layer completes what the controller asks and where all the business rules and logic lives
*/
//Transactional either you do the method or not or when something crashes, then nothing is saved and there is a clean slate
//Optional is used when a value does or doesn't exist.
package com.gambingapp.gaminghub.service;

import com.gambingapp.gaminghub.model.User;
import com.gambingapp.gaminghub.model.CoinTransaction;
import com.gambingapp.gaminghub.repository.UserRepository;
import com.gambingapp.gaminghub.repository.CoinTransactionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,CoinTransactionRepository coinTransactionRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.coinTransactionRepository = coinTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Creates a new user account with 100 starting coins
    @Transactional
    public boolean signup(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        User user = new User(username, passwordEncoder.encode(password));
        userRepository.save(user);

        CoinTransaction startingCoins = new CoinTransaction(user, 100, "REFILL", null);
        coinTransactionRepository.save(startingCoins);
        return true;
    }

    // Validates username and password on login
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    // Fetches a user and runs the coin refill check
    @Transactional
    public Optional<User> getUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(this::checkAndRefillCoins);
        return userOpt;
    }

    // Restores coins to 100 if below 100 and 24h have passed
    @Transactional
    public void checkAndRefillCoins(User user) {
        if (user.getCoins() >= 100) {
            return;
        }
        Duration timeSinceRefill = Duration.between(
            user.getLastRefillAt(), LocalDateTime.now()
        );
        if (timeSinceRefill.toHours() >= 24) {
            int coinsToAdd = 100 - user.getCoins();
            user.setCoins(100);
            user.setLastRefillAt(LocalDateTime.now());
            userRepository.save(user);

            CoinTransaction refill = new CoinTransaction(
                user, coinsToAdd, "REFILL", null
            );
            coinTransactionRepository.save(refill);
        }
    }

    // Deducts coins for a bet — enforces min 10, max 75, and balance floor
    @Transactional
    public boolean placeBet(String username, int betAmount, String roundId) {
        if (betAmount < 10 || betAmount > 75) {
            return false;
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (user.getCoins() < betAmount) {
            return false;
        }
        user.setCoins(user.getCoins() - betAmount);
        userRepository.save(user);

        CoinTransaction bet = new CoinTransaction(user, -betAmount, "BET", "blackjack", roundId);
        coinTransactionRepository.save(bet);
        return true;
    }

    // Awards winnings to a player after a win
    @Transactional
    public void awardWinnings(String username, int amount, String game, String roundId) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        user.setCoins(user.getCoins() + amount);
        userRepository.save(user);

        CoinTransaction win = new CoinTransaction(user, amount, "WIN", game, roundId);
        coinTransactionRepository.save(win);
    }
    //Old Versions of awardWinnings and placeBet for when the newer versions don't work
    @Transactional
    public boolean placeBet(String username, int betAmount){
        return placeBet(username, betAmount, null);
    }
    @Transactional
    public void awardWinnings(String username, int amount, String game){
        awardWinnings(username, amount, game, null);
    }

    // Returns last 7 days of coin transactions for a user
    public List<CoinTransaction> getTransactionHistory(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return coinTransactionRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                userOpt.get().getId(), oneWeekAgo
            );
    }

    // Sets the tutorial seen flag to true once player closes the walkthrough
    @Transactional
    public void markTutorialSeen(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(user -> {
            user.setHasSeenBlackjackTutorial(true);
            userRepository.save(user);
        });
    }
    //Changes a user password after checking the old one
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

        if(!passwordEncoder.matches(currentPassword, user.getPassword())){
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
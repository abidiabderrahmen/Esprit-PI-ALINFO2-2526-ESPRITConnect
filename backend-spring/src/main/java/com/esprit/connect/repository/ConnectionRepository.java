package com.esprit.connect.repository;

import com.esprit.connect.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    List<Connection> findAcceptedConnections(@Param("uid") Long userId);

    @Query("SELECT c FROM Connection c WHERE c.receiver.id = :uid AND c.status = 'PENDING'")
    List<Connection> findPendingForUser(@Param("uid") Long userId);

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :uid1 AND c.receiver.id = :uid2) OR (c.sender.id = :uid2 AND c.receiver.id = :uid1)")
    Optional<Connection> findBetweenUsers(@Param("uid1") Long uid1, @Param("uid2") Long uid2);

    @Query("SELECT COUNT(c) FROM Connection c WHERE (c.sender.id = :uid OR c.receiver.id = :uid) AND c.status = 'ACCEPTED'")
    long countAcceptedConnections(@Param("uid") Long userId);
}

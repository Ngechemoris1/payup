package payup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import payup.payup.model.Communication;
import payup.payup.model.User;


import java.util.List;

public interface CommunicationRepository extends JpaRepository<Communication, Long> {
    List<Communication> findBySender(User sender);
    List<Communication> findByReceiver(User receiver);
    List<Communication> findBySenderAndReceiver(User sender, User receiver);
}

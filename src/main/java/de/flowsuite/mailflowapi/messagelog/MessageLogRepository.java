package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MessageLogRepository extends CrudRepository<MessageLogEntry, Long> {
    Iterable<MessageLogEntry> findByCustomerId(long customerId);
}

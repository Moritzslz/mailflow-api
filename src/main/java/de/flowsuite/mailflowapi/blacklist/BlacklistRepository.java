package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface BlacklistRepository extends CrudRepository<BlacklistEntry, Long> {
    List<BlacklistEntry> findByUserId(long userId);
    boolean existsByUserIdAndBlacklistedEmailAddressHash(long userId, String emailAddressHash);
}

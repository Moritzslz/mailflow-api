package de.flowsuite.mailflowapi.messagecategory;

import de.flowsuite.mailflowcommon.entity.MessageCategory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface MessageCategoryRepository extends CrudRepository<MessageCategory, Long> {

    List<MessageCategory> findByCustomerId(long customerId);

    boolean existsByCategory(String category);
}

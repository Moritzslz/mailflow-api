package de.flowsuite.mailflow.api.messagecategory;

import de.flowsuite.mailflow.common.entity.MessageCategory;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface MessageCategoryRepository extends CrudRepository<MessageCategory, Long> {

    List<MessageCategory> findByCustomerId(long customerId);

    boolean existsByCustomerIdAndCategory(long customerId, String category);
}

package de.flowsuite.mailflowapi.ragurl;

import de.flowsuite.mailflowapi.common.entity.RagUrl;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface RagUrlRepository extends CrudRepository<RagUrl, Long> {

    List<RagUrl> findByCustomerId(long customerId);

    boolean existsByUrl(String url);
}

package de.flowsuite.mailflowapi.ragurl;

import de.flowsuite.mailflowapi.common.entity.RagUrl;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class RagUrlService {

    private final RagUrlRepository ragUrlRepository;

    RagUrlService(RagUrlRepository ragUrlRepository) {
        this.ragUrlRepository = ragUrlRepository;
    }

    RagUrl createRagUrl(long customerId, RagUrl ragUrl, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        if (!ragUrl.getCustomerId().equals(customerId)) {
            throw new IdConflictException();
        }

        return ragUrlRepository.save(ragUrl);
    }

    List<RagUrl> listRagUrls(long customerId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        return ragUrlRepository.findByCustomerId(customerId);
    }

    void deleteRagUrl(long customerId, long ragUrlId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        RagUrl ragUrl =
                ragUrlRepository
                        .findById(ragUrlId)
                        .orElseThrow(
                                () -> new EntityNotFoundException(RagUrl.class.getSimpleName()));

        if (!ragUrl.getCustomerId().equals(customerId)) {
            throw new IdorException();
        }

        ragUrlRepository.delete(ragUrl);
    }
}

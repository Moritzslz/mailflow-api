package de.flowsuite.mailflow.api.ragurl;

import de.flowsuite.mailflow.common.entity.RagUrl;
import de.flowsuite.mailflow.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflow.common.exception.EntityNotFoundException;
import de.flowsuite.mailflow.common.exception.IdConflictException;
import de.flowsuite.mailflow.common.exception.IdorException;
import de.flowsuite.mailflow.common.util.AuthorisationUtil;
import de.flowsuite.mailflow.common.util.Util;

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

        if (ragUrl.getId() != null || !ragUrl.getCustomerId().equals(customerId)) {
            throw new IdConflictException();
        }

        String url = ragUrl.getUrl();
        Util.validateUrl(url);

        if (ragUrlRepository.existsByCustomerIdAndUrl(customerId, url)) {
            throw new EntityAlreadyExistsException(RagUrl.class.getSimpleName());
        }

        return ragUrlRepository.save(ragUrl);
    }

    RagUrl getRagUrl(long customerId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        RagUrl ragUrl =
                ragUrlRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(RagUrl.class.getSimpleName()));

        if (!ragUrl.getCustomerId().equals(customerId)) {
            throw new IdorException();
        }

        return ragUrl;
    }

    List<RagUrl> listRagUrls(long customerId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        return ragUrlRepository.findByCustomerId(customerId);
    }

    void deleteRagUrl(long customerId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        RagUrl ragUrl =
                ragUrlRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException(RagUrl.class.getSimpleName()));

        if (!ragUrl.getCustomerId().equals(customerId)) {
            throw new IdorException();
        }

        ragUrlRepository.delete(ragUrl);
    }
}

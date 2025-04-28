package de.flowsuite.mailflowapi.ragurl;

import de.flowsuite.mailflowcommon.entity.RagUrl;
import de.flowsuite.mailflowcommon.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowcommon.exception.EntityNotFoundException;
import de.flowsuite.mailflowcommon.exception.IdConflictException;
import de.flowsuite.mailflowcommon.exception.IdorException;
import de.flowsuite.mailflowcommon.util.AuthorisationUtil;
import de.flowsuite.mailflowcommon.util.Util;

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

package de.flowsuite.mailflowapi.ragurl;

import de.flowsuite.mailflowapi.common.entity.RagUrl;
import de.flowsuite.mailflowapi.common.exception.EntityAlreadyExistsException;
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

        if (ragUrl.getId() != null || !ragUrl.getCustomerId().equals(customerId)) {
            throw new IdConflictException();
        }

        String url = ragUrl.getUrl().trim();

        if (!url.contains("://")) {
            url = "https://" + url;
        } else if (url.toLowerCase().startsWith("http://")) {
            url = "https://" + url.substring(7);
        }

        ragUrl.setUrl(url);

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

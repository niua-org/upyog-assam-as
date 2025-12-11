package org.egov.bpa.service;

import java.util.concurrent.ConcurrentHashMap;

import org.egov.bpa.util.BPAUtil;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service responsible for caching MDMS (Master Data Management System) responses.
 *
 * <p>This cache prevents repeated MDMS API calls for the same tenantId,
 * significantly improving performance and reducing network load.</p>
 *
 * <h3>Why ConcurrentHashMap?</h3>
 * <p>Since MDMS responses are accessed by multiple threads handling parallel
 * requests, {@link ConcurrentHashMap} ensures thread-safety without locking
 * the entire map. It supports high concurrency and atomic operations such as
 * {@code computeIfAbsent}, making it ideal for request-heavy applications.</p>
 *
 * <h3>Cache Behavior</h3>
 * <ul>
 *     <li>Each tenantId (e.g., <code>as</code>, <code>as.barpetagp</code>)
 *         is stored as a separate cache key.</li>
 *     <li>Once MDMS data is retrieved for a tenantId, subsequent calls will
 *         serve data directly from the in-memory cache.</li>
 *     <li>Cache can be cleared manually (e.g., during master data refresh).</li>
 * </ul>
 */
@Service
public class MdmsCacheService {

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    @Autowired
    private BPAUtil util;

    public Object getMdmsData(RequestInfo requestInfo, String tenantId) {
        return cache.computeIfAbsent(tenantId, key -> {
          
            return util.mDMSCall(requestInfo, key);
        });
    }

    public void clearCache() {
        cache.clear();
    }
}

package com.freedom.saving;

import com.freedom.saving.application.policy.ProductSnapshotSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 어드민용 금감원(FSS) 적금 상품 스냅샷 동기화 서비스
 * 직접 FSS API를 호출하여 동기화를 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSavingSyncService {

    private final ProductSnapshotSyncService syncService;

    /**
     * FSS API를 직접 호출하여 적금 데이터를 동기화합니다.
     */
    public ProductSnapshotSyncService.SyncResult syncAll() {
        log.info("[ADMIN] FSS 적금 데이터 동기화 시작");
        
        try {
            ProductSnapshotSyncService.SyncResult result = syncService.syncAll();
            log.info("[ADMIN] FSS 적금 데이터 동기화 완료: pages={}, products={}, options={}, skipped={}",
                    result.getPages(), result.getProducts(), result.getOptions(), result.getSkipped());
            return result;
        } catch (Exception e) {
            log.error("[ADMIN] FSS 적금 데이터 동기화 중 오류 발생", e);
            throw new RuntimeException("FSS 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

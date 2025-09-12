package com.freedom.saving;

import com.freedom.saving.application.policy.ProductSnapshotSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/saving")
@RequiredArgsConstructor
public class AdminSavingController {

    private final AdminSavingSyncService syncService;

    @PostMapping("/fss-sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductSnapshotSyncService.SyncResult> syncFssData() {
        log.info("[ADMIN] FSS 적금 데이터 동기화 요청");
        ProductSnapshotSyncService.SyncResult result = syncService.syncAll();
        log.info("[ADMIN] FSS 적금 데이터 동기화 완료: pages={}, products={}, options={}, skipped={}",
                result.getPages(), result.getProducts(), result.getOptions(), result.getSkipped());
        return ResponseEntity.ok(result);
    }
}

package com.freedom.admin.api;

import com.freedom.common.exception.SuccessResponse;
import com.freedom.saving.application.job.FssSnapshotSyncJob;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/saving")
@RequiredArgsConstructor
public class AdminSavingController {

	private final FssSnapshotSyncJob syncJob;

	@PostMapping("/fss-sync")
	public ResponseEntity<SuccessResponse> triggerFssSync() {
		syncJob.runOnce();
		return ResponseEntity.ok(SuccessResponse.ok("금감원 적금 동기화가 트리거되었습니다."));
	}
}

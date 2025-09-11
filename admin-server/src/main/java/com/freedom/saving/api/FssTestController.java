package com.freedom.saving.api;

import com.freedom.saving.infra.fss.FssSavingApiClient;
import com.freedom.saving.infra.fss.FssSavingResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * FSS API 테스트용 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/test")
@RequiredArgsConstructor
public class FssTestController {

    private final FssSavingApiClient fssClient;

    @GetMapping("/fss")
    public ResponseEntity<?> testFssApi(@RequestParam(defaultValue = "1") int pageNo) {
        log.info("[TEST] FSS API 테스트 시작 - pageNo: {}", pageNo);
        
        try {
            Mono<FssSavingResponseDto> response = fssClient.fetchSavings("020000", pageNo);
            FssSavingResponseDto result = response.block();
            
            if (result == null) {
                log.warn("[TEST] FSS API 응답이 null입니다.");
                return ResponseEntity.ok().body("FSS API 응답이 null입니다.");
            }
            
            if (result.result == null) {
                log.warn("[TEST] FSS API result가 null입니다.");
                return ResponseEntity.ok().body("FSS API result가 null입니다.");
            }
            
            log.info("[TEST] FSS API 응답 성공 - errCd: {}, nowPage: {}, maxPage: {}, baseList: {}, optionList: {}", 
                    result.result.errCd, result.result.nowPageNo, result.result.maxPageNo,
                    result.result.baseList != null ? result.result.baseList.size() : 0,
                    result.result.optionList != null ? result.result.optionList.size() : 0);
            
            return ResponseEntity.ok().body(result);
            
        } catch (Exception e) {
            log.error("[TEST] FSS API 호출 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("FSS API 호출 중 오류: " + e.getMessage());
        }
    }
}

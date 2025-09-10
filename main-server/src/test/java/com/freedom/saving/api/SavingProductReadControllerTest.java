package com.freedom.saving.api;

import com.freedom.common.exception.GlobalExceptionHandler;
import com.freedom.common.exception.custom.SavingProductNotFoundException;
import com.freedom.saving.application.SavingProductReadService;
import com.freedom.saving.application.read.SavingProductDetail;
import com.freedom.saving.application.read.SavingProductListItem;
import com.freedom.saving.application.read.SavingProductOptionItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 컨트롤러 슬라이스 테스트
 * - @WebMvcTest: MVC 레이어만 로드하여 빠르고 독립적으로 검증
 * - @MockBean: 애플리케이션 서비스 스텁 주입
 * - @Import(GlobalExceptionHandler): 전역 예외 포맷/상태코드 검증을 위해 명시 등록
 *
 * 케이스
 *  1) 목록 200: /api/products?type=SAVING&sort=popular&page=0&size=2
 *  2) 검증 400: page가 음수일 때 @Min(0) 위반 → 400 + Validation 에러 포맷
 *  3) 상세 200: /api/products/{id}
 *  4) 상세 404: 서비스가 SavingProductNotFoundException 던질 때 PRODUCT001(404) 매핑
 */
@WebMvcTest(
        controllers = SavingProductReadController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = com.freedom.common.config.SecurityConfig.class // 시큐리티 구성 제외
                ),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = com.freedom.common.security.JwtAuthenticationFilter.class // JWT 필터 제외
                ),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class SavingProductReadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavingProductReadService readService;

    @Test
    @DisplayName("목록 200 - 인기순(임시:fetchedAt DESC) 페이지 반환")
    void list_popular_success_200() throws Exception {
        SavingProductListItem item = new SavingProductListItem();
        item.setProductSnapshotId(10L);
        item.setProductName("적금A");
        item.setBankName("은행A");
        item.setAiSummary("");

        Mockito.when(readService.getPopularSavingProducts(0, 2))
                .thenReturn(new PageImpl<>(
                        Collections.singletonList(item),
                        org.springframework.data.domain.PageRequest.of(0, 2),
                        1
                ));

        mockMvc.perform(get("/api/products")
                        .param("type", "SAVING")
                        .param("sort", "popular")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productSnapshotId", is(10)))
                .andExpect(jsonPath("$.content[0].productName", is("적금A")))
                .andExpect(jsonPath("$.content[0].bankName", is("은행A")));
    }

    @Test
    @DisplayName("목록 400 - page가 음수이면 @Min(0) 위반으로 400")
    void list_validation_error_page_negative_400() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("type", "SAVING")
                        .param("sort", "popular")
                        .param("page", "-1")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION001")))
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.errors[0].field", is("page")))
                .andExpect(jsonPath("$.errors[0].code", is("Min")));
    }

    @Test
    @DisplayName("상세 200 - 헤더 + 옵션 목록 반환")
    void detail_success_200() throws Exception {
        SavingProductDetail detail = new SavingProductDetail();
        detail.setProductSnapshotId(123L);
        detail.setProductName("적금A");
        detail.setBankName("은행A");
        detail.setJoinWay("인터넷");
        detail.setSpecialCondition("우대 없음");
        detail.setJoinDeny("1");
        detail.setJoinMember("제한 없음");
        detail.setEtcNote("");
        detail.setFetchedAt(LocalDateTime.now());
        detail.setAiSummary("");

        SavingProductOptionItem opt = new SavingProductOptionItem();
        opt.setTermMonths(12);
        opt.setRate(new BigDecimal("3.20"));
        opt.setRatePreferential(new BigDecimal("3.50"));
        opt.setRateType("S");
        opt.setRateTypeName("단리");
        detail.setOptions(Arrays.asList(opt));

        Mockito.when(readService.getDetail(123L)).thenReturn(detail);

        mockMvc.perform(get("/api/products/{id}", 123L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productSnapshotId", is(123)))
                .andExpect(jsonPath("$.productName", is("적금A")))
                .andExpect(jsonPath("$.bankName", is("은행A")))
                .andExpect(jsonPath("$.options", hasSize(1)))
                .andExpect(jsonPath("$.options[0].termMonths", is(12)));
    }

    @Test
    @DisplayName("상세 404 - 존재하지 않는 ID면 PRODUCT001 반환")
    void detail_not_found_404() throws Exception {
        Mockito.when(readService.getDetail(999L))
                .thenThrow(new SavingProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/{id}", 999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("PRODUCT001")));
    }
}

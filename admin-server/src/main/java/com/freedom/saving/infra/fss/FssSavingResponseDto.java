package com.freedom.saving.infra.fss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FssSavingResponseDto {

    @JsonProperty("result")
    public Result result;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("prdt_div")
        public String prdtDiv;              // "S" (적금)

        @JsonProperty("total_count")
        public Integer totalCount;          // 총 상품 건수

        @JsonProperty("max_page_no")
        public Integer maxPageNo;           // 총 페이지 건수

        @JsonProperty("now_page_no")
        public Integer nowPageNo;           // 현재 조회 페이지 번호

        @JsonProperty("err_cd")
        public String errCd;                // 응답 코드

        @JsonProperty("err_msg")
        public String errMsg;               // 응답 메시지

        @JsonProperty("baseList")
        public List<Base> baseList;         // 상품 기본정보 목록

        @JsonProperty("optionList")
        public List<Option> optionList;     // 옵션(기간/금리) 목록
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Base {

        @JsonProperty("dcls_month")
        public String dclsMonth;            // 공시 제출월(YYYYMM)

        @JsonProperty("fin_co_no")
        public String finCoNo;              // 금융회사 코드

        @JsonProperty("kor_co_nm")
        public String korCoNm;              // 금융 회사 명

        @JsonProperty("fin_prdt_cd")
        public String finPrdtCd;            // 금융 상품 코드

        @JsonProperty("fin_prdt_nm")
        public String finPrdtNm;            // 상품명

        @JsonProperty("join_way")
        public String joinWay;              // 가입 방법

        @JsonProperty("mtrt_int")
        public String mtrtInt;              // 만기 후 이자율

        @JsonProperty("spcl_cnd")
        public String spclCnd;              // 우대조건

        @JsonProperty("join_deny")
        public String joinDeny;             // 가입제한 여부(Ex 1:제한없음, 2:서민전용, 3:일부제한)

        @JsonProperty("join_member")
        public String joinMember;           // 가입 대상

        @JsonProperty("etc_note")
        public String etcNote;              // 기타 유의사항

        @JsonProperty("max_limit")
        public Integer maxLimit;            // 최고 한도

        @JsonProperty("dcls_strt_day")
        public String dclsStrtDay;          // 공시 시작일

        @JsonProperty("dcls_end_day")
        public String dclsEndDay;           // 공시 종료일

        @JsonProperty("fin_co_subm_day")
        public String finCoSubmDay;         // 금융회사 제출일(YYYYMMDDHH24MI)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Option {

        @JsonProperty("dcls_month")
        public String dclsMonth;            // 공시월

        @JsonProperty("fin_co_no")
        public String finCoNo;              // 금융회사 코드

        @JsonProperty("fin_prdt_cd")
        public String finPrdtCd;            // 상품 코드

        @JsonProperty("intr_rate_type")
        public String intrRateType;         // 저축 금리 유형

        @JsonProperty("intr_rate_type_nm")
        public String intrRateTypeNm;       // 저축 금리 유형명

        @JsonProperty("save_trm")
        public String saveTrm;              // 저축 기간(개월) → 문자열로 받아두고, 사용 시 Integer.parseInt()

        @JsonProperty("intr_rate")
        public Double intrRate;             // 저축 금리 [소수점 2자리]

        @JsonProperty("intr_rate2")
        public Double intrRate2;            // 최고 우대 금리 [소수점 2자리]
    }
}

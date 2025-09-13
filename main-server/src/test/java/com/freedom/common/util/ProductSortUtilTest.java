package com.freedom.common.util;

import com.freedom.saving.util.ProductSortUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductSortUtil 테스트")
class ProductSortUtilTest {

    @Test
    @DisplayName("sortByProductName - 상품명 가나다순 정렬")
    void sortByProductName_ShouldSortInKoreanOrder() {
        // Given
        List<TestProduct> products = Arrays.asList(
            new TestProduct("다라마 적금"),
            new TestProduct("가나다 적금"),
            new TestProduct("나다라 적금"),
            new TestProduct("하하하 적금"),
            new TestProduct("바바바 적금")
        );

        // When
        ProductSortUtil.sortByProductName(products, TestProduct::getName);

        // Then
        assertThat(products).extracting(TestProduct::getName)
            .containsExactly(
                "가나다 적금",
                "나다라 적금",
                "다라마 적금",
                "바바바 적금",
                "하하하 적금"
            );
    }


    @Test
    @DisplayName("sortByProductName - null 값이 포함된 경우 정상 처리")
    void sortByProductName_WithNullValues_ShouldHandleGracefully() {
        // Given
        List<TestProduct> products = Arrays.asList(
            new TestProduct("다라마 적금"),
            new TestProduct(null),
            new TestProduct("가나다 적금"),
            new TestProduct("나다라 적금")
        );

        // When
        ProductSortUtil.sortByProductName(products, TestProduct::getName);

        // Then
        assertThat(products).extracting(TestProduct::getName)
            .containsExactly(
                null,
                "가나다 적금",
                "나다라 적금",
                "다라마 적금"
            );
    }

    @Test
    @DisplayName("sortByProductName - 빈 리스트 처리")
    void sortByProductName_WithEmptyList_ShouldNotThrowException() {
        // Given
        List<TestProduct> products = new ArrayList<>();

        // When & Then
        ProductSortUtil.sortByProductName(products, TestProduct::getName);
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("sortByProductName - 단일 요소 리스트 처리")
    void sortByProductName_WithSingleElement_ShouldNotChangeOrder() {
        // Given
        List<TestProduct> products = Arrays.asList(new TestProduct("가나다 적금"));

        // When
        ProductSortUtil.sortByProductName(products, TestProduct::getName);

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("가나다 적금");
    }

    @Test
    @DisplayName("sortByProductName - 대소문자 구분하지 않음")
    void sortByProductName_ShouldIgnoreCase() {
        // Given
        List<TestProduct> products = Arrays.asList(
            new TestProduct("다라마 적금"),
            new TestProduct("가나다 적금"),
            new TestProduct("GANA 적금"),
            new TestProduct("나다라 적금")
        );

        // When
        ProductSortUtil.sortByProductName(products, TestProduct::getName);

        // Then
        assertThat(products).extracting(TestProduct::getName)
            .containsExactly(
                "가나다 적금",
                "GANA 적금",
                "나다라 적금",
                "다라마 적금"
            );
    }

    @Test
    @DisplayName("getProductNameComparator - Comparator 반환 확인")
    void getProductNameComparator_ShouldReturnValidComparator() {
        // Given
        List<TestProduct> products = Arrays.asList(
            new TestProduct("다라마 적금"),
            new TestProduct("가나다 적금")
        );

        // When
        products.sort(ProductSortUtil.getProductNameComparator(TestProduct::getName));

        // Then
        assertThat(products).extracting(TestProduct::getName)
            .containsExactly("가나다 적금", "다라마 적금");
    }


    // 테스트용 내부 클래스
    private static class TestProduct {
        private final String name;

        public TestProduct(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

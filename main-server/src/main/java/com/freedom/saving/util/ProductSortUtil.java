package com.freedom.saving.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 금융 상품 정렬을 위한 공통 유틸리티 클래스
 * 예금, 적금 상품 모두에서 사용할 수 있는 정렬 기능을 제공
 */
public class ProductSortUtil {

    /**
     * 상품 이름을 가나다순으로 정렬하는 Comparator
     * 한글 정렬을 위해 Collator를 사용하여 자연스러운 정렬을 제공
     */
    public static <T> Comparator<T> getProductNameComparator(Function<T, String> nameExtractor) {
        Collator collator = Collator.getInstance(java.util.Locale.KOREAN);
        collator.setStrength(Collator.PRIMARY); // 대소문자 구분하지 않음
        
        return (o1, o2) -> {
            String name1 = nameExtractor.apply(o1);
            String name2 = nameExtractor.apply(o2);
            
            if (name1 == null && name2 == null) return 0;
            if (name1 == null) return -1;
            if (name2 == null) return 1;
            
            return collator.compare(name1, name2);
        };
    }

    /**
     * 리스트를 상품 이름 가나다순으로 정렬
     */
    public static <T> void sortByProductName(List<T> list, Function<T, String> nameExtractor) {
        list.sort(getProductNameComparator(nameExtractor));
    }
}

package com.freedom.news.domain.service;

import com.freedom.news.application.dto.NewsArticleDto;
import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.news.infra.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsPersistenceService {
    private final NewsArticleRepository newsArticleRepository;

    public List<NewsArticle> saveNewArticles(List<NewsArticleDto> newArticles) {
        List<NewsArticle> savedEntities = new ArrayList<>();
        
        for (NewsArticleDto dto : newArticles) {
            try {
                // 먼저 기존 뉴스가 있는지 확인
                NewsArticle existingArticle = newsArticleRepository.findByNewsItemId(dto.getNewsItemId())
                        .orElse(null);
                
                if (existingArticle != null) {
                    // 기존 뉴스가 있으면 업데이트
                    log.info("기존 뉴스 업데이트: newsItemId={}", dto.getNewsItemId());
                    existingArticle.updateFromDto(dto);
                    savedEntities.add(newsArticleRepository.save(existingArticle));
                } else {
                    // 새 뉴스 저장
                    NewsArticle newEntity = dto.toEntity();
                    savedEntities.add(newsArticleRepository.save(newEntity));
                }
            } catch (DataIntegrityViolationException e) {
                // 동시성 문제로 인한 중복 삽입 시도 시 재시도
                log.warn("뉴스 저장 중 중복 오류 발생, 업데이트로 재시도: newsItemId={}", dto.getNewsItemId());
                try {
                    NewsArticle existingArticle = newsArticleRepository.findByNewsItemId(dto.getNewsItemId())
                            .orElseThrow(() -> new IllegalStateException("뉴스를 찾을 수 없습니다: " + dto.getNewsItemId()));
                    
                    existingArticle.updateFromDto(dto);
                    savedEntities.add(newsArticleRepository.save(existingArticle));
                } catch (Exception updateException) {
                    log.error("뉴스 업데이트 실패: newsItemId={}", dto.getNewsItemId(), updateException);
                    // 개별 뉴스 저장 실패해도 전체 프로세스는 계속 진행
                }
            } catch (Exception e) {
                log.error("뉴스 저장 실패: newsItemId={}", dto.getNewsItemId(), e);
                // 개별 뉴스 저장 실패해도 전체 프로세스는 계속 진행
            }
        }
        
        return savedEntities;
    }

    public void updateArticles(List<NewsArticleDto> updatedArticles) {
        updatedArticles.forEach(dto -> {
            try {
                NewsArticle entity = newsArticleRepository.findByNewsItemId(dto.getNewsItemId())
                        .orElseThrow(() -> new IllegalStateException("기존 뉴스가 존재하지 않습니다. id=" + dto.getNewsItemId()));

                entity.updateFromDto(dto);
                newsArticleRepository.save(entity);
            } catch (Exception e) {
                log.error("뉴스 업데이트 실패: newsItemId={}", dto.getNewsItemId(), e);
                // 개별 뉴스 업데이트 실패해도 전체 프로세스는 계속 진행
            }
        });
    }
}

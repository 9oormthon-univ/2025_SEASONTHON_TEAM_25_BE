package com.freedom.news.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "news_content_block")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsContentBlock extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_article_id")
    private NewsArticle newsArticle;
    
    @Column(name = "news_article_id", nullable = false, insertable = false, updatable = false)
    private Long newsArticleId;
    
    @Column(name = "block_order", nullable = false)
    private Integer blockOrder;
    
    @Column(name = "block_type", length = 20, nullable = false)
    private String blockType;
    
    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;
    
    @Column(name = "plain_content", columnDefinition = "TEXT")
    private String plainContent;
    
    @Column(name = "url", length = 1000)
    private String url;
    
    @Column(name = "alt_text", length = 500)
    private String altText;
    
    @Builder
    public NewsContentBlock(NewsArticle newsArticle, Long newsArticleId, Integer blockOrder, String blockType, 
                           String originalContent, String plainContent, String url, String altText) {
        this.newsArticle = newsArticle;
        this.newsArticleId = newsArticleId;
        this.blockOrder = blockOrder;
        this.blockType = blockType;
        this.originalContent = originalContent;
        this.plainContent = plainContent;
        this.url = url;
        this.altText = altText;
    }
}

package com.freedom.news.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "news_article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsArticle extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "news_item_id", length = 10, unique = true, nullable = false)
    private String newsItemId;
    
    @Column(name = "contents_status", length = 1, nullable = false)
    private String contentsStatus;
    
    @Column(name = "modify_id")
    private Integer modifyId;
    
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;
    
    @Column(name = "approve_date")
    private LocalDateTime approveDate;
    
    @Column(name = "approver_name", length = 50)
    private String approverName;
    
    @Column(name = "embargo_date")
    private LocalDateTime embargoDate;
    
    @Column(name = "grouping_code", length = 20)
    private String groupingCode;
    
    @Column(name = "title", length = 400, nullable = false)
    private String title;
    
    @Column(name = "sub_title1", length = 200)
    private String subTitle1;
    
    @Column(name = "sub_title2", length = 200)  
    private String subTitle2;
    
    @Column(name = "sub_title3", length = 200)
    private String subTitle3;
    
    @Column(name = "contents_type", length = 1, nullable = false)
    private String contentsType;
    
    @Column(name = "data_contents", columnDefinition = "TEXT")
    private String dataContents;
    
    @Column(name = "plain_text_content", columnDefinition = "TEXT")
    private String plainTextContent;
    
    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "minister_code", length = 20)
    private String ministerCode;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column(name = "original_img_url", length = 1000)
    private String originalImgUrl;
    
    @Column(name = "original_url", length = 1000)
    private String originalUrl;
    
    @Column(name = "ai_summary", length = 500)
    private String aiSummary;
    
    @OneToMany(mappedBy = "newsArticle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("blockOrder ASC")
    private List<NewsContentBlock> contentBlocks = new ArrayList<>();
    
    @Builder
    public NewsArticle(String newsItemId, String contentsStatus, Integer modifyId, 
                      LocalDateTime modifyDate, LocalDateTime approveDate, String approverName,
                      LocalDateTime embargoDate, String groupingCode, String title,
                      String subTitle1, String subTitle2, String subTitle3,
                      String contentsType, String dataContents, String plainTextContent,
                      String contentHash, String ministerCode, String thumbnailUrl,
                      String originalImgUrl, String originalUrl, String aiSummary) {
        this.newsItemId = newsItemId;
        this.contentsStatus = contentsStatus != null ? contentsStatus : "U";
        this.modifyId = modifyId != null ? modifyId : 1;
        this.modifyDate = modifyDate;
        this.approveDate = approveDate;
        this.approverName = approverName;
        this.embargoDate = embargoDate;
        this.groupingCode = groupingCode;
        this.title = title;
        this.subTitle1 = subTitle1;
        this.subTitle2 = subTitle2;
        this.subTitle3 = subTitle3;
        this.contentsType = contentsType;
        this.dataContents = dataContents;
        this.plainTextContent = plainTextContent;
        this.contentHash = contentHash;
        this.ministerCode = ministerCode;
        this.thumbnailUrl = thumbnailUrl;
        this.originalImgUrl = originalImgUrl;
        this.originalUrl = originalUrl;
        this.aiSummary = aiSummary;
    }

    private NewsArticle(Long id) {
        this.id = id;
    }

    public static NewsArticle createNewsArticle(Long id){
        return new NewsArticle(id);
    }
}

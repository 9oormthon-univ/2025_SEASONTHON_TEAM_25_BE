package com.freedom.news.application.dto;

import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.news.domain.entity.NewsContentBlock;
import com.freedom.common.util.HashUtil;
import com.freedom.news.domain.model.ProcessedBlock;
import com.freedom.news.domain.model.ProcessedNews;
import com.freedom.news.infra.client.response.NewsItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class NewsArticleDto {
    
    private String newsItemId;
    private String contentsStatus;
    private Integer modifyId;
    private LocalDateTime modifyDate;
    private LocalDateTime approveDate;
    private String approverName;
    private LocalDateTime embargoDate;
    private String groupingCode;
    private String title;
    private String subTitle1;
    private String subTitle2;
    private String subTitle3;
    private String contentsType;
    private String dataContents;
    private String plainTextContent;
    private String ministerCode;
    private String thumbnailUrl;
    private String originalImgUrl;
    private String originalUrl;
    private List<ProcessedBlock> processedBlocks;
    private String aiSummary;
    private Boolean economic;

    public static NewsArticleDto from(NewsItem newsItem, ProcessedNews processedNews) {
        return NewsArticleDto.builder()
                .newsItemId(newsItem.getNewsItemId())
                .contentsStatus(newsItem.getContentsStatus())
                .modifyId(newsItem.getModifyId())
                .modifyDate(parseDateTime(newsItem.getModifyDate()))
                .approveDate(parseDateTime(newsItem.getApproveDate()))
                .approverName(newsItem.getApproverName())
                .embargoDate(parseDateTime(newsItem.getEmbargoDate()))
                .groupingCode(newsItem.getGroupingCode())
                .title(newsItem.getTitle())
                .subTitle1(newsItem.getSubTitle1())
                .subTitle2(newsItem.getSubTitle2())
                .subTitle3(newsItem.getSubTitle3())
                .contentsType(newsItem.getContentsType())
                .dataContents(newsItem.getDataContents())
                .plainTextContent(processedNews.getEntirePlainText())
                .ministerCode(newsItem.getMinisterCode())
                .thumbnailUrl(newsItem.getThumbnailUrl())
                .originalImgUrl(newsItem.getOriginalImgUrl())
                .originalUrl(newsItem.getOriginalUrl())
                .processedBlocks(processedNews.getProcessedBlocks())
                .aiSummary(null)
                .economic(false)
                .build();
    }
    
    private static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    
    public NewsArticleDto withAiSummary(String aiSummary) {
        return NewsArticleDto.builder()
                .newsItemId(this.newsItemId)
                .contentsStatus(this.contentsStatus)
                .modifyId(this.modifyId)
                .modifyDate(this.modifyDate)
                .approveDate(this.approveDate)
                .approverName(this.approverName)
                .embargoDate(this.embargoDate)
                .groupingCode(this.groupingCode)
                .title(this.title)
                .subTitle1(this.subTitle1)
                .subTitle2(this.subTitle2)
                .subTitle3(this.subTitle3)
                .contentsType(this.contentsType)
                .dataContents(this.dataContents)
                .plainTextContent(this.plainTextContent)
                .ministerCode(this.ministerCode)
                .thumbnailUrl(this.thumbnailUrl)
                .originalImgUrl(this.originalImgUrl)
                .originalUrl(this.originalUrl)
                .processedBlocks(this.processedBlocks)
                .aiSummary(aiSummary)
                .economic(this.economic)
                .build();
    }

    public NewsArticleDto withClassifiedSummary(boolean economic, String aiSummary) {
        return NewsArticleDto.builder()
                .newsItemId(this.newsItemId)
                .contentsStatus(this.contentsStatus)
                .modifyId(this.modifyId)
                .modifyDate(this.modifyDate)
                .approveDate(this.approveDate)
                .approverName(this.approverName)
                .embargoDate(this.embargoDate)
                .groupingCode(this.groupingCode)
                .title(this.title)
                .subTitle1(this.subTitle1)
                .subTitle2(this.subTitle2)
                .subTitle3(this.subTitle3)
                .contentsType(this.contentsType)
                .dataContents(this.dataContents)
                .plainTextContent(this.plainTextContent)
                .ministerCode(this.ministerCode)
                .thumbnailUrl(this.thumbnailUrl)
                .originalImgUrl(this.originalImgUrl)
                .originalUrl(this.originalUrl)
                .processedBlocks(this.processedBlocks)
                .aiSummary(aiSummary)
                .economic(economic)
                .build();
    }
    
    public NewsArticle toEntity() {
        NewsArticle article = NewsArticle.builder()
                .newsItemId(this.newsItemId)
                .contentsStatus(this.contentsStatus)
                .modifyId(this.modifyId)
                .modifyDate(this.modifyDate)
                .approveDate(this.approveDate)
                .approverName(this.approverName)
                .embargoDate(this.embargoDate)
                .groupingCode(this.groupingCode)
                .title(this.title)
                .subTitle1(this.subTitle1)
                .subTitle2(this.subTitle2)
                .subTitle3(this.subTitle3)
                .contentsType(this.contentsType)
                .dataContents(this.dataContents)
                .plainTextContent(this.plainTextContent)
                .contentHash(generateContentHash())
                .ministerCode(this.ministerCode)
                .thumbnailUrl(this.thumbnailUrl)
                .originalImgUrl(this.originalImgUrl)
                .originalUrl(this.originalUrl)
                .aiSummary(this.aiSummary)
                .build();

        if (this.processedBlocks != null && !this.processedBlocks.isEmpty()) {
            int order = 1;
            for (ProcessedBlock block : this.processedBlocks) {
                NewsContentBlock entityBlock = NewsContentBlock.from(block, article, order++);
                article.getContentBlocks().add(entityBlock);
            }
        }

        return article;
    }

    public String generateContentHash() {
        return HashUtil.sha256(this.dataContents != null ? this.dataContents : "");
    }
}

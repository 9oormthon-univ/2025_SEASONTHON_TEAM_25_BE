package com.freedom.news.infra.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsItem {
    
    @JacksonXmlProperty(localName = "NewsItemId")
    private String newsItemId;

    @JacksonXmlProperty(localName = "ContentsStatus")
    private String contentsStatus;

    @JacksonXmlProperty(localName = "ModifyId")
    private Integer modifyId;

    @JacksonXmlProperty(localName = "ModifyDate")
    private String modifyDate;

    @JacksonXmlProperty(localName = "ApproveDate")
    private String approveDate;

    @JacksonXmlProperty(localName = "ApproverName")
    private String approverName;

    @JacksonXmlProperty(localName = "EmbargoDate")
    private String embargoDate;

    @JacksonXmlProperty(localName = "GroupingCode")
    private String groupingCode;

    @JacksonXmlProperty(localName = "Title")
    private String title;

    @JacksonXmlProperty(localName = "SubTitle1")
    private String subTitle1;

    @JacksonXmlProperty(localName = "SubTitle2")
    private String subTitle2;

    @JacksonXmlProperty(localName = "SubTitle3")
    private String subTitle3;

    @JacksonXmlProperty(localName = "ContentsType")
    private String contentsType;

    @JacksonXmlProperty(localName = "DataContents")
    private String dataContents;

    @JacksonXmlProperty(localName = "MinisterCode")
    private String ministerCode;

    @JacksonXmlProperty(localName = "ThumbnailUrl")
    private String thumbnailUrl;

    @JacksonXmlProperty(localName = "OriginalimgUrl")
    private String originalImgUrl;

    @JacksonXmlProperty(localName = "OriginalUrl")
    private String originalUrl;
}

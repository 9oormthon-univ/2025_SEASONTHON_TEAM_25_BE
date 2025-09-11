package com.freedom.news.domain.service;

import com.freedom.news.domain.model.ProcessedBlock;
import com.freedom.news.domain.model.ProcessedNews;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsContentProcessingService {

    public ProcessedNews processHtmlContent(String htmlContent) {
        try {
            List<ProcessedBlock> blocks = isHtmlContent(htmlContent)
                    ? parseHtmlContent(htmlContent)
                    : parseTextContent(htmlContent);

            String entirePlainText = extractPlainText(htmlContent);
            return ProcessedNews.of(blocks, entirePlainText);

        } catch (Exception e) {
            return createFallbackContent(htmlContent);
        }
    }

    private boolean isHtmlContent(String content) {
        return content.contains("<") && content.contains(">");
    }

    private List<ProcessedBlock> parseHtmlContent(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        List<ProcessedBlock> blocks = new ArrayList<>();

        parseElement(document.body(), blocks);
        return blocks;
    }

    private List<ProcessedBlock> parseTextContent(String textContent) {
        List<ProcessedBlock> blocks = new ArrayList<>();
        String[] paragraphs = textContent.split("\n\n");

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!trimmed.isEmpty()) {
                blocks.add(ProcessedBlock.text(trimmed, trimmed));
                blocks.add(ProcessedBlock.paragraphBreak());
            }
        }

        removeLastParagraphBreak(blocks);
        return blocks;
    }

    private void parseElement(Element element, List<ProcessedBlock> blocks) {
        if (element == null) return;

        for (Node node : element.childNodes()) {
            if (node instanceof Element childElement) {
                handleElement(childElement, blocks);
            } else if (node instanceof TextNode textNode) {
                handleTextNode(textNode, blocks);
            }
        }
    }

    private void handleElement(Element element, List<ProcessedBlock> blocks) {
        String tagName = element.tagName().toLowerCase();

        switch (tagName) {
            case "p" -> addTextWithBreak(element, blocks);
            case "h1", "h2", "h3", "h4", "h5", "h6" -> addHeading(element, tagName, blocks);
            case "img" -> addImage(element, blocks);
            case "figure" -> handleFigure(element, blocks);
            case "br" -> blocks.add(ProcessedBlock.paragraphBreak());
            case "div", "span" -> parseElement(element, blocks);
            default -> addTextIfNotEmpty(element, blocks);
        }
    }

    private void handleTextNode(TextNode textNode, List<ProcessedBlock> blocks) {
        String original = textNode.text();
        String plain = original.trim();
        if (!plain.isEmpty()) {
            blocks.add(ProcessedBlock.text(original, plain));
        }
    }

    private void addTextWithBreak(Element element, List<ProcessedBlock> blocks) {
        String originalHtml = element.outerHtml();
        String plainText = element.text();
        
        if (!plainText.trim().isEmpty()) {
            blocks.add(ProcessedBlock.text(originalHtml, plainText));
            blocks.add(ProcessedBlock.paragraphBreak());
        }
    }

    private void addHeading(Element element, String tagName, List<ProcessedBlock> blocks) {
        String originalHtml = element.outerHtml();
        String plainText = element.text();
        
        if (!plainText.trim().isEmpty()) {
            int level = Integer.parseInt(tagName.substring(1));
            blocks.add(ProcessedBlock.heading(originalHtml, plainText, level));
        }
    }

    private void addImage(Element imgElement, List<ProcessedBlock> blocks) {
        String src = imgElement.attr("src");
        if (!src.isEmpty()) {
            String alt = imgElement.attr("alt");
            blocks.add(ProcessedBlock.image(src, alt));
        }
    }

    private void handleFigure(Element figureElement, List<ProcessedBlock> blocks) {
        // figure 내부의 img 태그 먼저 확인
        Element imgElement = figureElement.selectFirst("img");
        if (imgElement != null) {
            // 이미지가 있을 때만 이미지와 캡션 모두 처리
            addImage(imgElement, blocks);
            
            // figcaption 내용을 텍스트 블록으로 처리
            Element figcaptionElement = figureElement.selectFirst("figcaption");
            if (figcaptionElement != null) {
                String originalHtml = figcaptionElement.outerHtml();
                String plainText = figcaptionElement.text();
                
                if (!plainText.trim().isEmpty()) {
                    blocks.add(ProcessedBlock.text(originalHtml, plainText));
                }
            }
        }
        // 이미지가 없으면 figure 전체를 무시
    }

    private void addTextIfNotEmpty(Element element, List<ProcessedBlock> blocks) {
        String originalHtml = element.outerHtml();
        String plainText = element.text();
        
        if (!plainText.trim().isEmpty()) {
            blocks.add(ProcessedBlock.text(originalHtml, plainText));
        }
    }

    private void removeLastParagraphBreak(List<ProcessedBlock> blocks) {
        if (!blocks.isEmpty() && "paragraph_break".equals(blocks.get(blocks.size() - 1).getType())) {
            blocks.removeLast();
        }
    }

    private String extractPlainText(String htmlContent) {
        if (!isHtmlContent(htmlContent)) {
            return htmlContent.trim();
        }

        try {
            Document document = Jsoup.parse(htmlContent);
            return document.text();
        } catch (Exception e) {
            return htmlContent.replaceAll("<[^>]+>", "").trim();
        }
    }

    private ProcessedNews createFallbackContent(String originalContent) {
        String plainText = extractPlainText(originalContent);
        List<ProcessedBlock> blocks = List.of(ProcessedBlock.text(originalContent, plainText));
        return ProcessedNews.of(blocks, plainText);
    }
}

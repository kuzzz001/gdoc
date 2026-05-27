package com.gdoc.document.service;

import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.model.dto.DocumentCreateRequest;
import com.gdoc.model.dto.DocumentVO;
import com.gdoc.model.entity.GdocDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportService {

    private final DocumentMapper documentMapper;
    private final DocumentService documentService;

    public ImportService(DocumentMapper documentMapper, DocumentService documentService) {
        this.documentMapper = documentMapper;
        this.documentService = documentService;
    }

    @Transactional
    public DocumentVO importMarkdown(Long userId, String title, String markdown) {
        DocumentCreateRequest request = new DocumentCreateRequest();
        request.setTitle(title != null ? title : "导入的文档");
        DocumentVO doc = documentService.create(request, userId);

        // Convert markdown to HTML and update content
        GdocDocument entity = documentMapper.selectById(doc.getId());
        // Simple markdown to HTML conversion handled by ExportService
        entity.setContent(convertMarkdownToHtml(markdown));
        documentMapper.updateById(entity);

        doc.setContent(entity.getContent());
        return doc;
    }

    private String convertMarkdownToHtml(String md) {
        if (md == null) return "<p></p>";
        String html = md;
        html = html.replaceAll("(?m)^### (.+)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^## (.+)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^# (.+)$", "<h1>$1</h1>");
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("\\*(.+?)\\*", "<em>$1</em>");
        html = html.replaceAll("~~(.+?)~~", "<s>$1</s>");
        html = html.replaceAll("`(.+?)`", "<code>$1</code>");
        html = html.replaceAll("(?m)^> (.+)$", "<blockquote>$1</blockquote>");
        html = html.replaceAll("(?m)^- (.+)$", "<li>$1</li>");
        html = html.replaceAll("(?m)^---$", "<hr>");
        html = html.replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>");
        html = html.replaceAll("!\\[\\]\\((.+?)\\)", "<img src=\"$1\" />");
        html = html.replaceAll("(?m)^$", "</p><p>");
        return "<p>" + html + "</p>";
    }
}
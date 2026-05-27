package com.gdoc.document.service;

import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.model.entity.GdocDocument;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ExportService {

    private final DocumentMapper documentMapper;

    public ExportService(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    public byte[] exportPdf(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        // PDF export: convert HTML to PDF
        // In production, use Flying Saucer / iText
        String html = wrapHtml(doc.getTitle(), doc.getContent());
        return html.getBytes();
    }

    public byte[] exportWord(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        // Word export: convert HTML to .docx
        // In production, use Apache POI / POI-TL
        String html = wrapHtml(doc.getTitle(), doc.getContent());
        return html.getBytes();
    }

    public byte[] exportMarkdown(Long docId, Long userId) {
        GdocDocument doc = documentMapper.selectById(docId);
        if (doc == null || doc.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DOC_NOT_FOUND);
        }
        String md = htmlToMarkdown(doc.getContent());
        return md.getBytes();
    }

    public String importMarkdown(Long userId, String markdown) {
        // Convert Markdown to HTML for TipTap editor
        return markdownToHtml(markdown);
    }

    private String wrapHtml(String title, String content) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>"
                + title + "</title><style>body{font-family:sans-serif;max-width:800px;margin:0 auto;padding:20px;}"
                + "h1{font-size:2em;}h2{font-size:1.5em;}h3{font-size:1.17em;}"
                + "pre{background:#f5f5f5;padding:12px;border-radius:4px;overflow-x:auto;}"
                + "code{background:#f0f0f0;padding:2px 4px;border-radius:2px;}"
                + "blockquote{border-left:3px solid #ddd;padding-left:12px;color:#666;}"
                + "table{border-collapse:collapse;width:100%;}th,td{border:1px solid #ddd;padding:8px;}"
                + "</style></head><body>" + content + "</body></html>";
    }

    private String htmlToMarkdown(String html) {
        if (html == null) return "";
        String md = html;
        md = md.replaceAll("<h1[^>]*>(.*?)</h1>", "# $1\n");
        md = md.replaceAll("<h2[^>]*>(.*?)</h2>", "## $1\n");
        md = md.replaceAll("<h3[^>]*>(.*?)</h3>", "### $1\n");
        md = md.replaceAll("<strong>(.*?)</strong>", "**$1**");
        md = md.replaceAll("<b>(.*?)</b>", "**$1**");
        md = md.replaceAll("<em>(.*?)</em>", "*$1*");
        md = md.replaceAll("<i>(.*?)</i>", "*$1*");
        md = md.replaceAll("<u>(.*?)</u>", "<u>$1</u>");
        md = md.replaceAll("<s>(.*?)</s>", "~~$1~~");
        md = md.replaceAll("<code>(.*?)</code>", "`$1`");
        md = md.replaceAll("<pre[^>]*>(.*?)</pre>", "```\n$1\n```");
        md = md.replaceAll("<blockquote[^>]*>(.*?)</blockquote>", "> $1\n");
        md = md.replaceAll("<li[^>]*>(.*?)</li>", "- $1\n");
        md = md.replaceAll("<ul[^>]*>", "");
        md = md.replaceAll("</ul>", "\n");
        md = md.replaceAll("<ol[^>]*>", "");
        md = md.replaceAll("</ol>", "\n");
        md = md.replaceAll("<p[^>]*>(.*?)</p>", "$1\n\n");
        md = md.replaceAll("<br\\s*/?>", "\n");
        md = md.replaceAll("<hr\\s*/?>", "---\n");
        md = md.replaceAll("<a[^>]*href=\"([^\"]*)\"[^>]*>(.*?)</a>", "[$2]($1)");
        md = md.replaceAll("<img[^>]*src=\"([^\"]*)\"[^>]*>", "![]($1)");
        md = md.replaceAll("<[^>]+>", ""); // Remove remaining tags
        md = md.replaceAll("&nbsp;", " ");
        md = md.replaceAll("&amp;", "&");
        md = md.replaceAll("&lt;", "<");
        md = md.replaceAll("&gt;", ">");
        md = md.replaceAll("\n{3,}", "\n\n");
        return md.trim();
    }

    private String markdownToHtml(String md) {
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
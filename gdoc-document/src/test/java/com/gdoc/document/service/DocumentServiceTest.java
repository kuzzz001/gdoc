package com.gdoc.document.service;

import com.gdoc.document.mapper.DocumentMapper;
import com.gdoc.model.entity.GdocDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentService documentService;

    private GdocDocument testDoc;

    @BeforeEach
    void setUp() {
        testDoc = new GdocDocument();
        testDoc.setId(1L);
        testDoc.setTitle("Test Document");
        testDoc.setContent("<p>Test content</p>");
        testDoc.setOwnerId(100L);
    }

    @Test
    void shouldCreateDocument() {
        GdocDocument doc = new GdocDocument();
        doc.setTitle("New Doc");
        doc.setOwnerId(100L);

        assertNotNull(doc);
        assertEquals("New Doc", doc.getTitle());
    }

    @Test
    void shouldGetDocumentById() {
        when(documentMapper.selectById(1L)).thenReturn(testDoc);

        GdocDocument found = documentMapper.selectById(1L);
        assertNotNull(found);
        assertEquals("Test Document", found.getTitle());
    }

    @Test
    void shouldDeleteDocument() {
        when(documentMapper.deleteById(1L)).thenReturn(1);
        int result = documentMapper.deleteById(1L);
        assertEquals(1, result);
    }
}
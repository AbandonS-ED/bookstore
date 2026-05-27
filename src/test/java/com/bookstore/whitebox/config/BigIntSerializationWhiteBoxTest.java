package com.bookstore.whitebox.config;

import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.vo.BookVO;
import com.bookstore.vo.CategoryVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-024 白盒测试：BigInt 精度问题验证
 *
 * 验证点：
 *   1. VO 中 Long 类型 ID 字段添加 @JsonSerialize(using = ToStringSerializer.class)
 *   2. Entity 中使用 @JsonFormat(shape = Shape.STRING) 标注雪花算法 ID
 *   3. DTO 使用 String 接收 ID，Service 层 Long.parseLong() 转换
 *
 * 背景：JS 的 Number.MAX_SAFE_INTEGER 约 16 位，雪花算法生成 19 位 Long，
 *       直接序列化会丢失精度。解决方案：String 化。
 */
@SpringBootTest
class BigIntSerializationWhiteBoxTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void bookVO_idShouldSerializeAsString() throws Exception {
        BookVO vo = new BookVO();
        vo.setId(1876543210987654321L);

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"id\":\"1876543210987654321\""));
        assertFalse(json.contains("\"id\":1876543210987654321"));
    }

    @Test
    void categoryVO_idShouldSerializeAsString() throws Exception {
        CategoryVO vo = new CategoryVO();
        vo.setId(1234567890123456789L);
        vo.setParentId(9876543210123456789L);

        String json = objectMapper.writeValueAsString(vo);

        assertTrue(json.contains("\"id\":\"1234567890123456789\""));
        assertTrue(json.contains("\"parentId\":\"9876543210123456789\""));
    }

    @Test
    void bookEntity_idShouldSerializeAsString() throws Exception {
        Book book = new Book();
        book.setId(1112223334445556667L);
        book.setTitle("精度测试书籍");

        String json = objectMapper.writeValueAsString(book);

        assertTrue(json.contains("\"id\":\"1112223334445556667\""));
    }

    @Test
    void deserialization_dtoWithStringId_shouldParseCorrectly() throws Exception {
        String json = "{\"bookId\":\"1234567890123456789\",\"quantity\":2}";
        var dto = objectMapper.readValue(json,
            com.bookstore.dto.CartAddDTO.class);

        assertEquals("1234567890123456789", dto.getBookId());
    }

    @Test
    void longIdBeyondMaxSafeInteger_shouldNotLosePrecision() throws Exception {
        long unsafeId = 9007199254740993L;

        BookVO vo = new BookVO();
        vo.setId(unsafeId);
        vo.setTitle("Test");

        String serialized = objectMapper.writeValueAsString(vo);
        BookVO deserialized = objectMapper.readValue(serialized, BookVO.class);

        assertEquals(unsafeId, deserialized.getId());
    }
}
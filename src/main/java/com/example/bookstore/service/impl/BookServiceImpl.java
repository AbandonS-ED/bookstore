package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.common.PageResult;
import com.example.bookstore.dto.BookQueryDTO;
import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.BookChapter;
import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.Review;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.entity.Favorite;
import com.example.bookstore.mapper.AuthorMapper;
import com.example.bookstore.mapper.BookChapterMapper;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.mapper.FavoriteMapper;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.service.BookService;
import com.example.bookstore.vo.AuthorVO;
import com.example.bookstore.vo.BookChapterVO;
import com.example.bookstore.vo.BookDetailVO;
import com.example.bookstore.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;
    private final FavoriteMapper favoriteMapper;
    private final AuthorMapper authorMapper;
    private final BookChapterMapper bookChapterMapper;

    @Override
    public PageResult<BookVO> pageQuery(BookQueryDTO queryDTO) {
        Page<Book> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        boolean hasPreorder = false;

        if (queryDTO.getTag() != null) {
            String[] tags = queryDTO.getTag().split(",");
            for (String t : tags) {
                switch (t.trim()) {
                    case "preorder":
                        hasPreorder = true;
                        break;
                    default:
                        break;
                }
            }
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Book::getStatus, queryDTO.getStatus());
        } else if (hasPreorder) {
            wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_PREORDER);
        } else {
            wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON);
        }

        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Book::getCategoryId, queryDTO.getCategoryId());
        }

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Book::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Book::getAuthor, queryDTO.getKeyword())
                    .or()
                    .like(Book::getIsbn, queryDTO.getKeyword()));
        }

        applySort(wrapper, queryDTO.getSortBy());

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> voList = convertToVOList(result.getRecords());

        PageResult<BookVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public PageResult<BookVO> findByCategory(Long categoryId, Integer pageNum, Integer pageSize, String sortBy,
                                             BigDecimal minPrice, BigDecimal maxPrice, Integer minRating, String timeRange) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON)
                .eq(Book::getCategoryId, categoryId);

        if (minPrice != null) {
            wrapper.ge(Book::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Book::getPrice, maxPrice);
        }

        if (minRating != null) {
            double threshold = minRating / 2.0;
            wrapper.apply("(SELECT COALESCE(AVG(r.rating), 0) FROM review r WHERE r.book_id = book.id AND r.status = {0}) >= {1}",
                    Constants.REVIEW_SHOW, threshold);
        }

        if (timeRange != null) {
            LocalDate cutoff = switch (timeRange) {
                case "1m" -> LocalDate.now().minusMonths(1);
                case "3m" -> LocalDate.now().minusMonths(3);
                case "6m" -> LocalDate.now().minusMonths(6);
                case "1y" -> LocalDate.now().minusYears(1);
                default -> null;
            };
            if (cutoff != null) {
                wrapper.ge(Book::getPublishDate, cutoff);
            }
        }

        applySort(wrapper, sortBy);

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> voList = convertToVOList(result.getRecords());

        PageResult<BookVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public PageResult<BookVO> search(String keyword, Integer pageNum, Integer pageSize) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON)
                .and(w -> w.like(Book::getTitle, keyword)
                        .or()
                        .like(Book::getAuthor, keyword)
                        .or()
                        .like(Book::getIsbn, keyword));

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> voList = convertToVOList(result.getRecords());

        PageResult<BookVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public BookDetailVO getDetail(Long id) {
        return getDetail(id, null);
    }

    @Override
    public BookDetailVO getDetail(Long id, Long userId) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(1, "书籍不存在");
        }

        BookDetailVO vo = new BookDetailVO();
        vo.setId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setAuthor(book.getAuthor());
        vo.setPublisher(book.getPublisher());
        vo.setPublishDate(book.getPublishDate());
        vo.setPrice(book.getPrice());
        vo.setOrigPrice(book.getOrigPrice());
        vo.setStock(book.getStock());
        vo.setSales(book.getSales());
        vo.setCategoryId(book.getCategoryId());
        vo.setDescription(book.getDescription());
        vo.setCoverUrl(book.getCoverUrl());
        vo.setQuote(book.getQuote());
        vo.setStatus(book.getStatus());

        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getBookId, id).eq(Review::getStatus, Constants.REVIEW_SHOW);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);

        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            vo.setAvgRating(avgRating);
            vo.setReviewCount(reviews.size());
        } else {
            vo.setAvgRating(0.0);
            vo.setReviewCount(0);
        }

        if (userId != null) {
            LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
            favWrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, id);
            vo.setIsFavorited(favoriteMapper.selectCount(favWrapper) > 0);
        } else {
            vo.setIsFavorited(false);
        }

        if (book.getAuthorId() != null) {
            Author author = authorMapper.selectById(book.getAuthorId());
            if (author != null) {
                AuthorVO authorVO = new AuthorVO();
                authorVO.setId(author.getId());
                authorVO.setName(author.getName());
                authorVO.setAvatarUrl(author.getAvatarUrl());
                authorVO.setBio(author.getBio());
                authorVO.setCountry(author.getCountry());
                authorVO.setBirthYear(author.getBirthYear());
                authorVO.setAwards(author.getAwards());
                vo.setAuthorInfo(authorVO);
            }
        }

        LambdaQueryWrapper<BookChapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(BookChapter::getBookId, id).orderByAsc(BookChapter::getChapterNum);
        List<BookChapter> chapters = bookChapterMapper.selectList(chapterWrapper);
        if (!chapters.isEmpty()) {
            List<BookChapterVO> chapterVOs = chapters.stream().map(ch -> {
                BookChapterVO cv = new BookChapterVO();
                cv.setChapterNum(ch.getChapterNum());
                cv.setTitle(ch.getTitle());
                cv.setPage(ch.getPage());
                return cv;
            }).collect(Collectors.toList());
            vo.setChapters(chapterVOs);
        }

        return vo;
    }

    private void applySort(LambdaQueryWrapper<Book> wrapper, String sortBy) {
        if (sortBy == null || sortBy.isEmpty() || "default".equals(sortBy)) return;
        switch (sortBy) {
            case "price_asc":
                wrapper.orderByAsc(Book::getPrice);
                break;
            case "price_desc":
                wrapper.orderByDesc(Book::getPrice);
                break;
            case "sales_desc":
                wrapper.orderByDesc(Book::getSales);
                break;
            case "publish_desc":
                wrapper.orderByDesc(Book::getPublishDate);
                break;
            case "rating_desc":
                wrapper.apply("ORDER BY (SELECT COALESCE(AVG(r.rating), 0) FROM review r WHERE r.book_id = book.id AND r.status = {0}) DESC", Constants.REVIEW_SHOW);
                break;
            default:
                break;
        }
    }

    @Override
    public List<BookVO> getRanking(String type, String period) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON);

        if ("new".equals(type) && period != null) {
            LocalDate cutoff = getPeriodCutoff(period);
            if (cutoff != null) {
                wrapper.ge(Book::getPublishDate, cutoff);
            }
        }

        switch (type) {
            case "new":
                wrapper.orderByDesc(Book::getPublishDate);
                break;
            case "rating":
                break;
            case "collection":
                wrapper.orderByDesc(Book::getFavoritedCount);
                break;
            default:
                wrapper.orderByDesc(Book::getSales);
                break;
        }

        List<Book> books = bookMapper.selectList(wrapper);
        List<BookVO> voList = convertToVOList(books);

        if ("rating".equals(type)) {
            voList.sort((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()));
        }

        return voList;
    }

    @Override
    public List<BookVO> getComingSoon() {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_COMING)
                .isNotNull(Book::getExpectedShelfDate)
                .orderByAsc(Book::getExpectedShelfDate);
        List<Book> books = bookMapper.selectList(wrapper);
        return convertToVOList(books);
    }

    private LocalDate getPeriodCutoff(String period) {
        return switch (period) {
            case "week" -> LocalDate.now().minusWeeks(1);
            case "month" -> LocalDate.now().minusMonths(1);
            case "quarter" -> LocalDate.now().minusMonths(3);
            case "year" -> LocalDate.now().minusYears(1);
            default -> null;
        };
    }

    private List<BookVO> convertToVOList(List<Book> books) {
        if (books.isEmpty()) return Collections.emptyList();

        Set<Long> bookIds = books.stream().map(Book::getId).collect(Collectors.toSet());

        Map<Long, double[]> reviewStats = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .in(Review::getBookId, bookIds)
                        .eq(Review::getStatus, Constants.REVIEW_SHOW)
        ).stream()
                .collect(Collectors.groupingBy(
                        Review::getBookId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    double avg = list.stream().mapToInt(Review::getRating).average().orElse(0.0);
                                    return new double[]{avg, list.size()};
                                }
                        )
                ));

        List<Long> categoryIds = books.stream()
                .map(Book::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNames;
        if (!categoryIds.isEmpty()) {
            categoryNames = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        } else {
            categoryNames = Collections.emptyMap();
        }

        return books.stream().map(book -> {
            BookVO vo = new BookVO();
            vo.setId(book.getId());
            vo.setIsbn(book.getIsbn());
            vo.setTitle(book.getTitle());
            vo.setAuthor(book.getAuthor());
            vo.setPublisher(book.getPublisher());
            vo.setPublishDate(book.getPublishDate());
            vo.setPrice(book.getPrice());
            vo.setOrigPrice(book.getOrigPrice());
            vo.setStock(book.getStock());
            vo.setSales(book.getSales());
            vo.setFavoritedCount(book.getFavoritedCount());
            vo.setCategoryId(book.getCategoryId());
            vo.setCategoryName(categoryNames.get(book.getCategoryId()));
            vo.setCoverUrl(book.getCoverUrl());
            vo.setQuote(book.getQuote());
            vo.setStatus(book.getStatus());
            vo.setExpectedShelfDate(book.getExpectedShelfDate());

            double[] stats = reviewStats.get(book.getId());
            if (stats != null) {
                vo.setAvgRating(stats[0]);
                vo.setReviewCount((int) stats[1]);
            } else {
                vo.setAvgRating(0.0);
                vo.setReviewCount(0);
            }

            return vo;
        }).collect(Collectors.toList());
    }

}
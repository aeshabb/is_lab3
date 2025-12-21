package org.itmo.lab3.util;

/**
 * Класс для запроса страницы (аналог Spring Data PageRequest)
 */
public class PageRequest {
    
    private final int pageNumber;
    private final int pageSize;
    private final Sort sort;
    
    private PageRequest(int pageNumber, int pageSize, Sort sort) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must not be less than zero");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must not be less than one");
        }
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
    }
    
    public static PageRequest of(int pageNumber, int pageSize) {
        return new PageRequest(pageNumber, pageSize, null);
    }
    
    public static PageRequest of(int pageNumber, int pageSize, Sort sort) {
        return new PageRequest(pageNumber, pageSize, sort);
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public Sort getSort() {
        return sort;
    }
    
    public long getOffset() {
        return (long) pageNumber * (long) pageSize;
    }
    
    public PageRequest next() {
        return new PageRequest(pageNumber + 1, pageSize, sort);
    }
    
    public PageRequest previous() {
        return pageNumber == 0 ? this : new PageRequest(pageNumber - 1, pageSize, sort);
    }
    
    public PageRequest first() {
        return new PageRequest(0, pageSize, sort);
    }
    
    public boolean hasPrevious() {
        return pageNumber > 0;
    }
}

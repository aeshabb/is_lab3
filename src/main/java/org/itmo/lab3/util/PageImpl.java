package org.itmo.lab3.util;

import java.util.Collections;
import java.util.List;

/**
 * Реализация Page (аналог Spring Data PageImpl)
 */
public class PageImpl<T> implements Page<T> {
    
    private final List<T> content;
    private final PageRequest pageable;
    private final long total;
    
    public PageImpl(List<T> content, PageRequest pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }
    
    @Override
    public int getTotalPages() {
        return pageable.getPageSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) pageable.getPageSize());
    }
    
    @Override
    public long getTotalElements() {
        return total;
    }
    
    @Override
    public int getNumber() {
        return pageable.getPageNumber();
    }
    
    @Override
    public int getSize() {
        return pageable.getPageSize();
    }
    
    @Override
    public int getNumberOfElements() {
        return content.size();
    }
    
    @Override
    public List<T> getContent() {
        return Collections.unmodifiableList(content);
    }
    
    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }
    
    @Override
    public Sort getSort() {
        return pageable.getSort();
    }
    
    @Override
    public boolean isFirst() {
        return !hasPrevious();
    }
    
    @Override
    public boolean isLast() {
        return !hasNext();
    }
    
    @Override
    public boolean hasNext() {
        return getNumber() + 1 < getTotalPages();
    }
    
    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }
    
    @Override
    public PageRequest nextPageable() {
        return hasNext() ? pageable.next() : pageable;
    }
    
    @Override
    public PageRequest previousPageable() {
        return hasPrevious() ? pageable.previous() : pageable;
    }
}

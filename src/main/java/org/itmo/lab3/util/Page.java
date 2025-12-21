package org.itmo.lab3.util;

import java.util.List;

/**
 * Интерфейс для страницы с данными (аналог Spring Data Page)
 */
public interface Page<T> {
    
    int getTotalPages();
    
    long getTotalElements();
    
    int getNumber();
    
    int getSize();
    
    int getNumberOfElements();
    
    List<T> getContent();
    
    boolean hasContent();
    
    Sort getSort();
    
    boolean isFirst();
    
    boolean isLast();
    
    boolean hasNext();
    
    boolean hasPrevious();
    
    PageRequest nextPageable();
    
    PageRequest previousPageable();
}

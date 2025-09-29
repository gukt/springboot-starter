package com.example.common;

import com.fairyland.common.api.JsonViews.ApiResultView;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 该类用于简化 Pageable 的序列化结果，去掉了 pageable、sort 等多余的层级，只返回够用的分页信息。
 */
@JsonIgnoreProperties({"pageable", "sort", "number"})
@SuppressWarnings("unused")
public class JsonPage<T> extends PageImpl<T> {

    private JsonPage(final List<T> content, final Pageable page, final long total) {
        super(content, page, total);
    }

    public static <T> JsonPage<T> of(final List<T> content, final Pageable page) {
        return new JsonPage<>(content, page, content.size());
    }

    public static <T> JsonPage<T> of(final Page<T> page) {
        return new JsonPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    @JsonView(ApiResultView.class)
    public int getPage() {
        return super.getPageable().getPageNumber();
    }

    @Override
    @JsonView(ApiResultView.class)
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @Override
    @JsonView(ApiResultView.class)
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @Override
    @JsonView(ApiResultView.class)
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    @JsonView(ApiResultView.class)
    public boolean hasContent() {
        return super.hasContent();
    }

    @Override
    @JsonView(ApiResultView.class)
    @Nonnull
    public List<T> getContent() {
        return super.getContent();
    }
}

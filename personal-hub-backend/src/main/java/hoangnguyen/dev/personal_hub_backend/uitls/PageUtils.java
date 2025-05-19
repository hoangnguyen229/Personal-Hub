package hoangnguyen.dev.personal_hub_backend.uitls;

import hoangnguyen.dev.personal_hub_backend.dto.response.PageResponse;
import org.springframework.data.domain.Page;

/**
 * Utility class for handling pagination-related operations
 */
public class PageUtils {

    /**
     * Builds a PageResponse object from a Spring Data Page
     *
     * @param page Spring Data Page object containing the paginated data
     * @param <T> Type of the content in the page
     * @return PageResponse object with pagination metadata
     */
    public static <T> PageResponse<T> buildPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}

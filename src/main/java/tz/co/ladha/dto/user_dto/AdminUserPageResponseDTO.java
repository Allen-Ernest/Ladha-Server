package tz.co.ladha.dto.user_dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPageResponseDTO {
    private List<AdminUserResponseDTO> content = new ArrayList<>();
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

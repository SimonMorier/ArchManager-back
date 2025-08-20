package com.archmanager_back.api.graph;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponseDTO {
    private boolean success;
    private String message;
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private String projectSlug;
}

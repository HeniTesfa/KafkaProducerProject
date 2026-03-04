package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Site implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String siteId;

    @NotBlank
    private String siteName;

    @NotNull
    private SiteType siteType;

    @NotNull
    private Address address;

    @NotNull
    private SiteStatus status;

    public enum SiteType { WAREHOUSE, STORE, DISTRIBUTION_CENTER }
    public enum SiteStatus { ACTIVE, INACTIVE }
}
package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String line1;

    private String line2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;
}
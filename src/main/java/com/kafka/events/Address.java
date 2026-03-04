package com.kafka.events;

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

    @NotBlank String line1;
    String line2;
    @NotBlank String city;
    @NotBlank String state;
    @NotBlank String postalCode;
    @NotBlank String country;
}
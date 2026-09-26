package com.bank.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RegisterRequest — the JSON body we expect when a new user registers.
 *
 * @Data (Lombok) generates: @Getter + @Setter + @ToString + @EqualsAndHashCode
 *
 * Validation annotations:
 *   @NotBlank  = cannot be null, empty, or just spaces
 *   @Email     = must look like an email address
 *   @Size      = min/max character length
 *
 * These are checked automatically by Spring when the controller
 * has @Valid on the method parameter.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;
}

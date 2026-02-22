package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = Nomenclature.REQD.USERNAME)
    private String username;

    @NotBlank(message = Nomenclature.REQD.PASSWORD)
    private String password;
}
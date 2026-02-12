package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {
    @NotBlank(message = Nomenclature.REQD.USERNAME)
    private String username;

    @NotBlank(message = Nomenclature.REQD.OLD_PASSWORD)
    private String oldPassword;

    @NotBlank(message = Nomenclature.REQD.NEW_PASSWORD)
    private String newPassword;
}

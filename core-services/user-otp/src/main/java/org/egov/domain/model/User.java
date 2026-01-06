package org.egov.domain.model;

import lombok.*;
import org.egov.persistence.contract.Role;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class User {
    private Long id;
    private String email;
    private String mobileNumber;
    private List<Role> roles;
}


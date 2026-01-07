package org.egov.persistence.contract;

import lombok.*;
import org.egov.domain.model.User;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserSearchResponseContent {
    private Long id;
    private String emailId;
    private String mobileNumber;
    private List<Role> roles;

    public User toDomainUser() {
        return new User(id, emailId, mobileNumber, roles);
    }
}
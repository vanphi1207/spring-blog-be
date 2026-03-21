package me.ihqqq.spring_blog.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.validator.DobConstraint;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    String password;
    String firstName;
    String lastName;

    String bio;
    String avatarUrl;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;


}

package com.gymcrm.model;

/*
It makes that User is an abstract class the specifications do not require its instantiation
Therefore, we shouldn't be able to create an User without a role.
Additionally, we could later have methods (e.g., for credentials), that accept a generic User type.
*/

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
/*
From: https://projectlombok.org/features/Data
@Data generates all the boilerplate that is normally associated with simple POJOs (Plain Old Java Objects) and beans:
getters for all fields, setters for all non-final fields, and appropriate toString, equals and hashCode implementations that
involve the fields of the class, and a constructor that initializes all final fields, as well as all non-final fields with no
initializer that have been marked with @NonNull, in order to ensure the field is never null.
@Data is like having implicit @Getter, @Setter, @ToString, @EqualsAndHashCode and @RequiredArgsConstructor annotations on the class
(except that no constructor will be generated if any explicitly written constructors already exist). However, the parameters of these
annotations (such as callSuper, includeFieldNames and exclude) cannot be set with @Data. If you need to set non-default values for any of
 these parameters, just add those annotations explicitly; @Data is smart enough to defer to those annotations.
All generated getters and setters will be public.
 */

@Data // Generates getters, setters, toString, equals/hashCode
@SuperBuilder // Required for Builder pattern inheritance
@NoArgsConstructor
public class User {
    private Long UserId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive = true;
}

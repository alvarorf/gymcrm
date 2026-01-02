package com.gymcrm.model;

/*
It makes that User is an abstract class the specifications do not require its instantiation
Therefore, we shouldn't be able to create a User without a role.
Additionally, we could later have methods (e.g., for credentials), that accept a generic User type.
*/

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
@NoArgsConstructor(access = AccessLevel.PUBLIC) // It is protected by default
@Table(name = "users")
@Entity // It is mandatory so that Hibernate knows that this class corresponds to a (persistent) table in the database
public class User {
    @Id // It means that the field will be the primary key of this entity
    @GeneratedValue(strategy = GenerationType.IDENTITY) // It tells hibernate to let the database generate the value automatically (auto increment for the IDENTITY generation type)
    private Long userId;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique=true)
    private String username;
    @Column(nullable = false)
    private String password;
    @JsonProperty("isActive")
    @Column(nullable = false)
    private boolean isActive = true;
}

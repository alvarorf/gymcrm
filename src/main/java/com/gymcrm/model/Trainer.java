package com.gymcrm.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data // Generates getters, setters, toString, equals/hashCode
@SuperBuilder // Required for Builder pattern inheritance
@NoArgsConstructor
public class Trainer extends User {
    private String specialization;
}

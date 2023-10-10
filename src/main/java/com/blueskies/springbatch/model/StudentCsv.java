package com.blueskies.springbatch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCsv {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
}

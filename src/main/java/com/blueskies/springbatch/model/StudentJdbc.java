package com.blueskies.springbatch.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="student")
public class StudentJdbc {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
}

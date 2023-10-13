package com.blueskies.springbatch.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "student")
public class StudentXml {
    private long id;
    //    @Getter(onMethod_ = {@XmlElement(name = "f_N")}) //This is to bind a property to a node in xml with different names
    private String firstName;
    private String lastName;
    private String email;
}

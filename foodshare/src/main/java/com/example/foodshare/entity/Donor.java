package com.example.foodshare.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
@Entity
public class Donor {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @NotBlank(message="Name is required") public String name; @NotBlank @Email(message="A valid email is required") public String email; public String phone; public String address;
}

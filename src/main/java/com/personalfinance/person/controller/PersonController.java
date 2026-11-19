package com.personalfinance.person.controller;

import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.person.dto.CreatePersonRequest;
import com.personalfinance.person.dto.PersonResponse;
import com.personalfinance.person.dto.UpdatePersonRequest;
import com.personalfinance.person.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public ApiResponse<List<PersonResponse>> getAllPersons() {
        List<PersonResponse> persons = personService.getAllPersons();
        return ApiResponse.success(persons);
    }

    @GetMapping("/{id}")
    public ApiResponse<PersonResponse> getPersonById(@PathVariable UUID id) {
        PersonResponse person = personService.getPersonById(id);
        return ApiResponse.success(person);
    }

    @PostMapping
    public ApiResponse<PersonResponse> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        PersonResponse person = personService.createPerson(request);
        return ApiResponse.success("Thêm đối tượng thành công", person);
    }

    @PutMapping("/{id}")
    public ApiResponse<PersonResponse> updatePerson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonRequest request) {
        PersonResponse person = personService.updatePerson(id, request);
        return ApiResponse.success("Cập nhật đối tượng thành công", person);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
        return ApiResponse.success("Xóa đối tượng thành công", null);
    }
}

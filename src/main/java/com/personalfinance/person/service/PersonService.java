package com.personalfinance.person.service;

import com.personalfinance.common.exception.ResourceNotFoundException;
import com.personalfinance.person.dto.CreatePersonRequest;
import com.personalfinance.person.dto.PersonResponse;
import com.personalfinance.person.dto.UpdatePersonRequest;
import com.personalfinance.person.entity.Person;
import com.personalfinance.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    @Transactional(readOnly = true)
    public List<PersonResponse> getAllPersons() {
        return personRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonResponse getPersonById(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người", id));
        return toResponse(person);
    }

    @Transactional(readOnly = true)
    public Person getEntityById(UUID id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người", id));
    }

    @Transactional
    public PersonResponse createPerson(CreatePersonRequest request) {
        Person person = Person.builder()
                .name(request.getName().trim())
                .phone(request.getPhone())
                .note(request.getNote())
                .build();

        Person saved = personRepository.save(person);
        log.info("Đã tạo thông tin đối tượng: {} (ID: {})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public PersonResponse updatePerson(UUID id, UpdatePersonRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người", id));

        person.setName(request.getName().trim());
        person.setPhone(request.getPhone());
        person.setNote(request.getNote());

        Person updated = personRepository.save(person);
        log.info("Đã cập nhật thông tin đối tượng: {} (ID: {})", updated.getName(), updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deletePerson(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người", id));
        personRepository.delete(person);
        log.info("Đã xóa đối tượng: {} (ID: {})", person.getName(), person.getId());
    }

    public PersonResponse toResponse(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .name(person.getName())
                .phone(person.getPhone())
                .note(person.getNote())
                .createdAt(person.getCreatedAt())
                .updatedAt(person.getUpdatedAt())
                .build();
    }
}

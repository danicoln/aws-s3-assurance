package com.danicoln.awss3assurance.service;

import com.danicoln.awss3assurance.exception.ResourceConflictException;
import com.danicoln.awss3assurance.exception.ResourceNotFoundException;
import com.danicoln.awss3assurance.model.UserEntity;
import com.danicoln.awss3assurance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        List<User> users = userRepository.findAll()
                .stream()
                .map(this::toUser)
                .sorted(Comparator.comparing(User::id))
                .toList();

        log.info("User list completed: count={}", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public User getById(String id) {
        ensureHasText(id, "User id is required");

        return toUser(findExisting(id));
    }

    @Transactional
    public User create(User user) {
        validate(user);
        ensureMissing(user.id());

        UserEntity savedUser = userRepository.save(new UserEntity(user.id(), user.nome()));

        log.info("User created: id={}", savedUser.getId());
        return toUser(savedUser);
    }

    @Transactional
    public User update(String id, String nome) {
        ensureHasText(id, "User id is required");
        ensureHasText(nome, "User nome is required");

        UserEntity existingUser = findExisting(id);
        existingUser.setNome(nome);
        UserEntity savedUser = userRepository.save(existingUser);

        log.info("User updated: id={}", id);
        return toUser(savedUser);
    }

    @Transactional
    public void delete(String id) {
        ensureHasText(id, "User id is required");
        findExisting(id);
        userRepository.deleteById(id);

        log.info("User deleted: id={}", id);
    }

    private void ensureMissing(String id) {
        if (userRepository.existsById(id)) {
            throw new ResourceConflictException("User with id '" + id + "' already exists");
        }
    }

    private void validate(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        ensureHasText(user.id(), "User id is required");
        ensureHasText(user.nome(), "User nome is required");
    }

    private void ensureHasText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private UserEntity findExisting(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' was not found"));
    }

    private User toUser(UserEntity entity) {
        return new User(entity.getId(), entity.getNome());
    }

    public record User(String id, String nome) {
    }
}

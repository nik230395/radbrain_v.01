package org.nikolic.programm.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.Role;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.ForbiddenException;
import org.nikolic.programm.exceptions.QuizAlreadyExistsException;
import org.nikolic.programm.exceptions.QuizNotFoundException;
import org.nikolic.programm.exceptions.UnauthorizedException;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminQuizController
 * Tests cover authentication, authorization, error handling, and business logic
 */
@ExtendWith(MockitoExtension.class)
class AdminQuizControllerTest {

    @Mock
    private QuizService quizService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminQuizController controller;

    private User adminUser;
    private User regularUser;
    private Quiz testQuiz;
    private CreateQuizRequest createQuizRequest;
    private Authentication adminAuth;
    private Authentication userAuth;

    @BeforeEach
    void setUp() {
        // Setup admin user
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setFullname("Admin User");
        Role adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(1L);
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        // Setup regular user
        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setEmail("user@test.com");
        regularUser.setFullname("Regular User");
        regularUser.setRoles(new HashSet<>());

        // Setup test quiz
        testQuiz = new Quiz();
        testQuiz.setId(1L);
        testQuiz.setTitle("Test Quiz");
        testQuiz.setDescription("Test Description");
        testQuiz.setCreatedBy(adminUser);
        testQuiz.setCreatedAt(LocalDateTime.now());
        testQuiz.setIsPublished(false);

        // Setup create quiz request
        createQuizRequest = new CreateQuizRequest();
        createQuizRequest.setTitle("New Quiz");
        createQuizRequest.setDescription("New Description");

        // Setup authentication
        adminAuth = new TestingAuthenticationToken(adminUser.getEmail(), null);
        userAuth = new TestingAuthenticationToken(regularUser.getEmail(), null);
    }

    @Test
    void testListAll_AsAdmin_ReturnsQuizzes() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.getAllQuizzes()).thenReturn(Collections.singletonList(testQuiz));

        // Act
        ResponseEntity<?> response = controller.listAll(adminAuth);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(quizService, times(1)).getAllQuizzes();
    }

    @Test
    void testListAll_AsNonAdmin_ReturnsForbidden() {
        // Arrange
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));

        // Act
        ResponseEntity<?> response = controller.listAll(userAuth);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(quizService, never()).getAllQuizzes();
    }

    @Test
    void testListAll_Unauthenticated_ReturnsUnauthorized() {
        // Act
        ResponseEntity<?> response = controller.listAll(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(quizService, never()).getAllQuizzes();
    }

    @Test
    void testCreate_ValidRequest_ReturnsCreated() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.createFromRequest(any(CreateQuizRequest.class), any(User.class)))
                .thenReturn(testQuiz);

        // Act
        ResponseEntity<?> response = controller.create(adminAuth, createQuizRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(quizService, times(1)).createFromRequest(any(CreateQuizRequest.class), any(User.class));
    }

    @Test
    void testCreate_DuplicateQuiz_ReturnsConflict() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.createFromRequest(any(CreateQuizRequest.class), any(User.class)))
                .thenThrow(new QuizAlreadyExistsException("A quiz with title 'New Quiz' already exists"));

        // Act
        ResponseEntity<?> response = controller.create(adminAuth, createQuizRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("quiz_already_exists"));
    }

    @Test
    void testCreate_EmptyTitle_ReturnsBadRequest() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.createFromRequest(any(CreateQuizRequest.class), any(User.class)))
                .thenThrow(new IllegalArgumentException("Quiz title cannot be empty"));

        createQuizRequest.setTitle("");

        // Act
        ResponseEntity<?> response = controller.create(adminAuth, createQuizRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("invalid_input"));
    }

    @Test
    void testUpdate_ValidRequest_ReturnsOk() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.updateFromRequest(eq(1L), any(CreateQuizRequest.class)))
                .thenReturn(testQuiz);

        // Act
        ResponseEntity<?> response = controller.update(adminAuth, 1L, createQuizRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(quizService, times(1)).updateFromRequest(eq(1L), any(CreateQuizRequest.class));
    }

    @Test
    void testUpdate_QuizNotFound_ReturnsNotFound() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.updateFromRequest(eq(999L), any(CreateQuizRequest.class)))
                .thenThrow(new QuizNotFoundException("Quiz not found with ID: 999"));

        // Act
        ResponseEntity<?> response = controller.update(adminAuth, 999L, createQuizRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("quiz_not_found"));
    }

    @Test
    void testUpdate_AsNonAdmin_ReturnsForbidden() {
        // Arrange
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));

        // Act
        ResponseEntity<?> response = controller.update(userAuth, 1L, createQuizRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(quizService, never()).updateFromRequest(anyLong(), any(CreateQuizRequest.class));
    }

    @Test
    void testPublish_ValidRequest_ReturnsOk() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        testQuiz.setIsPublished(true);
        when(quizService.setPublished(1L, true)).thenReturn(testQuiz);

        // Act
        ResponseEntity<?> response = controller.publish(adminAuth, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(quizService, times(1)).setPublished(1L, true);
    }

    @Test
    void testUnpublish_ValidRequest_ReturnsOk() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(quizService.setPublished(1L, false)).thenReturn(testQuiz);

        // Act
        ResponseEntity<?> response = controller.unpublish(adminAuth, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(quizService, times(1)).setPublished(1L, false);
    }

    @Test
    void testDelete_ValidRequest_ReturnsOk() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        doNothing().when(quizService).deleteQuizById(1L);

        // Act
        ResponseEntity<?> response = controller.delete(adminAuth, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Quiz deleted successfully"));
        verify(quizService, times(1)).deleteQuizById(1L);
    }

    @Test
    void testDelete_QuizNotFound_ReturnsNotFound() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        doThrow(new QuizNotFoundException("Quiz not found with ID: 999"))
                .when(quizService).deleteQuizById(999L);

        // Act
        ResponseEntity<?> response = controller.delete(adminAuth, 999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("quiz_not_found"));
    }

    @Test
    void testDelete_AsNonAdmin_ReturnsForbidden() {
        // Arrange
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));

        // Act
        ResponseEntity<?> response = controller.delete(userAuth, 1L);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(quizService, never()).deleteQuizById(anyLong());
    }

    @Test
    void testAdminUserWithLegacyRole_IsRecognizedAsAdmin() {
        // Arrange - Admin user with legacy role field instead of roles collection
        User legacyAdmin = new User();
        legacyAdmin.setId(3L);
        legacyAdmin.setEmail("admin@test.com");
        legacyAdmin.setFullname("Legacy Admin");
        legacyAdmin.setRole("ADMIN"); // Legacy role field
        legacyAdmin.setRoles(new HashSet<>()); // Empty roles collection

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(legacyAdmin));
        when(quizService.getAllQuizzes()).thenReturn(Collections.singletonList(testQuiz));

        // Act
        ResponseEntity<?> response = controller.listAll(adminAuth);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(quizService, times(1)).getAllQuizzes();
    }

    @Test
    void testUserWithNullRoles_ReturnsForbidden() {
        // Arrange - User with null roles
        User nullRolesUser = new User();
        nullRolesUser.setId(4L);
        nullRolesUser.setEmail("nullroles@test.com");
        nullRolesUser.setFullname("Null Roles User");
        nullRolesUser.setRoles(null);
        nullRolesUser.setRole(null);

        Authentication nullRolesAuth = new TestingAuthenticationToken(nullRolesUser.getEmail(), null);
        when(userRepository.findByEmail("nullroles@test.com")).thenReturn(Optional.of(nullRolesUser));

        // Act
        ResponseEntity<?> response = controller.listAll(nullRolesAuth);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(quizService, never()).getAllQuizzes();
    }
}

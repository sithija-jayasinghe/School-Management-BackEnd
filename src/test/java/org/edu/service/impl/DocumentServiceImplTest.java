package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.edu.dto.DocumentDTO;
import org.edu.dto.request.DocumentCreateRequest;
import org.edu.dto.request.DocumentUpdateRequest;
import org.edu.entity.Document;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.mapper.DocumentMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.DocumentRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.DocumentStorageService;
import org.edu.util.DocumentType;
import org.edu.util.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        DocumentMapper documentMapper = Mappers.getMapper(DocumentMapper.class);
        documentService = new DocumentServiceImpl(
                documentRepository,
                studentRepository,
                userRepository,
                staffRepository,
                classRepository,
                timetableRepository,
                documentStorageService,
                documentMapper
        );
    }

    @Test
    void shouldUploadDocumentForAdmin() {
        User admin = user(100L, Role.ADMIN, "Admin User");
        Student student = student(20L, 30L);
        DocumentCreateRequest request = new DocumentCreateRequest(20L, DocumentType.REPORT_CARD, "Term 1 Report", "Student report", true);
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "pdf-content".getBytes());

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(documentStorageService.store(file))
                .thenReturn(new StoredDocumentFile("report.pdf", "stored-report.pdf", "application/pdf", 11L));
        when(documentRepository.save(org.mockito.Mockito.any(Document.class)))
                .thenAnswer(invocation -> {
                    Document document = invocation.getArgument(0);
                    document.setId(1L);
                    return document;
                });

        DocumentDTO saved = documentService.uploadDocument(100L, request, file);

        assertEquals(1L, saved.getId());
        assertEquals("Term 1 Report", saved.getTitle());
        assertEquals("report.pdf", saved.getOriginalFileName());
    }

    @Test
    void shouldRejectTeacherUploadForInaccessibleStudent() {
        User teacherUser = user(101L, Role.TEACHER, "Teacher User");
        Staff staff = new Staff();
        staff.setId(50L);
        Student student = student(20L, 30L);
        DocumentCreateRequest request = new DocumentCreateRequest(20L, DocumentType.OTHER, "Student Record", null, false);
        MockMultipartFile file = new MockMultipartFile("file", "record.pdf", "application/pdf", "data".getBytes());

        when(userRepository.findById(101L)).thenReturn(Optional.of(teacherUser));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(staffRepository.findByUser_IdAndActiveTrue(101L)).thenReturn(Optional.of(staff));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 50L)).thenReturn(false);
        when(timetableRepository.existsByStaffIdAndStudentClassId(50L, 30L)).thenReturn(false);

        assertThrows(org.edu.exception.ResourceNotFoundException.class,
                () -> documentService.uploadDocument(101L, request, file));
        verify(documentStorageService, never()).store(file);
    }

    @Test
    void shouldReturnDocumentsByStudentForTeacherAccess() {
        User teacherUser = user(101L, Role.TEACHER, "Teacher User");
        Staff staff = new Staff();
        staff.setId(50L);
        Student student = student(20L, 30L);
        Document document = document(1L, student, teacherUser);

        when(userRepository.findById(101L)).thenReturn(Optional.of(teacherUser));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(staffRepository.findByUser_IdAndActiveTrue(101L)).thenReturn(Optional.of(staff));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 50L)).thenReturn(true);
        when(documentRepository.findByStudentIdAndActiveTrueOrderByCreatedAtDesc(20L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(document)));

        var page = documentService.getDocumentsByStudent(101L, 20L, Pageable.unpaged());

        assertEquals(1, page.getTotalElements());
        assertEquals("Document Title", page.getContent().get(0).getTitle());
    }

    @Test
    void shouldDownloadDocumentForAdmin() {
        User admin = user(100L, Role.ADMIN, "Admin User");
        Student student = student(20L, 30L);
        Document document = document(1L, student, admin);

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(documentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(document));
        when(documentStorageService.loadAsResource("stored-report.pdf"))
                .thenReturn(new ByteArrayResource("file".getBytes()));

        var fileResponse = documentService.downloadDocument(100L, 1L);

        assertEquals("report.pdf", fileResponse.getFileName());
        assertEquals("application/pdf", fileResponse.getContentType());
    }

    @Test
    void shouldDeleteStoredFileWhenDocumentSaveFails() {
        User admin = user(100L, Role.ADMIN, "Admin User");
        Student student = student(20L, 30L);
        DocumentCreateRequest request = new DocumentCreateRequest(20L, DocumentType.REPORT_CARD, "Term 1 Report", "Student report", true);
        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "pdf-content".getBytes());

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(documentStorageService.store(file))
                .thenReturn(new StoredDocumentFile("report.pdf", "stored-report.pdf", "application/pdf", 11L));
        when(documentRepository.save(org.mockito.Mockito.any(Document.class)))
                .thenThrow(new IllegalStateException("DB write failed"));

        assertThrows(IllegalStateException.class, () -> documentService.uploadDocument(100L, request, file));
        verify(documentStorageService).delete("stored-report.pdf");
    }

    private User user(Long id, Role role, String name) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setName(name);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        return user;
    }

    private Student student(Long studentId, Long classId) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName("Grade 10A");

        Student student = new Student();
        student.setId(studentId);
        student.setName("Student User");
        student.setCurrentClass(studentClass);
        student.setActive(true);
        return student;
    }

    private Document document(Long id, Student student, User uploader) {
        Document document = new Document();
        document.setId(id);
        document.setStudent(student);
        document.setUploadedBy(uploader);
        document.setDocumentType(DocumentType.REPORT_CARD);
        document.setTitle("Document Title");
        document.setDescription("Document description");
        document.setOriginalFileName("report.pdf");
        document.setStoredFileName("stored-report.pdf");
        document.setContentType("application/pdf");
        document.setFileSize(123L);
        document.setActive(true);
        document.setVisibleToParent(true);
        return document;
    }
}

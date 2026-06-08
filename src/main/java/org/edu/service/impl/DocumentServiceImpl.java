package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.DocumentDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.DocumentCreateRequest;
import org.edu.dto.request.DocumentUpdateRequest;
import org.edu.entity.Document;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.DocumentMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.DocumentRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.DocumentService;
import org.edu.service.DocumentStorageService;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ClassRepository classRepository;
    private final TimetableRepository timetableRepository;
    private final DocumentStorageService documentStorageService;
    private final DocumentMapper documentMapper;

    @Override
    public DocumentDTO uploadDocument(Long authenticatedUserId, DocumentCreateRequest request, MultipartFile file) {
        User uploader = getUser(authenticatedUserId);
        Student student = getActiveStudent(request.getStudentId());
        validateDocumentAccess(uploader, student);

        StoredDocumentFile storedFile = documentStorageService.store(file);

        try {
            Document document = new Document();
            document.setStudent(student);
            document.setUploadedBy(uploader);
            document.setDocumentType(request.getDocumentType());
            document.setTitle(request.getTitle());
            document.setDescription(request.getDescription());
            document.setOriginalFileName(storedFile.getOriginalFileName());
            document.setStoredFileName(storedFile.getStoredFileName());
            document.setContentType(storedFile.getContentType());
            document.setFileSize(storedFile.getFileSize());
            document.setVisibleToParent(request.isVisibleToParent());
            document.setActive(true);

            return documentMapper.toDTO(documentRepository.save(document));
        } catch (RuntimeException ex) {
            documentStorageService.delete(storedFile.getStoredFileName());
            throw ex;
        }
    }

    @Override
    public DocumentDTO updateDocument(Long authenticatedUserId, Long documentId, DocumentUpdateRequest request) {
        User user = getUser(authenticatedUserId);
        Document document = getActiveDocument(documentId);
        validateDocumentAccess(user, document.getStudent());

        if (request.getDocumentType() != null) {
            document.setDocumentType(request.getDocumentType());
        }
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getVisibleToParent() != null) {
            document.setVisibleToParent(request.getVisibleToParent());
        }

        return documentMapper.toDTO(documentRepository.save(document));
    }

    @Override
    public void deleteDocument(Long authenticatedUserId, Long documentId) {
        User user = getUser(authenticatedUserId);
        Document document = getActiveDocument(documentId);
        validateDocumentAccess(user, document.getStudent());

        document.setActive(false);
        documentStorageService.delete(document.getStoredFileName());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDTO getDocument(Long authenticatedUserId, Long documentId) {
        User user = getUser(authenticatedUserId);
        Document document = getActiveDocument(documentId);
        validateDocumentAccess(user, document.getStudent());
        return documentMapper.toDTO(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getDocumentsByStudent(Long authenticatedUserId, Long studentId, Pageable pageable) {
        User user = getUser(authenticatedUserId);
        Student student = getActiveStudent(studentId);
        validateDocumentAccess(user, student);
        return documentRepository.findByStudentIdAndActiveTrueOrderByCreatedAtDesc(studentId, pageable)
                .map(documentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentFileResponse downloadDocument(Long authenticatedUserId, Long documentId) {
        User user = getUser(authenticatedUserId);
        Document document = getActiveDocument(documentId);
        validateDocumentAccess(user, document.getStudent());
        return new DocumentFileResponse(
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize(),
                documentStorageService.loadAsResource(document.getStoredFileName())
        );
    }

    private User getUser(Long authenticatedUserId) {
        return userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private Student getActiveStudent(Long studentId) {
        return studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active student not found with id: " + studentId));
    }

    private Document getActiveDocument(Long documentId) {
        return documentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
    }

    private void validateDocumentAccess(User user, Student student) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() != Role.TEACHER) {
            throw new ResourceNotFoundException("Document access is not available for current user");
        }

        Long staffId = staffRepository.findByUser_IdAndActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"))
                .getId();

        if (student.getCurrentClass() == null) {
            throw new ResourceNotFoundException("Student not found in current teacher document access");
        }

        Long classId = student.getCurrentClass().getId();
        boolean classTeacherAccess = classRepository.existsByIdAndClassTeacherIdAndActiveTrue(classId, staffId);
        boolean teachingAccess = timetableRepository.existsByStaffIdAndStudentClassId(staffId, classId);

        if (!classTeacherAccess && !teachingAccess) {
            throw new ResourceNotFoundException("Student not found in current teacher document access");
        }
    }
}

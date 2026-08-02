package org.edu.service.impl;

import jakarta.persistence.criteria.JoinType;
import java.util.HashSet;
import java.util.Set;
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
import org.edu.util.DocumentType;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public DocumentDTO updateDocument(Long authenticatedUserId, Long documentId, DocumentUpdateRequest request, MultipartFile file) {
        User user = getUser(authenticatedUserId);
        Document document = getActiveDocument(documentId);
        validateDocumentAccess(user, document.getStudent());
        StoredDocumentFile storedFile = null;
        String previousStoredFileName = document.getStoredFileName();

        try {
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
            if (file != null && !file.isEmpty()) {
                storedFile = documentStorageService.store(file);
                document.setOriginalFileName(storedFile.getOriginalFileName());
                document.setStoredFileName(storedFile.getStoredFileName());
                document.setContentType(storedFile.getContentType());
                document.setFileSize(storedFile.getFileSize());
            }

            Document savedDocument = documentRepository.save(document);

            if (storedFile != null && previousStoredFileName != null && !previousStoredFileName.equals(savedDocument.getStoredFileName())) {
                documentStorageService.delete(previousStoredFileName);
            }

            return documentMapper.toDTO(savedDocument);
        } catch (RuntimeException ex) {
            if (storedFile != null) {
                documentStorageService.delete(storedFile.getStoredFileName());
            }
            throw ex;
        }
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
    public Page<DocumentDTO> filterDocuments(
            Long authenticatedUserId,
            Long studentId,
            Long classId,
            DocumentType documentType,
            Boolean visibleToParent,
            Pageable pageable
    ) {
        User user = getUser(authenticatedUserId);
        Specification<Document> specification = documentFilters(studentId, classId, documentType, visibleToParent)
                .and(accessibleDocuments(user));

        return documentRepository.findAll(specification, pageable)
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

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> getVisibleDocumentsByStudent(Long studentId, Pageable pageable) {
        getActiveStudent(studentId);
        return documentRepository.findByStudentIdAndVisibleToParentTrueAndActiveTrueOrderByCreatedAtDesc(studentId, pageable)
                .map(documentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentFileResponse downloadVisibleDocument(Long studentId, Long documentId) {
        getActiveStudent(studentId);
        Document document = documentRepository.findByIdAndStudentIdAndVisibleToParentTrueAndActiveTrue(documentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
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

    private Specification<Document> documentFilters(
            Long studentId,
            Long classId,
            DocumentType documentType,
            Boolean visibleToParent
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isTrue(root.get("active")));

            if (studentId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("student").get("id"), studentId));
            }
            if (classId != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("student").get("currentClass").get("id"), classId)
                );
            }
            if (documentType != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("documentType"), documentType));
            }
            if (visibleToParent != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("visibleToParent"), visibleToParent));
            }

            return predicate;
        };
    }

    private Specification<Document> accessibleDocuments(User user) {
        if (user.getRole() == Role.ADMIN) {
            return Specification.where(null);
        }

        if (user.getRole() != Role.TEACHER) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }

        Long staffId = staffRepository.findByUser_IdAndActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher profile not found for current user"))
                .getId();

        Set<Long> classIds = new HashSet<>();
        classRepository.findByClassTeacherIdAndActiveTrueOrderByNameAsc(staffId)
                .forEach(clazz -> classIds.add(clazz.getId()));
        timetableRepository.findByStaffIdOrderByDayOfWeekAscStartTimeAsc(staffId)
                .forEach(timetable -> classIds.add(timetable.getStudentClass().getId()));

        if (classIds.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }

        return (root, query, criteriaBuilder) -> root
                .join("student", JoinType.INNER)
                .join("currentClass", JoinType.INNER)
                .get("id")
                .in(classIds);
    }
}

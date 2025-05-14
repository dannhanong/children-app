package com.team.child_be.repositories;

import com.team.child_be.models.FileUpload;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    void deleteByFileCode(String fileCode);
    FileUpload findByFileCode(String fileCode);
}

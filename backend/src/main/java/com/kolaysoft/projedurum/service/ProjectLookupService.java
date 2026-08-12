package com.kolaysoft.projedurum.service;

import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectLookupService {

    private final ProjectRepository projectRepository;

    public Project findOr404(Long id) {
        return projectRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proje bulunamadi"));
    }
}

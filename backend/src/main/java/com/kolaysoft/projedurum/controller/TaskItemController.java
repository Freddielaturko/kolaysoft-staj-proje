package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.TaskItemRequest;
import com.kolaysoft.projedurum.dto.TaskItemResponse;
import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.TaskItem;
import com.kolaysoft.projedurum.repository.TaskItemRepository;
import com.kolaysoft.projedurum.security.ProjectAccessGuard;
import com.kolaysoft.projedurum.security.UserPrincipal;
import com.kolaysoft.projedurum.service.ProjectLookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class TaskItemController {

    private final TaskItemRepository taskItemRepository;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessGuard accessGuard;

    // PM: kendi projesine is kalemi ekler
    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskItemResponse createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Project project = projectLookupService.findOr404(projectId);
        accessGuard.ensureCanWrite(principal, project);

        TaskItem task = new TaskItem();
        applyRequest(task, request);
        task.setProject(project);

        return TaskItemResponse.from(taskItemRepository.save(task));
    }

    // PM: kendi projesindeki is kalemini gunceller (orn. durum degisikligi)
    @PutMapping("/api/projects/{projectId}/tasks/{taskId}")
    public TaskItemResponse updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Project project = projectLookupService.findOr404(projectId);
        accessGuard.ensureCanWrite(principal, project);

        TaskItem task = taskItemRepository.findById(Objects.requireNonNull(taskId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Is kalemi bulunamadi"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Is kalemi bu projeye ait degil");
        }

        applyRequest(task, request);

        return TaskItemResponse.from(taskItemRepository.save(task));
    }

    // PM: kendi projesinin is kalemleri | CTO: herhangi bir projenin is kalemleri
    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskItemResponse> listTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Project project = projectLookupService.findOr404(projectId);
        accessGuard.ensureCanAccess(principal, project);

        return taskItemRepository.findByProjectId(projectId)
                .stream().map(TaskItemResponse::from).toList();
    }

    private void applyRequest(TaskItem task, TaskItemRequest request) {
        task.setBaslik(request.baslik());
        task.setAciklama(request.aciklama());
        task.setSorumlu(request.sorumlu());
        task.setDurum(request.durum());
        task.setPlanlananTarih(request.planlananTarih());
        task.setTamamlananTarih(request.tamamlananTarih());
        task.setNot(request.not());
    }
}

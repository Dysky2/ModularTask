package net.edu.modulartask.tasktemplate;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-templates")
@CrossOrigin("http://localhost:5173")
public class TaskTemplateController {

    @Autowired
    TaskTemplateService taskTemplateService;

    @GetMapping("/all")
    public List<TaskTemplate> getAllTemplates() {
        return taskTemplateService.getAllTemplates();
    }

    @GetMapping("/{templateId}")
    public TaskTemplate getTaskTemplate(@PathVariable(name = "templateId") UUID templateId) {
        return taskTemplateService.findById(templateId);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createTaskTemplate(@Valid @RequestBody NewTaskTemplateDTO newTaskTemplateDTO) {
        return taskTemplateService.createTaskTemplate(newTaskTemplateDTO);
    }
}

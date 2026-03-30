package net.edu.modulartask.tasktemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
}

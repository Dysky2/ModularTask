package net.edu.modulartask.tasktemplate;

import net.edu.modulartask.subtask.SubtaskTemplate;
import net.edu.modulartask.subtask.SubtaskTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskTemplateService {

    @Autowired
    TaskTemplateRepository taskTemplateRepository;

    @Autowired
    SubtaskTemplateService subtaskTemplateService;

    public TaskTemplate findById(UUID taskTemplateId) {
        return taskTemplateRepository.findById(taskTemplateId).orElseThrow(
                () -> new IllegalArgumentException("Not found this template"));
    }

    public List<TaskTemplate> getAllTemplates() {
        List<TaskTemplate> templateList = taskTemplateRepository.findAll();

        List<TaskTemplate> resList = new ArrayList<>();

        for(var template : templateList) {
            List<SubtaskTemplate> subtaskTemplateList = subtaskTemplateService.findByTemplate(template);

            template.setSubtasks(subtaskTemplateList);

            resList.add(template);
        }

        return resList;
    }
}

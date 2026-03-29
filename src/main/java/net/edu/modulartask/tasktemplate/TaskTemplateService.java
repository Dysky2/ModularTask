package net.edu.modulartask.tasktemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskTemplateService {

    @Autowired
    TaskTemplateRepository taskTemplateRepository;

    public TaskTemplate findById(UUID taskTemplateId) {
        return taskTemplateRepository.findById(taskTemplateId).orElseThrow(
                () -> new IllegalArgumentException("Not found this template"));
    }
}

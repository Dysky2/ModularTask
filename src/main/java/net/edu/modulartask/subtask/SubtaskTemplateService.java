package net.edu.modulartask.subtask;

import net.edu.modulartask.tasktemplate.TaskTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubtaskTemplateService {

    @Autowired
    SubtaskTemplateRepository subtaskTemplateRepository;

    public List<SubtaskTemplate> findByTemplate(TaskTemplate taskTemplate) {
        return subtaskTemplateRepository.findByTemplate(taskTemplate);
    }

    public void saveSubTask(SubtaskTemplate subtaskTemplate) {
        subtaskTemplateRepository.save(subtaskTemplate);
    }

}

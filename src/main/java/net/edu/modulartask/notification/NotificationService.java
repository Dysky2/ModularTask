package net.edu.modulartask.notification;

import net.edu.modulartask.email.EmailSender;
import net.edu.modulartask.tasks.Task;
import net.edu.modulartask.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    EmailSender emailSender;

    public void notifyAssignment(Task task, User user) {
        emailSender.sendMail(user.getEmail(), task.getTitle(), "system@100kcompany.com", task.getDescription());
    }

    public void notifyRemovedFromTask(Task task, User user) {
        emailSender.sendMail(user.getEmail(), "Remove from " + task.getTitle(), "system@100kcompany.com", "You are not longer working on this task");
    }

}

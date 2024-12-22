package com.studenthive.studentHiveWeb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value="error", required=false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password, please try again.");
        }
        return "login";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        MongoDBManager mongoDBManager = new MongoDBManager();
        String[] daysOfTheWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        MongoCollection<Document> schedulesCollection = mongoDBManager.getCollection("schedules");

        Map<String, List<LessonDTO>> scheduleMap = new LinkedHashMap<>();

        for (String day : daysOfTheWeek) {
            List<Document> lessons = schedulesCollection.find(eq("day", day)).into(new ArrayList<>());

            List<LessonDTO> lessonDTOs = new ArrayList<>();
            if (!lessons.isEmpty()) {
                for (Document lesson : lessons) {
                    String number = lesson.getString("number");
                    String type = lesson.getString("type");
                    String time = lesson.getString("time");
                    String subject = lesson.getString("subject");
                    String lecturer = lesson.getString("lecturer");
                    String link = lesson.getString("link");
                    if (!link.startsWith("http")) {
                        link = "https://" + link;
                    }

                    LessonDTO lessonDTO = new LessonDTO(number, type, time, subject, lecturer, link);
                    System.out.println(lessonDTO);
                    lessonDTOs.add(lessonDTO);
                }
            }


            scheduleMap.put(day, lessonDTOs);
        }
        System.out.println(scheduleMap);

        model.addAttribute("schedule", scheduleMap);

        return "schedule";
    }

}

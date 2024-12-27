package com.studenthive.studentHiveWeb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
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
                    lessonDTOs.add(lessonDTO);
                }
            }


            scheduleMap.put(day, lessonDTOs);
        }

        model.addAttribute("schedule", scheduleMap);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "admin/schedule";
        }

        return "schedule";
    }

    @GetMapping("/schedule/edit/{day}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editSchedule(@PathVariable("day") String day, Model model) {
        MongoDBManager mongoDBManager = new MongoDBManager();
        MongoCollection<Document> schedulesCollection = mongoDBManager.getCollection("schedules");
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
                ObjectId lessonID = lesson.getObjectId("_id");
                if (!link.startsWith("http")) {
                    link = "https://" + link;
                }

                LessonDTO lessonDTO = new LessonDTO(number, type, time, subject, lecturer, link, lessonID);
                lessonDTOs.add(lessonDTO);
            }
        }
        model.addAttribute("lessons", lessonDTOs);
        model.addAttribute("day", day);

        return "admin/schedule-edit";
    }

    @PostMapping("/schedule/update/{day}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateSchedule(
            @PathVariable("day") String day,
            @RequestParam("number[]") List<String> numbers,
            @RequestParam("type[]") List<String> types,
            @RequestParam("time[]") List<String> times,
            @RequestParam("subject[]") List<String> subjects,
            @RequestParam("lecturer[]") List<String> lecturers,
            @RequestParam("link[]") List<String> links) {

        MongoDBManager mongoDBManager = new MongoDBManager();
        MongoCollection<Document> schedulesCollection = mongoDBManager.getCollection("schedules");
        schedulesCollection.deleteMany(new Document("day", day));

        List<Document> newLessons = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            Document lesson = new Document()
                    .append("day", day)
                    .append("number", numbers.get(i))
                    .append("type", types.get(i))
                    .append("time", times.get(i))
                    .append("subject", subjects.get(i))
                    .append("lecturer", lecturers.get(i))
                    .append("link", links.get(i));
            schedulesCollection.insertOne(lesson);
        }

        return "redirect:/schedule";
    }

    @PostMapping("/schedule/clear/{day}")
    @PreAuthorize("hasRole('ADMIN')")
    public String clearSchedule(@PathVariable("day") String day) {
        MongoDBManager mongoDBManager = new MongoDBManager();
        MongoCollection<Document> schedulesCollection = mongoDBManager.getCollection("schedules");
        schedulesCollection.deleteMany(eq("day", day));

        return "redirect:/schedule";
    }

    @GetMapping("/attendance")
    public String attendance(Model model) {
        MongoDBManager mongoDBManager = new MongoDBManager();
        MongoCollection<Document> attendanceCollection = mongoDBManager.getCollection("attendance");

        List<Document> attendeesList = attendanceCollection.find().into(new ArrayList<>());

        Map<String, Map<String, List<String>>> grouped = new TreeMap<>();

        for (Document doc : attendeesList) {
            String day = doc.getString("day");
            String subject = doc.getString("subject");
            int lesson = doc.getInteger("lesson");
            List<List<String>> attendees = (List<List<String>>) doc.get("attendees");

            String formattedSubject = lesson + " - " + subject;

            List<String> attendeesDisplay = new ArrayList<>();
            for (List<String> attendeeGroup : attendees) {
                if (attendeeGroup.size() >= 2) {
                    String username = attendeeGroup.get(0);
                    String firstName = attendeeGroup.get(1);
                    String lastName = attendeeGroup.get(2);
                    if (lastName != null) {
                        attendeesDisplay.add(firstName + " (" + username + ") " + lastName);
                    }
                    else {
                        attendeesDisplay.add(firstName + " (" + username + ")");
                    }
                }
            }

            grouped.putIfAbsent(day, new TreeMap<>());
            Map<String, List<String>> dayMap = grouped.get(day);

            dayMap.putIfAbsent(formattedSubject, new ArrayList<>());
            dayMap.get(formattedSubject).addAll(attendeesDisplay);
        }


        model.addAttribute("groupedData", grouped);

        return "attendance"; // Render the attendance page
    }

}

package com.edusphere.platform.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {
    private final List<Map<String, Object>> users = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> events = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> notifications = new CopyOnWriteArrayList<>();

    public ApiController() {
        users.add(user(1, "Бақтыбаева Акерке", "student@edusphere.kz", "student123", "STUDENT", "IT-23"));
        users.add(user(2, "Демо студент", "demo@edusphere.kz", "demo123", "STUDENT", "IT-23"));
        events.add(event(1, "IT Conference", "AI және Java бойынша университеттік конференция", "2026-05-15", "10:00", "Main Hall", "Conference"));
        events.add(event(2, "Dance Casting", "Студенттік концертке кастинг", "2026-05-18", "15:30", "A-204", "Casting"));
        notifications.add(note("Жаңа баға", "Database Systems пәнінен баға қойылды"));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "");
        String password = body.getOrDefault("password", "");
        for (Map<String, Object> u : users) {
            if (email.equals(u.get("email")) && password.equals(u.get("password"))) {
                return Map.of("success", true, "user", publicUser(u), "token", "demo-token-" + u.get("id"));
            }
        }
        return Map.of("success", false, "message", "Email немесе құпиясөз қате");
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        int id = users.size() + 1;
        Map<String, Object> u = user(id, body.getOrDefault("name", "New Student"), body.getOrDefault("email", "student" + id + "@mail.kz"), body.getOrDefault("password", "123456"), "STUDENT", body.getOrDefault("group", "IT-23"));
        users.add(u);
        notifications.add(note("Жаңа студент", u.get("name") + " платформаға тіркелді"));
        return Map.of("success", true, "user", publicUser(u));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
            "users", users.size(),
            "courses", 4,
            "lessons", 12,
            "average", 81,
            "aiInsight", "AI кеңесі: Database Systems бойынша күн сайын 35 минут қайталау жоспарын орындаңыз."
        );
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return users.stream().map(this::publicUser).toList();
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> courses() {
        return List.of(
            Map.of("title", "Advanced Java", "teacher", "Сая Мұғалім", "progress", 74, "credits", 5),
            Map.of("title", "Database Systems", "teacher", "Сая Мұғалім", "progress", 64, "credits", 5),
            Map.of("title", "Web Development", "teacher", "Сая Мұғалім", "progress", 88, "credits", 4),
            Map.of("title", "Artificial Intelligence", "teacher", "Сая Мұғалім", "progress", 92, "credits", 5)
        );
    }

    @GetMapping("/schedule")
    public List<Map<String, Object>> schedule() {
        return List.of(
            Map.of("year", "2025-2026", "semester", "Spring", "day", "Monday", "time", "09:00-10:30", "subject", "Advanced Java", "room", "A-301"),
            Map.of("year", "2025-2026", "semester", "Spring", "day", "Tuesday", "time", "11:00-12:30", "subject", "Database Systems", "room", "B-204"),
            Map.of("year", "2025-2026", "semester", "Spring", "day", "Thursday", "time", "14:00-15:30", "subject", "AI Lab", "room", "AI Center")
        );
    }

    @GetMapping("/grades")
    public List<Map<String, Object>> grades() {
        return List.of(
            Map.of("course", "Advanced Java", "midterm", 82, "finalExam", 88, "total", 85, "letter", "A-"),
            Map.of("course", "Database Systems", "midterm", 60, "finalExam", 68, "total", 64, "letter", "C"),
            Map.of("course", "Web Development", "midterm", 90, "finalExam", 86, "total", 88, "letter", "B+")
        );
    }

    @GetMapping("/events")
    public List<Map<String, Object>> getEvents() {
        return events;
    }

    @PostMapping("/events")
    public Map<String, Object> addEvent(@RequestBody Map<String, String> body) {
        Map<String, Object> ev = event(events.size() + 1, body.getOrDefault("title", "Student event"), body.getOrDefault("description", ""), body.getOrDefault("date", "2026-05-20"), body.getOrDefault("time", "12:00"), body.getOrDefault("place", "Campus"), body.getOrDefault("type", "Event"));
        events.add(ev);
        sendNotificationToAll("Жаңа іс-шара: " + ev.get("title") + " | " + ev.get("date") + " " + ev.get("time") + " | " + ev.get("place"));
        return Map.of("success", true, "event", ev);
    }

    @GetMapping("/notifications")
    public List<Map<String, Object>> getNotifications() {
        return notifications;
    }

    @PostMapping("/ai")
    public Map<String, Object> ai(@RequestBody Map<String, String> body) {
        String q = body.getOrDefault("question", "").trim();
        return Map.of("answer", buildAiAnswer(q), "time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(defaultValue = "") String q) {
        String query = q.toLowerCase();
        List<String> results = new ArrayList<>();
        if ("course сабақ java database ai".contains(query) || query.isBlank()) results.add("Courses: Advanced Java, Database Systems, AI");
        if ("grade баға transcript gpa".contains(query) || query.isBlank()) results.add("Grades/Transcript: GPA, midterm, final exam");
        if ("schedule расписание кесте".contains(query) || query.isBlank()) results.add("Schedule: academic year, semester, lesson time, room");
        if ("event блог casting concert conference".contains(query) || query.isBlank()) results.add("Campus Events: concerts, castings, conferences");
        return Map.of("query", q, "results", results);
    }

    private String buildAiAnswer(String question) {
        String q = question.toLowerCase();
        if (q.isBlank()) return "Сұрағыңызды жазыңыз. Мен оқу, баға, кесте, пәндер және студенттік іс-шаралар бойынша көмектесемін.";
        if (q.contains("id")) return "ID — жүйедегі пайдаланушының бірегей нөмірі. Мысалы, студент, курс немесе баға жазбасын база ішінде ажырату үшін қолданылады.";
        if (q.contains("gpa") || q.contains("орташа") || q.contains("баға")) return "GPA/орташа баға пәндердегі қорытынды нәтижелер арқылы есептеледі. Қазір орташа көрсеткіш: 81%. Әлсіз пән: Database Systems — 64%.";
        if (q.contains("java")) return "Java — объектіге бағытталған бағдарламалау тілі. Бұл жобада Java Spring Boot сервер логикасын, API және AI көмекші модулін іске қосу үшін қолданылды.";
        if (q.contains("sql") || q.contains("database")) return "SQL — деректер базасымен жұмыс істеу тілі. Мысалы: SELECT, INSERT, UPDATE, DELETE. Бұл платформада студенттер, курстар, бағалар сияқты деректер сақталады.";
        if (q.contains("кесте") || q.contains("schedule") || q.contains("расписание")) return "Сабақ кестесінде оқу жылы, семестр, пән атауы, басталу/аяқталу уақыты және аудитория көрсетіледі.";
        if (q.contains("event") || q.contains("іс-шара") || q.contains("концерт") || q.contains("кастинг")) return "Campus Events бөлімінде студенттер концерт, кастинг, конференция сияқты іс-шараларды қосып, уақытын, орнын көрсетіп, басқа студенттерге хабарлама жібере алады.";
        if (q.contains("rest")) return "REST API — frontend пен backend арасындағы байланыс тәсілі. Мысалы, /api/courses курстарды, /api/ai AI жауабын қайтарады.";
        return "AI жауап: сұрағыңыз қабылданды. Мен оны оқу платформасы контекстінде түсіндім. Нақты пән, баға, кесте немесе тапсырма атауын жазсаңыз, толық жоспар ұсынамын.";
    }

    private void sendNotificationToAll(String message) {
        notifications.add(note("Campus notification", message));
    }

    private Map<String, Object> user(int id, String name, String email, String password, String role, String group) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("email", email); m.put("password", password); m.put("role", role); m.put("group", group);
        return m;
    }
    private Map<String, Object> publicUser(Map<String, Object> u) {
        return Map.of("id", u.get("id"), "name", u.get("name"), "email", u.get("email"), "role", u.get("role"), "group", u.get("group"));
    }
    private Map<String, Object> event(int id, String title, String description, String date, String time, String place, String type) {
        return Map.of("id", id, "title", title, "description", description, "date", date, "time", time, "place", place, "type", type);
    }
    private Map<String, Object> note(String title, String text) {
        return Map.of("title", title, "text", text, "time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }
}

package service;
import dao.StudentDao;
import Model.Student;

public class StudentService {
    StudentDao dao = new StudentDao();

    public void registerStudent(int id, String name) {
        // Simple Rule: Naam khali nahi hona chahiye
        if (name != "") {
            Student s = new Student(id, name);
            dao.saveToDatabase(s);
        } else {
            System.out.println("Naam galat hai!");
        }
    }
}

package dao;
import Model.Student;

public class StudentDao {
    public void saveToDatabase(Student student) {
        System.out.println("Database mein save ho gaya: " + student.name);
    }
}
package dao;

import Model.Library;
import java.util.List;

/**
 * Interface defining Library Data Access operations.
 * Satisfies Day 3: Nearby libraries search by city/pincode and distance sorting.
 */
public interface LibraryDAO {
    List<Library> getAllLibraries();
    Library getLibraryById(String libraryId);
    List<Library> searchByLocation(String query);
    void addLibrary(Library library);
}

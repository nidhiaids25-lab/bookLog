package service;

import Model.Library;
import dao.DAOFactory;
import dao.LibraryDAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing Library branches, location searching, and distance sorting.
 * Satisfies Day 3: Nearby libraries (city or pincode match) + distance sorting.
 */
public class LibraryService {
    private final LibraryDAO libraryDAO;

    public LibraryService() {
        this.libraryDAO = DAOFactory.getLibraryDAO();
    }

    public LibraryService(LibraryDAO libraryDAO) {
        this.libraryDAO = libraryDAO;
    }

    public List<Library> getAllLibraries() {
        List<Library> list = libraryDAO.getAllLibraries();
        list.sort(Comparator.comparingDouble(Library::getDistanceKm));
        return list;
    }

    public Library getLibraryById(String libraryId) {
        return libraryDAO.getLibraryById(libraryId);
    }

    /**
     * Search libraries by city, pincode, or branch name, sorted by distance (nearest first).
     */
    public List<Library> searchNearbyLibraries(String query) {
        List<Library> results = libraryDAO.searchByLocation(query);
        results.sort(Comparator.comparingDouble(Library::getDistanceKm));
        return results;
    }
}

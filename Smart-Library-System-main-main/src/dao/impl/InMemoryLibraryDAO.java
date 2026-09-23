package dao.impl;

import Model.Library;
import dao.LibraryDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-Memory thread-safe implementation of LibraryDAO.
 * Seeded with branch libraries in major cities with pincodes and distance values for sorting.
 */
public class InMemoryLibraryDAO implements LibraryDAO {
    private final Map<String, Library> libraries = new ConcurrentHashMap<>();

    public InMemoryLibraryDAO() {
        seedLibraries();
    }

    private void seedLibraries() {
        addLibrary(new Library("LIB-01", "Central Knowledge Hub", "Delhi", "110001", "Connaught Place, Central Wing", 1.2, "+91 11 2345 6789"));
        addLibrary(new Library("LIB-02", "North Campus Academic Library", "Delhi", "110007", "University Enclave, North Campus", 3.8, "+91 11 2766 1122"));
        addLibrary(new Library("LIB-03", "South Extension Digital Commons", "Delhi", "110049", "Ring Road, South Ext-Part II", 5.4, "+91 11 4164 9900"));
        addLibrary(new Library("LIB-04", "Marine Drive State Library", "Mumbai", "400020", "Nariman Point Promenade", 2.1, "+91 22 2281 3400"));
        addLibrary(new Library("LIB-05", "Bandra West Public Reading Room", "Mumbai", "400050", "Hill Road, Bandra West", 6.7, "+91 22 2642 7788"));
        addLibrary(new Library("LIB-06", "Indiranagar Tech Library", "Bengaluru", "560038", "100 Feet Road, Indiranagar", 1.8, "+91 80 2521 8844"));
        addLibrary(new Library("LIB-07", "Koramangala Innovation Commons", "Bengaluru", "560034", "80 Feet Main Road, 4th Block", 4.2, "+91 80 4123 5500"));
        addLibrary(new Library("LIB-08", "Cyber City Reference Hub", "Hyderabad", "500081", "HITEC City Phase 2", 3.1, "+91 40 6789 1234"));
        addLibrary(new Library("LIB-09", "Shivaji Nagar Central Archive", "Pune", "411005", "Fergusson College Road", 2.5, "+91 20 2553 4400"));
    }

    @Override
    public void addLibrary(Library library) {
        if (library != null && library.getLibraryId() != null) {
            libraries.put(library.getLibraryId(), library);
        }
    }

    @Override
    public List<Library> getAllLibraries() {
        return new ArrayList<>(libraries.values());
    }

    @Override
    public Library getLibraryById(String libraryId) {
        if (libraryId == null) return null;
        return libraries.get(libraryId);
    }

    @Override
    public List<Library> searchByLocation(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllLibraries();
        }
        String q = query.trim().toLowerCase();
        return libraries.values().stream()
                .filter(lib -> lib.matchesLocation(q))
                .sorted(Comparator.comparingDouble(Library::getDistanceKm))
                .collect(Collectors.toList());
    }
}

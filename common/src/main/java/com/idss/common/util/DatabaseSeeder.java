package com.idss.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idss.common.config.MongoConnection;
import com.idss.common.model.Exam;
import com.idss.common.model.Room;
import com.idss.common.model.Student;
import com.idss.common.model.Timeslot;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database seeder (master_context_file.md Section 5).
 *
 * <p>Loads the registry input JSON files from {@code data/input/} and inserts
 * them into the corresponding Mongo collections: {@code exams}, {@code students},
 * {@code rooms}, {@code timeslots}. Output collections (conflict_graph,
 * room_rankings, master_schedules, etc.) are produced by the task modules at
 * runtime and are NOT seeded here.</p>
 *
 * <p>Each collection is cleared before insert so the seeder is idempotent.
 * Missing input files are skipped with a warning rather than failing, so the
 * seeder can run as soon as a subset of datasets is available.</p>
 */
public final class DatabaseSeeder {

    private static final ObjectMapper MAPPER = JsonLoader.mapper();
    private static final String INPUT_DIR = "data/input";

    private DatabaseSeeder() {
        throw new AssertionError("DatabaseSeeder is a utility; do not instantiate.");
    }

    /** Seeds the default database (from {@code .env}) using {@code data/input/}. */
    public static void seed() throws IOException {
        seed(MongoConnection.getDatabase());
    }

    /** Seeds the given database using {@code data/input/}. */
    public static void seed(MongoDatabase db) throws IOException {
        seedCollection(db, "exams", "input_exams.json", Exam.class);
        seedCollection(db, "students", "input_student_enrollments.json", Student.class);
        seedCollection(db, "rooms", "input_room_master.json", Room.class);
        seedCollection(db, "timeslots", "input_timeslots.json", Timeslot.class);
    }

    private static <T> void seedCollection(MongoDatabase db, String collection,
                                           String fileName, Class<T> type) throws IOException {
        String relativePath = INPUT_DIR + "/" + fileName;
        if (!JsonLoader.resolve(relativePath).exists()) {
            System.err.println("[Seeder] Skipping " + collection + ": "
                    + JsonLoader.resolve(relativePath).getAbsolutePath() + " not found");
            return;
        }
        List<T> items = JsonLoader.loadList(relativePath, type);
        MongoCollection<Document> coll = db.getCollection(collection);
        coll.deleteMany(new Document());
        List<Document> docs = new ArrayList<>();
        for (T item : items) {
            docs.add(Document.parse(MAPPER.writeValueAsString(item)));
        }
        if (!docs.isEmpty()) {
            coll.insertMany(docs);
        }
        System.out.println("[Seeder] Seeded " + docs.size() + " docs into " + collection);
    }
}

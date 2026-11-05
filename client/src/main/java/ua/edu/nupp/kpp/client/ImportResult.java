package ua.edu.nupp.kpp.client;

import java.util.List;

public record ImportResult(int imported, int skipped, List<String> errors) {
}

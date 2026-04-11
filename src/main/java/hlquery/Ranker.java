package hlquery;

import java.util.HashMap;
import java.util.Map;

/**
 * Ranking helpers used by hlquery API clients.
 */
public final class Ranker {
    private static final Map<String, Double> DEFAULT_WEIGHTS = Map.of(
            "popularity_log", 1.15,
            "hit_log", 0.95,
            "popularity_sqrt", 0.25,
            "hit_log_sqrt", 0.15
    );

    private Ranker() {
        // Utility class
    }

    public static double computeRankSignal(double popularity, double hitLog, Map<String, Double> overrides) {
        Map<String, Double> weights = new HashMap<>(DEFAULT_WEIGHTS);
        if (overrides != null) {
            overrides.forEach((key, value) -> {
                if (value != null && weights.containsKey(key)) {
                    weights.put(key, value);
                }
            });
        }

        return Math.log(popularity + 1) * weights.get("popularity_log")
                + Math.log(hitLog + 1) * weights.get("hit_log")
                + Math.sqrt(popularity) * weights.get("popularity_sqrt")
                + Math.sqrt(hitLog) * weights.get("hit_log_sqrt");
    }

    public static void attachRankSort(Map<String, Object> params, String field, String direction) {
        if (params == null) {
            return;
        }

        direction = direction == null ? "desc" : direction.toLowerCase();
        if (!"asc".equals(direction) && !"desc".equals(direction)) {
            direction = "desc";
        }

        String sortInstruction = field + ":" + direction;
        Object existing = params.get("sort_by");
        if (existing instanceof String && !((String) existing).trim().isEmpty()) {
            params.put("sort_by", existing + "," + sortInstruction);
        } else {
            params.put("sort_by", sortInstruction);
        }
    }
}

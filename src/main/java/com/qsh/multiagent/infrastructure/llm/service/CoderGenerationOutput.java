package com.qsh.multiagent.infrastructure.llm.service;

import java.util.List;

public record CoderGenerationOutput(Boolean passed,
                                    String changeSummary,
                                    List<String> changedFiles,
                                    String codeDraft,
                                    String risks) {
}

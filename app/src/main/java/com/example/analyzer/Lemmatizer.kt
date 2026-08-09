package com.example.analyzer

object Lemmatizer {

    private val irregularMap = mapOf(
        "am" to "be", "is" to "be", "are" to "be", "was" to "be", "were" to "be", "been" to "be", "being" to "be",
        "has" to "have", "had" to "have", "having" to "have",
        "does" to "do", "did" to "do", "done" to "do", "doing" to "do",
        "went" to "go", "gone" to "go", "going" to "go", "goes" to "go", "gonna" to "go",
        "came" to "come", "coming" to "come",
        "took" to "take", "taken" to "take", "taking" to "take",
        "saw" to "see", "seen" to "see", "seeing" to "see",
        "made" to "make", "making" to "make",
        "knew" to "know", "known" to "know", "knowing" to "know",
        "thought" to "think", "thinking" to "think",
        "found" to "find", "finding" to "find",
        "gave" to "give", "given" to "give", "giving" to "give",
        "told" to "tell", "telling" to "tell",
        "became" to "become", "becoming" to "become",
        "left" to "leave", "leaving" to "leave",
        "felt" to "feel", "feeling" to "feel",
        "brought" to "bring", "bringing" to "bring",
        "wrote" to "write", "written" to "write", "writing" to "write",
        "sat" to "sit", "sitting" to "sit",
        "stood" to "stand", "standing" to "stand",
        "lost" to "lose", "losing" to "lose",
        "paid" to "pay", "paying" to "pay", "pays" to "pay",
        "met" to "meet", "meeting" to "meet",
        "ran" to "run", "running" to "run", "runs" to "run",
        "spoke" to "speak", "spoken" to "speak", "speaking" to "speak",
        "read" to "read", "reading" to "read",
        "grew" to "grow", "grown" to "grow", "growing" to "grow",
        "kept" to "keep", "keeping" to "keep",
        "began" to "begin", "begun" to "begin", "beginning" to "begin",
        "held" to "hold", "holding" to "hold",
        "bought" to "buy", "buying" to "buy",
        "understood" to "understand", "understanding" to "understand",
        "better" to "good", "best" to "good", "worse" to "bad", "worst" to "bad",
        "children" to "child", "men" to "man", "women" to "woman", "feet" to "foot",
        "teeth" to "tooth", "geese" to "goose", "mice" to "mouse", "people" to "person"
    )

    fun getLemma(word: String): String {
        val clean = word.lowercase().trim()
        if (clean.length <= 2) return clean

        // Check irregular map first
        irregularMap[clean]?.let { return it }

        // Suffix rules
        when {
            clean.endsWith("ies") && clean.length > 4 -> return clean.dropLast(3) + "y"
            clean.endsWith("ves") && clean.length > 4 -> return clean.dropLast(3) + "f"
            clean.endsWith("es") && (clean.endsWith("shes") || clean.endsWith("ches") || clean.endsWith("xes") || clean.endsWith("sses") || clean.endsWith("zzes")) -> return clean.dropLast(2)
            clean.endsWith("s") && !clean.endsWith("ss") && !clean.endsWith("us") && !clean.endsWith("is") -> return clean.dropLast(1)
            clean.endsWith("ing") && clean.length > 5 -> {
                val base = clean.dropLast(3)
                return when {
                    base.endsWith("tt") || base.endsWith("pp") || base.endsWith("nn") || base.endsWith("mm") || base.endsWith("rr") || base.endsWith("gg") -> base.dropLast(1)
                    else -> base
                }
            }
            clean.endsWith("ed") && clean.length > 4 -> {
                val base = clean.dropLast(2)
                return when {
                    clean.endsWith("ied") -> clean.dropLast(3) + "y"
                    base.endsWith("tt") || base.endsWith("pp") || base.endsWith("nn") || base.endsWith("mm") || base.endsWith("rr") || base.endsWith("gg") -> base.dropLast(1)
                    else -> base
                }
            }
            clean.endsWith("ly") && clean.length > 4 -> return clean.dropLast(2)
            clean.endsWith("er") && clean.length > 4 -> return clean.dropLast(2)
            clean.endsWith("est") && clean.length > 5 -> return clean.dropLast(3)
        }

        return clean
    }
}
